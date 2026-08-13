package org.chud.springuniapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//specifically for online courses
public record CreateOnlineCourseRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name,

        @NotNull(message = "departmentId is required")
        Long departmentId,

        @NotBlank(message = "meetingUrl is required")
        @Size(max = 300, message = "meetingUrl must be maximum 300 characters")
        String meetingUrl
) {
}
