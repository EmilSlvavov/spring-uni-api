package org.chud.springuniapi.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StudentTest {

    private Department informatics;
    private OnsiteCourse databases;
    private Student ana;

    @BeforeEach
    void setup() {
        //Assign
        informatics = new Department("Informatics");
        databases = new OnsiteCourse("Databases", informatics, 101L);
        ana = new Student("Ana", "ana@uni.bg");
    }

    @Nested
    @DisplayName("enroll tests")
    class Enroll {

        @Test
        @DisplayName("enroll registers the course on the student's side")
        void enrollAddsCourseToStudent() {
            //Act
            ana.enroll(databases);

            //Assert
            assertThat(ana.getCourses()).containsExactly(databases);
        }

        @Test
        @DisplayName("enroll registers the student on the course's side")
        void enrollAddsStudentToCourse() {
            //Act
            ana.enroll(databases);

            //Assert
            assertThat(databases.getStudents()).containsExactly(ana);
        }

        @Test
        @DisplayName("student can enroll in multiple courses")
        void studentCanEnrollMultipleCourses() {
            OnlineCourse oop = new OnlineCourse("OOP", informatics, "meethingUrl");

            //Act
            ana.enroll(databases);
            ana.enroll(oop);

            //Assert
            assertThat(ana.getCourses()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("withdraw tests")
    class Withdraw {

        @Test
        @DisplayName("withdraw removes the course from the student")
        void withdrawRemovesCourseFromStudent() {
            ana.enroll(databases);

            //Act

            ana.withdraw(databases);

            //Assert
            assertThat(ana.getCourses()).isEmpty();
        }

        @Test
        @DisplayName("withdraw removes the student from the course")
        void withdrawRemovesStudentFromCourse() {
            ana.enroll(databases);

            //Act

            ana.withdraw(databases);

            //Assert
            assertThat(databases.getStudents()).isEmpty();
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
