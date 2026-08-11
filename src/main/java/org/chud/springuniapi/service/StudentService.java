package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.request.UpdateStudentRequest;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::from)
                .toList();
    }

    public StudentResponse findById(Long id) {
        Student student = studentRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        if (studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException(
                    "Email '%s' is already registered".formatted(request.email()));
        }

        Student saved = studentRepository.save(new Student(request.name(), request.email()));
        return StudentResponse.from(saved);
    }

    @Transactional
    public StudentResponse update(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setName(request.name()); // no save() flush() will save changes
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse enroll(Long studentId, Long courseId) {
        Student student = studentRepository.findWithCoursesById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        student.enroll(course); // again dirty checking saves it
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse withdraw(Long studentId, Long courseId) {
        Student student = studentRepository.findWithCoursesById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        student.withdraw(course); // again dirty checking saves it
        return StudentResponse.from(student);
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
}
