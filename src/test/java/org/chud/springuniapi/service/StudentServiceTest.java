package org.chud.springuniapi.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.entity.OnlineCourse;
import org.chud.springuniapi.entity.OnsiteCourse;
import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.StudentMapper;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentService studentService;

    @Test
    @DisplayName("check if mappings are correct")
    void findByIdReturnsMappedResponse(){
        Student ana = new Student("Ana", "ana@uni.bg");

        StudentResponse expected = new StudentResponse(1L, "Ana", "ana@uni.bg", null, null, List.of());

        when(studentRepository.findWithCoursesById(1L)).thenReturn(Optional.of(ana));
        when(studentMapper.toResponse(ana, false)).thenReturn(expected);

        StudentResponse result = studentService.findById(1L, false);

        assertThat(result).isEqualTo(expected);
        verify(studentRepository).findWithCoursesById(1L);
    }

    @Test
    @DisplayName("findbyid throws when missing the entity")
    void findByIdThrowsWhenMissing() {
        when(studentRepository.findWithCoursesById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(999L, false))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Student with id 999 not found");

        verifyNoInteractions(studentMapper);
    }

    @Test
    @DisplayName("happy path create")
    void createHappyPath() {

        CreateStudentRequest request = new CreateStudentRequest("Ana", "ana@uni.bg");

        Student saved = new Student("Ana", "ana@uni.bg");

        StudentResponse expected = new StudentResponse(
            1L, "Ana", "ana@uni.bg", null, null, List.of());

        when(studentRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class))).thenReturn(saved);
        when(studentMapper.toResponse(saved)).thenReturn(expected);

        StudentResponse result = studentService.create(request);

        assertThat(result).isEqualTo(expected);
        verify(studentRepository).saveAndFlush((any(Student.class)));
    }

    @Test
    @DisplayName("email already exists")
    void createEmailAlreadyExists() {
        CreateStudentRequest request = new CreateStudentRequest("Ana", "ana@uni.bg");

        when(studentRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Email 'ana@uni.bg' is already registered");

        verifyNoInteractions(studentMapper);
    }

    @Test
    @DisplayName("lost race when creating")
    void createLostRace() {
        CreateStudentRequest request = new CreateStudentRequest("Ana", "ana@uni.bg");

        when(studentRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class)))
            .thenThrow(new DataIntegrityViolationException("Violation of UNIQUE KEY constraint 'UQ_students_email'"));

        assertThatThrownBy(() -> studentService.create(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Email 'ana@uni.bg' is already registered");
    }

    @Test
    @DisplayName("delete happy path")
    void deleteHappyPath() {
        Student student = new Student("Ana", "ana@uni.bg");
        Department department = new Department("department");
        OnlineCourse onlineCourse = new OnlineCourse("onlineCourse", department, "url");
        OnsiteCourse onsiteCourse = new OnsiteCourse("onsiteCourse", department, 303L);

        student.enroll(onlineCourse);
        student.enroll(onsiteCourse);

        when(studentRepository.findWithCoursesById(1L)).thenReturn(Optional.of(student));

        studentService.delete(1L);

        assertThat(student.getCourses()).isEmpty();
        assertThat(onlineCourse.getStudents()).doesNotContain(student);
        assertThat(onsiteCourse.getStudents()).doesNotContain(student);

        verify(studentRepository).delete(student);
    }

    @Test
    @DisplayName("student not found when delete")
    void deleteStudentNotFound() {
        when(studentRepository.findWithCoursesById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.delete(1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Student with id 1 not found");

        verify(studentRepository, never()).delete(any(Student.class));
    }
}
