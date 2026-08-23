package org.chud.springuniapi.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private Department department;
    private OnsiteCourse onsiteCourse;
    private User user;

    @BeforeEach
    void setup() {
        //Assign
        department = new Department("Informatics");
        onsiteCourse = new OnsiteCourse("Databases", department, 101L);
        user = new User("Ana", "ana@uni.bg");
    }

    @Nested
    @DisplayName("enroll tests")
    class Enroll {

        @Test
        @DisplayName("enroll registers the course on the user's side")
        void enrollAddsCourseToUser() {
            //Act
            user.enroll(onsiteCourse);

            //Assert
            assertThat(user.getCourses()).containsExactly(onsiteCourse);
        }

        @Test
        @DisplayName("enroll registers the user on the course's side")
        void enrollAddsUserToCourse() {
            //Act
            user.enroll(onsiteCourse);

            //Assert
            assertThat(onsiteCourse.getUsers()).containsExactly(user);
        }

        @Test
        @DisplayName("user can enroll in multiple courses")
        void userCanEnrollMultipleCourses() {
            OnlineCourse oop = new OnlineCourse("OOP", department, "meethingUrl");

            //Act
            user.enroll(onsiteCourse);
            user.enroll(oop);

            //Assert
            assertThat(user.getCourses()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("withdraw tests")
    class Withdraw {

        @Test
        @DisplayName("withdraw removes the course from the user")
        void withdrawRemovesCourseFromUser() {
            user.enroll(onsiteCourse);

            //Act
            user.withdraw(onsiteCourse);

            //Assert
            assertThat(user.getCourses()).isEmpty();
        }

        @Test
        @DisplayName("withdraw removes the user from the course")
        void withdrawRemovesUserFromCourse() {
            user.enroll(onsiteCourse);

            //Act
            user.withdraw(onsiteCourse);

            //Assert
            assertThat(onsiteCourse.getUsers()).isEmpty();
        }
    }

    @Test
    @DisplayName("new user doesnt have courses")
    void newUserHasNoCourses() {
        //Assign
        User ana = new User("Ana", "ana@uni.bg");

        //Assert
        assertThat(ana.getCourses()).isEmpty();

    }
}
