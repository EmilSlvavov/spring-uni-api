package org.chud.springuniapi.dto.response;

import org.chud.springuniapi.entity.Student;

import java.util.Comparator;
import java.util.List;

public record StudentResponse(Long id, String name, String email, List<CourseSummaryResponse> courses) {

    public StudentResponse {
        courses = courses == null ? List.of() : List.copyOf(courses);
    }

    public static StudentResponse from(Student student) {
        List<CourseSummaryResponse> courses = student.getCourses().stream()
                .map(CourseSummaryResponse::from)
                .sorted(Comparator.comparing(CourseSummaryResponse::id))
                .toList();

        return new StudentResponse(student.getId(), student.getName(), student.getEmail(), courses);
    }
}
