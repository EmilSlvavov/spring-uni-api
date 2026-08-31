package org.chud.springuniapi.service.serviceInterface.internal;

import org.chud.springuniapi.entity.Role;
import org.chud.springuniapi.enums.RoleName;

public interface IRoleServiceInternal {

    Role require(RoleName roleName);
}
