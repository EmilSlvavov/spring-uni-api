package org.chud.springuniapi.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudentTest {

    private Department department;
    private OnsiteCourse onsiteCourse;
    private Student student;

    @BeforeEach
    void setup() {
        //Assign
        department = new Department("Informatics");
        onsiteCourse = new OnsiteCourse("Databases", department, 101L);
        student = new Student("Ana", "ana@uni.bg");
    }

    @Nested
    @DisplayName("enroll tests")
    class Enroll {

        @Test
        @DisplayName("enroll registers the course on the student's side")
        void enrollAddsCourseToStudent() {
            //Act
            student.enroll(onsiteCourse);

            //Assert
            assertThat(student.getCourses()).containsExactly(onsiteCourse);
        }

        @Test
        @DisplayName("enroll registers the student on the course's side")
        void enrollAddsStudentToCourse() {
            //Act
            student.enroll(onsiteCourse);

            //Assert
            assertThat(onsiteCourse.getStudents()).containsExactly(student);
        }

        @Test
        @DisplayName("student can enroll in multiple courses")
        void studentCanEnrollMultipleCourses() {
            OnlineCourse oop = new OnlineCourse("OOP", department, "meethingUrl");

            //Act
            student.enroll(onsiteCourse);
            student.enroll(oop);

            //Assert
            assertThat(student.getCourses()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("withdraw tests")
    class Withdraw {

        @Test
        @DisplayName("withdraw removes the course from the student")
        void withdrawRemovesCourseFromStudent() {
            student.enroll(onsiteCourse);

            //Act
            student.withdraw(onsiteCourse);

            //Assert
            assertThat(student.getCourses()).isEmpty();
        }

        @Test
        @DisplayName("withdraw removes the student from the course")
        void withdrawRemovesStudentFromCourse() {
            student.enroll(onsiteCourse);

            //Act
            student.withdraw(onsiteCourse);

            //Assert
            assertThat(onsiteCourse.getStudents()).isEmpty();
        }
    }

    @Test
    @DisplayName("new student doesnt have courses")
    void newStudentHasNoCourses() {
        //Assign
        Student ana = new Student("Ana", "ana@uni.bg");

        //Assert
        assertThat(ana.getCourses()).isEmpty();

    }
}
