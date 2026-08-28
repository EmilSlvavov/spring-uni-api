package org.chud.springuniapi.service.serviceInterface;

import java.util.List;
import org.chud.springuniapi.dto.response.RoleResponse;
import org.chud.springuniapi.enums.RoleName;

public interface IRoleService {
    List<RoleResponse> findAll();

    RoleResponse findById(Long id);

    RoleResponse findByName(RoleName roleName);

}
