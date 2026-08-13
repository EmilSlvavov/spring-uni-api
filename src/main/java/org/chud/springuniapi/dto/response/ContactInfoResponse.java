package org.chud.springuniapi.dto.response;

import org.chud.springuniapi.enums.ContactType;


//Response-read side of ContactInfoRequest
public record ContactInfoResponse(
        ContactType type,
        String value
) { }
