package org.chud.springuniapi.dto.request;

import jakarta.validation.constraints.NotNull;
import org.chud.springuniapi.enums.RoleName;

public record AssignRoleRequest(@NotNull RoleName role) { }