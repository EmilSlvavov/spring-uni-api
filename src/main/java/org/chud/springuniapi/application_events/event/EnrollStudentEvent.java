package org.chud.springuniapi.application_events.event;

import lombok.Getter;

@Getter
public class EnrollStudentEvent {
    private final Long StudentId;
    private final Long CourseId;
    private final String courseName;

    public EnrollStudentEvent(Long studentId, Long courseId, String courseName) {
        StudentId = studentId;
        CourseId = courseId;
        this.courseName = courseName;
    }
}
