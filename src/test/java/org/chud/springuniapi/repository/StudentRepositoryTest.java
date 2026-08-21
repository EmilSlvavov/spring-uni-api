package org.chud.springuniapi.repository;

import org.chud.springuniapi.config.JpaAuditingConfig;
import org.chud.springuniapi.entity.*;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
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
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("save round trip")
    void saveRoundTrip() {
        Student student = new Student("Ana", "ana@uni.bg");
        studentRepository.saveAndFlush(student);
        Long id = student.getId();

        String studentName = student.getName();
        String studentEmail = student.getEmail();

        entityManager.flush();
        entityManager.clear();

        Student found = studentRepository.findById(id).orElseThrow();

        assertThat(found.getName()).isEqualTo(studentName);
        assertThat(found.getEmail()).isEqualTo(studentEmail);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save fails due to duplicate email")
    void saveStudentWithDuplicateEmail() {
        Student student = new Student("Ana", "ana@uni.bg");
        studentRepository.saveAndFlush(student);
        Student duplicateEmailStudent = new Student("Anna", "ana@uni.bg");

        assertThatThrownBy(() -> studentRepository.saveAndFlush(duplicateEmailStudent))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest(name = "existsByEmailIgnoreCase(\"{0}\") is true")
    @ValueSource(strings = {"ANA@UNI.BG", "Ana@Uni.Bg", "ana@uni.bg"})
    @DisplayName("existsByEmailIgnoreCase testing inputs")
    void checkExistsByEmailIgnoreCase(String email) {
        Student student = new Student("Ana", "ana@uni.bg");
        studentRepository.saveAndFlush(student);
        entityManager.clear();

        assertThat(studentRepository.existsByEmailIgnoreCase(email)).isTrue();
    }

    @Test
    @DisplayName("get student with courses with the 3 options for listing soft deleted courses")
    void checkGetStudentWithSoftDeletedCoursesOptions() {
        Student student = new Student("Ana", "ana@uni.bg");
        Department department = entityManager.persistAndFlush(new Department("department"));
        OnlineCourse onlineCourse = new OnlineCourse("onlineCourse", department, "url");
        OnsiteCourse onsiteCourse = new OnsiteCourse("onsiteCourse", department, 509L);

        onsiteCourse.setDeleted(true);
        entityManager.persistAndFlush(onlineCourse);
        entityManager.persistAndFlush(onsiteCourse);
        student.enroll(onlineCourse);
        student.enroll(onsiteCourse);
        studentRepository.saveAndFlush(student);

        entityManager.flush();
        entityManager.clear();

        Long studentId = student.getId();
        Long onlineId  = onlineCourse.getId();
        Long onsiteId  = onsiteCourse.getId();
        String onlineName = onlineCourse.getName();
        String onsiteName = onsiteCourse.getName();

        assertThat(studentRepository.findCourseSummariesByStudentIds(List.of(studentId), false))
                .containsExactly(new CourseSummaryRow(studentId, onlineId, onlineName));

        assertThat(studentRepository.findCourseSummariesByStudentIds(List.of(studentId), true))
                .containsExactly(new CourseSummaryRow(studentId, onsiteId, onsiteName));

        assertThat(studentRepository.findCourseSummariesByStudentIds(List.of(studentId), null))
                .containsExactlyInAnyOrder(
                        new CourseSummaryRow(studentId, onlineId, onlineName),
                        new CourseSummaryRow(studentId, onsiteId, onsiteName));
    }
}
