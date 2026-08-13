package org.chud.springuniapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
//Secondary table, separate table -> one class; they both share primary key
//Always join on read, no lazy loading for a secondary table so lost time per
//read. Inserts and update go to both tables
@SecondaryTable(
        name = "student_profiles",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "student_id")
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(table = "student_profiles", length = 2000)
    private String bio;

    @Column(table = "student_profiles")
    private LocalDate dateOfBirth;


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