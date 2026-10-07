package org.chud.springuniapi.dto.response;

import org.chud.springuniapi.enums.RoleName;

public record MeResponse(Long id, String name, String email, RoleName role) { }
