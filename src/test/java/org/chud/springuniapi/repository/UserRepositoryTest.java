package org.chud.springuniapi.repository;

import org.chud.springuniapi.config.JpaAuditingConfig;
import org.chud.springuniapi.entity.*;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    Role role;

    @BeforeEach
    void setUp() {
        role = entityManager.persistAndFlush(new Role(RoleName.STUDENT));
    }

    @Test
    @DisplayName("save round trip")
    void saveRoundTrip() {
        User user = new User(
                "Ana",
                "ana@uni.bg",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                role);
        userRepository.saveAndFlush(user);
        Long id = user.getId();

        String userName = user.getName();
        String userEmail = user.getEmail();

        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(id).orElseThrow();

        assertThat(found.getName()).isEqualTo(userName);
        assertThat(found.getEmail()).isEqualTo(userEmail);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save fails due to duplicate email")
    void saveUserWithDuplicateEmail() {
        User user = new User(
                "Ana",
                "ana@uni.bg",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                role);
        userRepository.saveAndFlush(user);
        User duplicateEmailUser = new User(
                "Anna",
                "ana@uni.bg",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                role);

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateEmailUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest(name = "existsByEmailIgnoreCase(\"{0}\") is true")
    @ValueSource(strings = {"ANA@UNI.BG", "Ana@Uni.Bg", "ana@uni.bg"})
    @DisplayName("existsByEmailIgnoreCase testing inputs")
    void checkExistsByEmailIgnoreCase(String email) {
        User user = new User(
                "Ana",
                "ana@uni.bg",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                role);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThat(userRepository.existsByEmailIgnoreCase(email)).isTrue();
    }

    @Test
    @DisplayName("get user with courses with the 3 options for listing soft enabled courses")
    void checkGetUserWithSoftDeletedCoursesOptions() {
        User user = new User(
                "Ana",
                "ana@uni.bg",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                role);
        Department department = entityManager.persistAndFlush(new Department("department"));
        OnlineCourse onlineCourse = new OnlineCourse("onlineCourse", department, "url");
        OnsiteCourse onsiteCourse = new OnsiteCourse("onsiteCourse", department, 509L);

        onsiteCourse.setDeleted(true);
        entityManager.persistAndFlush(onlineCourse);
        entityManager.persistAndFlush(onsiteCourse);
        user.enroll(onlineCourse);
        user.enroll(onsiteCourse);
        userRepository.saveAndFlush(user);

        entityManager.flush();
        entityManager.clear();

        Long userId = user.getId();
        Long onlineId  = onlineCourse.getId();
        Long onsiteId  = onsiteCourse.getId();
        String onlineName = onlineCourse.getName();
        String onsiteName = onsiteCourse.getName();

        assertThat(userRepository.findCourseSummariesByUserIds(List.of(userId), false))
                .containsExactly(new CourseSummaryRow(userId, onlineId, onlineName));

        assertThat(userRepository.findCourseSummariesByUserIds(List.of(userId), true))
                .containsExactly(new CourseSummaryRow(userId, onsiteId, onsiteName));

        assertThat(userRepository.findCourseSummariesByUserIds(List.of(userId), null))
                .containsExactlyInAnyOrder(
                        new CourseSummaryRow(userId, onlineId, onlineName),
                        new CourseSummaryRow(userId, onsiteId, onsiteName));
    }
}
