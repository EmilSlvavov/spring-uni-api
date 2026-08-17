package org.chud.springuniapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.chud.springuniapi.enums.CourseType;

//annotation means do not include null fields
// using it since course can be both online or onsite and one value will be null
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseResponse(Long id,
                             String name,
                             Long departmentId,
                             String departmentName,
                             CourseType type,
                             String meetingUrl,
                             Long roomNumber,
                             List<StudentSummaryResponse> students
                             ) {
    public CourseResponse {
        students = students == null ? List.of() : List.copyOf(students);
    }
}
