package org.chud.springuniapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
public class Student extends BaseEntity{


    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();


    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void enroll(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }

    public void withdraw(Course course) {
        courses.remove(course);
        course.getStudents().remove(this);
    }

}