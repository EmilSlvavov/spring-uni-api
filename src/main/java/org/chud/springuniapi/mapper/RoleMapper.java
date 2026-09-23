package org.chud.springuniapi.mapper;

import org.chud.springuniapi.dto.response.RoleResponse;
import org.chud.springuniapi.entity.Role;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface RoleMapper {
    RoleResponse toResponse(Role role);
}
