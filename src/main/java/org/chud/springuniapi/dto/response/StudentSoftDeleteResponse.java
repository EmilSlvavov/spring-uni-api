package org.chud.springuniapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StudentSoftDeleteResponse(
    Long id,
    String name,
    String email,
    String bio,
    LocalDate dateOfBirth,
    List<CourseSummaryResponse> courses,
    boolean isDeleted
) {

    //compact constructor again to make sure we cant modify the list we get
    public StudentSoftDeleteResponse {
        courses = courses == null ? List.of() : List.copyOf(courses);
    }
}