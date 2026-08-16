package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.request.UpdateStudentProfileRequest;
import org.chud.springuniapi.dto.request.UpdateStudentRequest;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.studentMapper = studentMapper;
    }

    public List<StudentResponse> findAll(Boolean deleted) {
        return studentRepository.findAll().stream()
                .map(student -> studentMapper.toResponse(student, deleted))
                .toList();
    }

    public StudentResponse findById(Long id, Boolean deleted) {
        Student student = studentRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        return studentMapper.toResponse(student, deleted);
    }

    public List<StudentDisplayResponse> findDisplayLabels() {
        return studentRepository.findAllProjectedBy().stream()
                .map(view ->
                        new StudentDisplayResponse(view.getName(),
                                view.getDisplayLabel()))
                .toList();
    }

    public List<StudentSoftDeleteResponse> findAllBySoftDeleted(boolean  isDeleted) {
        return studentRepository.findStudentsByDeleted(isDeleted).stream()
            .map(studentMapper::toResponseWithSoftDelete)
            .toList();
    }

    //changed method so it checks if it won the race condition and throws if not
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
        return studentMapper.toResponse(saved);
    }

    @Transactional
    public StudentResponse update(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setName(request.name()); // no save() flush() will save changes
        return studentMapper.toResponse(student);
    }

    @Transactional
    public StudentResponse updateProfile(Long id, UpdateStudentProfileRequest request) {
        Student student = studentRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setBio(request.bio());
        student.setDateOfBirth(request.dateOfBirth());

        return studentMapper.toResponse(student);
    }

    @Transactional
    public StudentResponse enroll(Long studentId, Long courseId) {
        Student student = studentRepository.findWithCoursesById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Course course = courseRepository.findWithLockById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        student.enroll(course); // again dirty checking saves it
        return studentMapper.toResponse(student);
    }

    @Transactional
    public StudentResponse withdraw(Long studentId, Long courseId) {
        Student student = studentRepository.findWithCoursesById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        student.withdraw(course); // again dirty checking saves it
        return studentMapper.toResponse(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        
        for (Course course : Set.copyOf(student.getCourses())) {
            student.withdraw(course);
        }

        studentRepository.delete(student);
    }

    @Transactional
    public StudentSoftDeleteResponse softDelete(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setDeleted(true);
        return studentMapper.toResponseWithSoftDelete(student);
    }

    @Transactional
    public StudentSoftDeleteResponse restoreSoftDelete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setDeleted(false);
        return studentMapper.toResponseWithSoftDelete(student);
    }
}
