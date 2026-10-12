package org.chud.springuniapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@DiscriminatorValue("ONLINE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OnlineCourse extends Course {

    @Column(length = 300)
    private String meetingUrl;

    public OnlineCourse(String name, Department department, User lecturer, String meetingUrl) {
        super(name, department, lecturer);
        this.meetingUrl = meetingUrl;
    }
}
