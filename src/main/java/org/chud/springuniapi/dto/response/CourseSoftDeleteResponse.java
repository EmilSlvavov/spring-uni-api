package org.chud.springuniapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.chud.springuniapi.enums.CourseType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseSoftDeleteResponse(Long id,
                                       String name,
                                       Long departmentId,
                                       String departmentName,
                                       CourseType type,
                                       String meetingUrl,
                                       Long roomNumber,
                                       boolean isDeleted
) {
}
