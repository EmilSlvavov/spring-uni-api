package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateCourseRequest;
import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    public CourseService(CourseRepository courseRepository, DepartmentRepository departmentRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<CourseResponse> findAll() {
        return courseRepository.findAllWithDepartment().stream()
                .map(CourseResponse::from)
                .toList();
    }

    public CourseResponse findById(Long id) {
        return courseRepository.findByIdWithDepartment(id)
                .map(CourseResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    public List<CourseResponse> findByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", departmentId);
        }

        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));

        Course saved = courseRepository.save(new Course(request.name(), department));
        return CourseResponse.from(saved);
    }

    @Transactional
    public CourseResponse update(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        course.setName(request.name()); // no save() flush() will save changes
        return CourseResponse.from(course);
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
}
