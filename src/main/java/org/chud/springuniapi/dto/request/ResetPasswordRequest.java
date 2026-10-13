package org.chud.springuniapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "new password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String newPassword
) {
}
