package org.chud.springuniapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends BaseEntity{


    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Course> courses = new HashSet<>();


    //Separate table, connected by foreign key on id
    //Used mainly for small and few values
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "department_contacts",
            joinColumns = @JoinColumn(name = "department_id")
    )
    private Set<ContactInfo> contacts = new HashSet<>();


    public Department(String name) {
        this.name = name;
    }

}