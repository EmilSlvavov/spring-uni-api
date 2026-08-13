package org.chud.springuniapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

//Again annotation just for not returning null fields
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StudentResponse(
        Long id,
        String name,
        String email,
        String bio,
        LocalDate dateOfBirth,
        List<CourseSummaryResponse> courses
) {

    //compact constructor again to make sure we cant modify the list we get
    public StudentResponse {
        courses = courses == null ? List.of() : List.copyOf(courses);
    }
}