package org.chud.springuniapi.dto.response;

import java.util.List;

public record DepartmentResponse(Long id,
                                 String name,
                                 List<ContactInfoResponse> contacts,
                                 List<CourseSummaryResponse> courses) {

    //compact constructor (no args, taken implied from record header). Used to
    //make an empty list instead of having null value/ create unmodifiable copy
    // List of the contacts
    public DepartmentResponse{
        contacts = contacts == null ? List.of() : List.copyOf(contacts);
        courses = courses == null ? List.of() : List.copyOf(courses);
    }
}
