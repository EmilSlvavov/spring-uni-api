package org.chud.springuniapi.dto.response;

import java.util.List;

public record DepartmentSoftDeleteResponse(Long id, String name, List<ContactInfoResponse> contacts, boolean isDeleted) {

    //compact constructor (no args, taken implied from record header). Used to
    //make an empty list instead of having null value/ create unmodifiable copy
    // List of the contacts
    public DepartmentSoftDeleteResponse{
        contacts = contacts == null ? List.of() : List.copyOf(contacts);
    }
}
