package org.chud.springuniapi.dto.response;

import org.chud.springuniapi.enums.RoleName;

public record RoleResponse(
    Long id,
    RoleName roleName
) { }
