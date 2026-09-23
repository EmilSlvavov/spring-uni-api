package org.chud.springuniapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.exception.BusinessRuleViolationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
//Secondary table, separate table -> one class; they both share primary key
//Always join on read, no lazy loading for a secondary table so lost time per
//read. Inserts and update go to both tables
@SecondaryTable(
        name = "user_profiles",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "user_id")
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity{


    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_courses",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();

    @Column(table = "user_profiles", length = 2000)
    private String bio;

    @Column(table = "user_profiles")
    private LocalDate dateOfBirth;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public void enroll(Course course) {
        if (isAdmin()) {
            throw new BusinessRuleViolationException("an ADMIN cannot be enrolled in courses");
        }
        courses.add(course);
        course.getUsers().add(this);
    }

    public void withdraw(Course course) {
        courses.remove(course);
        course.getUsers().remove(this);
    }

    public boolean isAdmin() {
        return this.role.getRoleName() == RoleName.ADMIN;
    }

}