package org.chud.springuniapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.chud.springuniapi.enums.RoleName;

public record CreateUserRequest(

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @NotBlank(message = "email is required")
        @Email(message = "must be a valid email")
        @Size(max = 180, message = "email must be at most 180 characters")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters") //Bcrypt truncates input after 72 bytes
        String password,

        @NotNull
        RoleName role

) { }
