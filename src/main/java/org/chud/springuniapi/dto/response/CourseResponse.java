package org.chud.springuniapi.dto.response;

import org.chud.springuniapi.entity.Course;

public record CourseResponse(Long id, String name, Long departmentId, String departmentName) {

    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDepartment().getId(),
                course.getDepartment().getName()
        );
    }
}
