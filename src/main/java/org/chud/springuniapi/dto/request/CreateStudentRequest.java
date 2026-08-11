package org.chud.springuniapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStudentRequest(

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @NotBlank(message = "email is required")
        @Email(message = "must be a valid email")
        @Size(max = 180, message = "email must be at most 180 characters")
        String email

) { }
