package org.chud.springuniapi.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("ONSITE")
@Getter
@Setter
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
public class OnsiteCourse extends Course {

    private Long roomNumber;

    public OnsiteCourse(String name, Department department, User lecturer, Long roomNumber) {
        super(name, department, lecturer);
        this.roomNumber = roomNumber;
    }
}
