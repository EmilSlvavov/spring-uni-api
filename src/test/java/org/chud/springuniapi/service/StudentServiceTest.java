package org.chud.springuniapi.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.chud.springuniapi.application_events.event.EnrollStudentEvent;
import org.chud.springuniapi.application_events.listener.EnrollListener;
import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.entity.OnlineCourse;
import org.chud.springuniapi.entity.OnsiteCourse;
import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.StudentMapper;
import org.chud.springuniapi.mapper.StudentMapperImpl;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.StudentRepository;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Spy
    private StudentMapper studentMapper = new StudentMapperImpl();

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<Student> studentCaptor;

    @Captor ArgumentCaptor<EnrollStudentEvent> eventCaptor;

    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl(
                studentRepository, courseRepository, studentMapper, eventPublisher);
    }

    @Test
    @DisplayName("check if mappings are correct")
    void findByIdReturnsMappedResponse(){
        Student student = new Student("Ana", "ana@uni.bg");
        ReflectionTestUtils.setField(student, "id", 1L);

        //switched from refactoring to using findById instead of previous findWithCoursesById
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentResponse result = studentService.findById(1L, false);

        //the real mapper ran, so these are its actual output
        assertThat(result.name()).isEqualTo("Ana");
        assertThat(result.email()).isEqualTo("ana@uni.bg");
        verify(studentRepository).findById(1L);
    }

    @Test
    @DisplayName("findById throws when missing the entity")
    void findByIdThrowsWhenMissing() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(999L, false))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Student with id 999 not found");

        verify(studentMapper, never()).toResponse(any(), any());
    }

    @Test
    @DisplayName("happy path create")
    void createHappyPath() {

        CreateStudentRequest request = new CreateStudentRequest("Ana", "ana@uni.bg");

        Student saved = new Student("Ana", "ana@uni.bg");

        when(studentRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class))).thenReturn(saved);

        StudentResponse result = studentService.create(request);

        //create passes List.of() for courses, so the real mapper has an empty list
        assertThat(result.name()).isEqualTo("Ana");
        assertThat(result.email()).isEqualTo("ana@uni.bg");
        assertThat(result.courses()).isEmpty();

        verify(studentRepository).saveAndFlush(studentCaptor.capture());

        Student captured = studentCaptor.getValue();

        assertThat(captured.getName()).isEqualTo("Ana");
        assertThat(captured.getEmail()).isEqualTo("ana@uni.bg");
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

    @Test
    @DisplayName("enroll happy path")
    void enrollStudentHappyPath() {
        Student student = new Student("Ana", "ana@uni.bg");
        ReflectionTestUtils.setField(student, "id", 1L);
        Department department = new Department("department");
        OnlineCourse onlineCourse = new OnlineCourse("onlineCourse", department, "url");

        when(studentRepository.findWithCoursesById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findWithLockById(2L)).thenReturn(Optional.of(onlineCourse));
        when(studentRepository.findCourseSummariesByStudentIds(List.of(1L), null))
                .thenReturn(List.of(new CourseSummaryRow(1L, 2L, "onlineCourse")));

        StudentResponse result = studentService.enroll(1L,2L);

        assertThat(student.getCourses()).contains(onlineCourse);
        assertThat(onlineCourse.getStudents()).contains(student);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EnrollStudentEvent event = eventCaptor.getValue();

        assertThat(event.getStudentId()).isEqualTo(1L);
        assertThat(event.getCourseId()).isEqualTo(2L);
        assertThat(event.getCourseName()).isEqualTo("onlineCourse");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Ana");
        assertThat(result.courses())
                .containsExactly(new CourseSummaryResponse(2L, "onlineCourse"));

    }

    @Test
    @DisplayName("enroll student not found")
    void enrollStudentNotFound() {
        when(studentRepository.findWithCoursesById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.enroll(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student with id 1 not found");

        verify(eventPublisher, never()).publishEvent(any(EnrollStudentEvent.class));
    }

    @Test
    @DisplayName("enroll course not found")
    void enrollCourseNotFound() {
        Student student = new Student("Ana", "ana@uni.bg");

        when(studentRepository.findWithCoursesById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findWithLockById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.enroll(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course with id 2 not found");

        verify(eventPublisher, never()).publishEvent(any(EnrollStudentEvent.class));
    }
}
