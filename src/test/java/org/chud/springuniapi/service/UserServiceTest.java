package org.chud.springuniapi.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.entity.*;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.UserMapper;
import org.chud.springuniapi.mapper.UserMapperImpl;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

//Only the User aggregate is exercised here now. Enrolment moved to
//EnrollmentFacadeTest and role resolution to UserAccountFacadeTest, because those
//operations no longer live in this class.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
            userRepository,
            userMapper,
            passwordEncoder);
    }

    @Test
    @DisplayName("check if mappings are correct")
    void findByIdReturnsMappedResponse() {
        User user = new User(
            "Ana",
            "ana@uni.bg",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            new Role(RoleName.STUDENT));
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

        //Raw and encoded are deliberately DIFFERENT strings. If they were the same
        //value this test could not tell a hashed password from a stored plaintext one,
        //which is the single thing it most needs to prove.
        String rawPassword = "hunter2secret";
        String encodedPassword = "{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        //The Role now arrives resolved, the facade looked it up. It is still a
        //different instance from anything on the request, so isSameAs below can prove
        //the service persisted the one it was handed.
        Role managedRole = new Role(RoleName.STUDENT);

        CreateUserRequest request =
            new CreateUserRequest("Ana", "ana@uni.bg", rawPassword, RoleName.STUDENT);

        User saved = new User("Ana", "ana@uni.bg", encodedPassword, managedRole);

        when(userRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(saved);

        UserResponse result = userService.create(request, managedRole);

        //result is mapped from the saved entity, so that is what it should carry
        assertThat(result.name()).isEqualTo(saved.getName());
        assertThat(result.email()).isEqualTo(saved.getEmail());
        assertThat(result.courses()).isEmpty();

        verify(userRepository).saveAndFlush(userCaptor.capture());

        User captured = userCaptor.getValue();

        //the captured entity was built from the request, so that is what it should carry
        assertThat(captured.getName()).isEqualTo(request.name());
        assertThat(captured.getEmail()).isEqualTo(request.email());

        //The password that reaches the database must be the encoder's output.
        assertThat(captured.getPassword()).isEqualTo(encodedPassword);
        //And it must not be what the client typed.
        assertThat(captured.getPassword()).isNotEqualTo(rawPassword);

        //The persisted role must be the managed row the caller resolved.
        assertThat(captured.getRole()).isSameAs(managedRole);
    }

    @Test
    @DisplayName("email already exists")
    void createEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("Ana",
            "ana@uni.bg",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            RoleName.STUDENT);

        when(userRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request, new Role(RoleName.STUDENT)))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Email 'ana@uni.bg' is already registered");

        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("lost race when creating")
    void createLostRace() {
        CreateUserRequest request = new CreateUserRequest("Ana",
            "ana@uni.bg",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            RoleName.STUDENT);

        when(userRepository.existsByEmailIgnoreCase("ana@uni.bg")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class)))
            .thenReturn("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");

        when(userRepository.saveAndFlush(any(User.class)))
            .thenThrow(new DataIntegrityViolationException(
                "Violation of UNIQUE KEY constraint 'UQ_users_email'"));

        assertThatThrownBy(() -> userService.create(request, new Role(RoleName.STUDENT)))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Email 'ana@uni.bg' is already registered");
    }

    @Test
    @DisplayName("assignRole rejects promoting an enrolled user to ADMIN")
    void assignRoleRejectsEnrolledAdmin() {
        User user = new User(
            "Ana",
            "ana@uni.bg",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            new Role(RoleName.STUDENT));
        user.enroll(new OnlineCourse("onlineCourse", new Department("department"), "url"));

        when(userRepository.findWithCoursesById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.assignRole(1L, new Role(RoleName.ADMIN)))
            .isInstanceOf(org.chud.springuniapi.exception.BusinessRuleViolationException.class)
            .hasMessage("a user enrolled in courses cannot be promoted to ADMIN");

        //the role must not have been swapped before the rule ran
        assertThat(user.getRole().getRoleName()).isEqualTo(RoleName.STUDENT);
    }

    @Test
    @DisplayName("delete happy path")
    void deleteHappyPath() {
        User user = new User(
            "Ana",
            "ana@uni.bg",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            new Role(RoleName.STUDENT));
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
    @DisplayName("loadWithCourses throws when the user is missing")
    void loadWithCoursesThrowsWhenMissing() {
        when(userRepository.findWithCoursesById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadWithCourses(1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("User with id 1 not found");
    }

    //This is the assertion the old "enroll happy path" test used to make about the
    //response body. The mapping lives in describe() now, so it is checked here, and
    //EnrollmentFacadeTest is left to check the orchestration.
    @Test
    @DisplayName("describe flushes and maps the user with their current courses")
    void describeMapsCoursesAfterFlush() {
        User user = new User(
            "Ana",
            "ana@uni.bg",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            new Role(RoleName.STUDENT));
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findCourseSummariesByUserIds(List.of(1L), null))
            .thenReturn(List.of(new CourseSummaryRow(1L, 2L, "onlineCourse")));

        UserResponse result = userService.describe(user);

        //the join rows have to be on the wire before the summary query runs
        verify(userRepository).flush();

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.name()).isEqualTo(user.getName());
        assertThat(result.courses())
            .containsExactly(new CourseSummaryResponse(2L, "onlineCourse"));
    }
}
