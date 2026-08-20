package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;
import org.chud.springuniapi.dto.response.StudentSummaryResponse;
import org.chud.springuniapi.entity.*;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.CourseMapper;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.DepartmentRepository;
import org.chud.springuniapi.repository.projection.CourseSummaryView;
import org.chud.springuniapi.repository.projection.StudentSummaryRow;
import org.chud.springuniapi.service.serviceInterface.ICourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements ICourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseMapper courseMapper;

    public CourseServiceImpl(CourseRepository courseRepository, DepartmentRepository departmentRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.courseMapper = courseMapper;
    }

    @Override
    public List<CourseResponse> findAll(Boolean deleted) {
        List<Course> courses = courseRepository.findAllWithDepartment();
        Map<Long, List<StudentSummaryResponse>> studentsByCourse = studentsByCourse(courses, deleted);

        return courses.stream()
                .map(course -> courseMapper.toResponse(course, studentsOf(studentsByCourse, course)))
                .toList();
    }

    @Override
    public CourseResponse findById(Long id, Boolean deleted) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        return courseMapper.toResponse(course, studentsOf(course, deleted));
    }

    //Changed ordering to avoid passing existence check even if right after it somebody deletes it
    @Override
    public List<CourseListItemResponse> findSummariesByDepartment(Long departmentId) {
        List<CourseSummaryView> views = courseRepository.findByDepartmentId(departmentId, CourseSummaryView.class);

        if (views.isEmpty() && !departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", departmentId);
        }

        return views.stream().map(view -> new CourseListItemResponse(
                        view.getId(),
                        view.getName(),
                        view.getDepartment().getName()))
                .toList();
    }

    //changed ordering here the same way from above
    @Override
    public List<CourseResponse> findByDepartment(Long departmentId, Boolean deleted) {

        List<Course> courses = courseRepository.findByDepartmentId(departmentId);

        if (courses.isEmpty() && !departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", departmentId);
        }

        Map<Long, List<StudentSummaryResponse>> studentsByCourse = studentsByCourse(courses, deleted);

        return courses.stream()
                .map(course -> courseMapper.toResponse(course, studentsOf(studentsByCourse, course)))
                .toList();
    }

    @Override
    public List<CourseSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted){
        return courseRepository
            .findCourseByDeleted(isDeleted)
            .stream()
            .map(courseMapper::toResponseWithSoftDelete)
            .toList();
    }

    @Override
    @Transactional
    public CourseResponse createOnline(CreateOnlineCourseRequest request) {
        Department department = requireDepartment(request.departmentId());
        //a fresh course has no students yet, so there is nothing to query for
        return courseMapper.toResponse(courseRepository.save(
                new OnlineCourse(request.name(), department, request.meetingUrl())), List.of());
    }

    @Override
    @Transactional
    public CourseResponse createOnsite(CreateOnsiteCourseRequest request) {
        Department department = requireDepartment(request.departmentId());
        return courseMapper.toResponse(courseRepository.save(
                new OnsiteCourse(request.name(), department, request.roomNumber())), List.of());
    }

    @Override
    @Transactional
    public CourseResponse update(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        course.setName(request.name()); // no save() flush() will save changes
        return courseMapper.toResponse(course, studentsOf(course, null));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        for (Student student : Set.copyOf(course.getStudents())) {
            student.withdraw(course);
        }

        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public CourseSoftDeleteResponse softDelete(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        course.setDeleted(true);
        return courseMapper.toResponseWithSoftDelete(course);

    }

    @Override
    @Transactional
    public CourseSoftDeleteResponse restoreSoftDelete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        course.setDeleted(false);
        return courseMapper.toResponseWithSoftDelete(course);

    }

    private Department requireDepartment(Long departmentId) {
        return departmentRepository.findWithLockById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));
    }

    //one query for the whole list instead of one per course
    private Map<Long, List<StudentSummaryResponse>> studentsByCourse(List<Course> courses, Boolean deleted) {
        List<Long> ids = courses.stream().map(Course::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        return StudentSummaryRow.groupByOwner(
                courseRepository.findStudentSummariesByCourseIds(ids, deleted));
    }

    private List<StudentSummaryResponse> studentsOf(Course course, Boolean deleted) {
        return studentsOf(studentsByCourse(List.of(course), deleted), course);
    }

    private List<StudentSummaryResponse> studentsOf(Map<Long, List<StudentSummaryResponse>> studentsByCourse,
        Course course) {

        return studentsByCourse.getOrDefault(course.getId(), List.of());
    }
}
