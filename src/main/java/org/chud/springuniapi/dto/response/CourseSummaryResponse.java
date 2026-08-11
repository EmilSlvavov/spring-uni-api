package org.chud.springuniapi.dto.response;

import org.chud.springuniapi.entity.Course;

public record CourseSummaryResponse(Long id, String name) {

    public static CourseSummaryResponse from(Course course) {
        return new CourseSummaryResponse(course.getId(), course.getName());
    }
}
