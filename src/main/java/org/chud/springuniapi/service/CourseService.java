package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.entity.*;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.CourseMapper;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.DepartmentRepository;
import org.chud.springuniapi.repository.projection.CourseSummaryView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository, DepartmentRepository departmentRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.courseMapper = courseMapper;
    }

    public List<CourseResponse> findAll() {
        return courseRepository.findAllWithDepartment().stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    public CourseResponse findById(Long id) {
        return courseRepository.findByIdWithDepartment(id)
                .map(courseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    //Changed ordering to avoid passing existence check even if right after it somebody deletes it
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
    public List<CourseResponse> findByDepartment(Long departmentId) {

        List<Course> courses = courseRepository.findByDepartmentId(departmentId);

        if (courses.isEmpty() && !departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", departmentId);
        }

        return courses.stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional
    public CourseResponse createOnline(CreateOnlineCourseRequest request) {
        Department department = requireDepartment(request.departmentId());
        return courseMapper.toResponse(courseRepository.save(
                new OnlineCourse(request.name(), department, request.meetingUrl())));
    }

    @Transactional
    public CourseResponse createOnsite(CreateOnsiteCourseRequest request) {
        Department department = requireDepartment(request.departmentId());
        return courseMapper.toResponse(courseRepository.save(
                new OnsiteCourse(request.name(), department, request.roomNumber())));
    }

    @Transactional
    public CourseResponse update(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        course.setName(request.name()); // no save() flush() will save changes
        return courseMapper.toResponse(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        for (Student student : Set.copyOf(course.getStudents())) {
            student.withdraw(course);
        }

        courseRepository.delete(course);
    }

    private Department requireDepartment(Long departmentId) {
        return departmentRepository.findWithLockById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));
    }
}
