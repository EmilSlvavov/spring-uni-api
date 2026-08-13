package org.chud.springuniapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.chud.springuniapi.enums.ContactType;

//used for the replace contact request dto
public record ContactInfoRequest(
        @NotNull(message = "type is required") ContactType type,
        @NotBlank(message = "value is required") @Size(max = 200) String value
) { }
