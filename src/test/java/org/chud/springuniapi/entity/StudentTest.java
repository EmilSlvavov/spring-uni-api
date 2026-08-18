package org.chud.springuniapi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StudentTest {

    @Test
    @DisplayName("enroll registers the course on the student's side")
    void enrollAddsCourseToStudent() {
        //Assign
        Department informatics = new Department("Informatics");
        OnsiteCourse databases = new OnsiteCourse("Databases", informatics, 101L);
        Student ana = new Student("Ana", "ana@uni.bg");

        //Act
        ana.enroll(databases);

        //Assert
        assertThat(ana.getCourses()).containsExactly(databases);
    }

    @Test
    @DisplayName("enroll registers the student on the course's side")
    void enrollAddsStudentToCourse() {
        //Assign
        Department informatics = new Department("Informatics");
        OnsiteCourse databases = new OnsiteCourse("Databases", informatics, 101L);
        Student ana = new Student("Ana", "ana@uni.bg");

        //Act
        ana.enroll(databases);

        //Assert
        assertThat(databases.getStudents()).containsExactly(ana);
    }

    @Test
    @DisplayName("student can enroll in multiple courses")
    void studentCanEnrollMultipleCourses() {
        //Assign
        Department informatics = new Department("Informatics");
        OnsiteCourse databases = new OnsiteCourse("Databases", informatics, 101L);
        OnlineCourse oop = new OnlineCourse("OOP", informatics, "meethingUrl");
        Student ana = new Student("Ana", "ana@uni.bg");

        //Act
        ana.enroll(databases);
        ana.enroll(oop);

        //Assert
        assertThat(ana.getCourses()).hasSize(2);
    }

    @Test
    @DisplayName("withdraw removes the course from the student")
    void withdrawRemovesCourseFromStudent() {
        //Assign
        Department informatics = new Department("Informatics");
        OnsiteCourse databases = new OnsiteCourse("Databases", informatics, 101L);
        Student ana = new Student("Ana", "ana@uni.bg");

        ana.enroll(databases);

        //Act

        ana.withdraw(databases);

        //Assert
        assertThat(ana.getCourses()).isEmpty();
    }

    @Test
    @DisplayName("withdraw removes the student from the course")
    void withdrawRemovesStudentFromCourse() {
        //Assign
        Department informatics = new Department("Informatics");
        OnsiteCourse databases = new OnsiteCourse("Databases", informatics, 101L);
        Student ana = new Student("Ana", "ana@uni.bg");

        ana.enroll(databases);

        //Act

        ana.withdraw(databases);

        //Assert
        assertThat(databases.getStudents()).isEmpty();
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
