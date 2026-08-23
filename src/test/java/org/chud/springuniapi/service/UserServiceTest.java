package org.chud.springuniapi.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.chud.springuniapi.application_events.event.EnrollUserEvent;
import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.entity.OnlineCourse;
import org.chud.springuniapi.entity.OnsiteCourse;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.UserMapper;
import org.chud.springuniapi.mapper.UserMapperImpl;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.UserRepository;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor ArgumentCaptor<EnrollUserEvent> eventCaptor;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository, courseRepository, userMapper, eventPublisher);
    }

    @Test
    @DisplayName("check if mappings are correct")
    void findByIdReturnsMappedResponse(){
        User user = new User("Ana", "ana@uni.bg");
        ReflectionTestUtils.setField(user, "id", 1L);

        //switched from refactoring to using findById instead of previous findWithCoursesById
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.findById(1L, false);

        //the real mapper ran, so these are its actual output
        assertThat(result.name()).isEqualTo(user.getName());
        assertThat(result.email()).isEqualTo(user.getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("findById throws when missing the entity")
    void findByIdThrowsWhenMissing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L, false))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("User with id 999 not found");

        verify(userMapper, never()).toResponse(any(), any());
    }

    @Test
    @DisplayName("happy path create")
    void createHappyPath() {

        CreateUserRequest request = new CreateUserRequest("Ana", "ana@uni.bg");

        User saved = new User("Ana", "ana@uni.bg");

        when(userRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(saved);

        UserResponse result = userService.create(request);

        //result is mapped from the saved entity, so that is what it should carry
        assertThat(result.name()).isEqualTo(saved.getName());
        assertThat(result.email()).isEqualTo(saved.getEmail());
        assertThat(result.courses()).isEmpty();

        verify(userRepository).saveAndFlush(userCaptor.capture());

        User captured = userCaptor.getValue();

        //the captured entity was built from the request, so that is what it should carry
        assertThat(captured.getName()).isEqualTo(request.name());
        assertThat(captured.getEmail()).isEqualTo(request.email());
    }

    @Test
    @DisplayName("email already exists")
    void createEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("Ana", "ana@uni.bg");

        when(userRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Email 'ana@uni.bg' is already registered");

        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("lost race when creating")
    void createLostRace() {
        CreateUserRequest request = new CreateUserRequest("Ana", "ana@uni.bg");

        when(userRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class)))
            .thenThrow(new DataIntegrityViolationException("Violation of UNIQUE KEY constraint 'UQ_users_email'"));

        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Email 'ana@uni.bg' is already registered");
    }

    @Test
    @DisplayName("delete happy path")
    void deleteHappyPath() {
        User user = new User("Ana", "ana@uni.bg");
        Department department = new Department("department");
        OnlineCourse onlineCourse = new OnlineCourse("onlineCourse", department, "url");
        OnsiteCourse onsiteCourse = new OnsiteCourse("onsiteCourse", department, 303L);

        user.enroll(onlineCourse);
        user.enroll(onsiteCourse);

        when(userRepository.findWithCoursesById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        assertThat(user.getCourses()).isEmpty();
        assertThat(onlineCourse.getUsers()).doesNotContain(user);
        assertThat(onsiteCourse.getUsers()).doesNotContain(user);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("user not found when delete")
    void deleteUserNotFound() {
        when(userRepository.findWithCoursesById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("User with id 1 not found");

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("enroll happy path")
    void enrollUserHappyPath() {
        User user = new User("Ana", "ana@uni.bg");
        ReflectionTestUtils.setField(user, "id", 1L);
        Department department = new Department("department");
        OnlineCourse onlineCourse = new OnlineCourse("onlineCourse", department, "url");

        when(userRepository.findWithCoursesById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findWithLockById(2L)).thenReturn(Optional.of(onlineCourse));
        when(userRepository.findCourseSummariesByUserIds(List.of(1L), null))
                .thenReturn(List.of(new CourseSummaryRow(1L, 2L, "onlineCourse")));

        UserResponse result = userService.enroll(1L,2L);

        assertThat(user.getCourses()).contains(onlineCourse);
        assertThat(onlineCourse.getUsers()).contains(user);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EnrollUserEvent event = eventCaptor.getValue();

        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getCourseId()).isEqualTo(2L);
        assertThat(event.getCourseName()).isEqualTo(onlineCourse.getName());

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.name()).isEqualTo(user.getName());
        assertThat(result.courses())
                .containsExactly(new CourseSummaryResponse(2L, onlineCourse.getName()));

    }

    @Test
    @DisplayName("enroll user not found")
    void enrollUserNotFound() {
        when(userRepository.findWithCoursesById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.enroll(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id 1 not found");

        verify(eventPublisher, never()).publishEvent(any(EnrollUserEvent.class));
    }

    @Test
    @DisplayName("enroll course not found")
    void enrollCourseNotFound() {
        User user = new User("Ana", "ana@uni.bg");

        when(userRepository.findWithCoursesById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findWithLockById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.enroll(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course with id 2 not found");

        verify(eventPublisher, never()).publishEvent(any(EnrollUserEvent.class));
    }
}
