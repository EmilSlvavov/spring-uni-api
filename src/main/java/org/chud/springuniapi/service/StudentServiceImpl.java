package org.chud.springuniapi.service;

import org.chud.springuniapi.application_events.event.EnrollStudentEvent;
import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.request.UpdateStudentProfileRequest;
import org.chud.springuniapi.dto.request.UpdateStudentRequest;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.StudentDisplayResponse;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.dto.response.StudentSoftDeleteResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.StudentMapper;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.StudentRepository;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.chud.springuniapi.service.serviceInterface.IStudentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class StudentServiceImpl implements IStudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentMapper studentMapper;
    private final ApplicationEventPublisher eventPublisher;

    public StudentServiceImpl(StudentRepository studentRepository, CourseRepository courseRepository, StudentMapper studentMapper, ApplicationEventPublisher eventPublisher) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.studentMapper = studentMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<StudentResponse> findAll(Boolean deleted) {
        List<Student> students = studentRepository.findAll();
        //get the map of courses filtered by soft delete used to show users with their courses
        Map<Long, List<CourseSummaryResponse>> coursesByStudent = coursesByStudent(students, deleted);

        return students.stream()
                .map(student -> studentMapper.toResponse(student, coursesOf(coursesByStudent, student)))
                .toList();
    }

    @Override
    public StudentResponse findById(Long id, Boolean deleted) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        return studentMapper.toResponse(student, coursesOf(student, deleted));
    }

    @Override
    public List<StudentDisplayResponse> findDisplayLabels() {
        return studentRepository.findAllProjectedBy().stream()
                .map(view ->
                        new StudentDisplayResponse(view.getName(),
                                view.getDisplayLabel()))
                .toList();
    }

    @Override
    public List<StudentSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted) {
        List<Student> students = studentRepository.findStudentsByDeleted(isDeleted);
        Map<Long, List<CourseSummaryResponse>> coursesByStudent = coursesByStudent(students, null);

        return students.stream()
            .map(student -> studentMapper.toResponseWithSoftDelete(student, coursesOf(coursesByStudent, student)))
            .toList();
    }

    //changed method so it checks if it won the race condition and throws if not
    @Override
    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        if (studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException(
                    "Email '%s' is already registered".formatted(request.email()));
        }

        Student saved;
        try{
            saved = studentRepository.saveAndFlush(new Student(request.name(), request.email()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException(
                    "Email '%s' is already registered".formatted(request.email())
            );
        }
        //a fresh student has no courses yet, so there is nothing to query for
        return studentMapper.toResponse(saved, List.of());
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setName(request.name()); // no save() flush() will save changes
        return studentMapper.toResponse(student, coursesOf(student, null));
    }

    @Override
    @Transactional
    public StudentResponse updateProfile(Long id, UpdateStudentProfileRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setBio(request.bio());
        student.setDateOfBirth(request.dateOfBirth());

        return studentMapper.toResponse(student, coursesOf(student, null));
    }

    @Override
    @Transactional
    public StudentResponse enroll(Long studentId, Long courseId) {
        Student student = studentRepository.findWithCoursesById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Course course = courseRepository.findWithLockById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        student.enroll(course); // again dirty checking saves it
        studentRepository.flush(); // push the join row out so the summary query sees it

        eventPublisher.publishEvent(new EnrollStudentEvent(studentId, courseId, course.getName()));

        return studentMapper.toResponse(student, coursesOf(student, null));
    }

    @Override
    @Transactional
    public StudentResponse withdraw(Long studentId, Long courseId) {
        Student student = studentRepository.findWithCoursesById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        student.withdraw(course); // again dirty checking saves it
        studentRepository.flush(); // drop the join row before we read the summaries back

        return studentMapper.toResponse(student, coursesOf(student, null));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        for (Course course : Set.copyOf(student.getCourses())) {
            student.withdraw(course);
        }

        studentRepository.delete(student);
    }

    @Override
    @Transactional
    public StudentSoftDeleteResponse softDelete(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setDeleted(true);
        return studentMapper.toResponseWithSoftDelete(student, coursesOf(student, null));
    }

    @Override
    @Transactional
    public StudentSoftDeleteResponse restoreSoftDelete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setDeleted(false);
        return studentMapper.toResponseWithSoftDelete(student, coursesOf(student, null));
    }

    //get the courses by student with filter for soft delete value
    private Map<Long, List<CourseSummaryResponse>> coursesByStudent(List<Student> students, Boolean deleted) {
        //get the ids of the students
        List<Long> ids = students.stream().map(Student::getId).toList();
        if (ids.isEmpty()) { //check for empty list
            return Map.of();
        }

        //findCourseSummariesByStudentIds returns a list of CourseSummaryRow and then we apply
        // the groupByOwner method which returns a map with keys of owner ids and values of CourseSummaryResponse
        return CourseSummaryRow.groupByOwner(
                studentRepository.findCourseSummariesByStudentIds(ids, deleted));
    }

    //for this method you have a map of ownerId values and their courses and the second parameter
    // is the student you want the courses of. You get the courses of that student in a list or if
    // there arent any you set his courses to be an empty list
    //used for pulling out one entry after the map is already built
    private List<CourseSummaryResponse> coursesOf(Map<Long, List<CourseSummaryResponse>> coursesByStudent,
                                                  Student student) {

        return coursesByStudent.getOrDefault(student.getId(), List.of());
    }

    //used for single entity paths which dont deal with multiple students like enroll, update etc.
    private List<CourseSummaryResponse> coursesOf(Student student, Boolean deleted) {
        return coursesOf(coursesByStudent(List.of(student), deleted), student);
    }
}
