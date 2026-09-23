package org.chud.springuniapi.application_events.event;

import lombok.Getter;

@Getter
public class EnrollUserEvent {
    private final Long UserId;
    private final Long CourseId;
    private final String courseName;

    public EnrollUserEvent(Long userId, Long courseId, String courseName) {
        UserId = userId;
        CourseId = courseId;
        this.courseName = courseName;
    }
}
