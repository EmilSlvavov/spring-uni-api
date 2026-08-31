package org.chud.springuniapi.service.facade;

import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.request.RegisterRequest;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.enums.RoleName;

public interface IUserAccountFacade {

    UserResponse create(CreateUserRequest request);

    UserResponse register(RegisterRequest request);

    UserResponse assignRole(Long id, RoleName roleName);
}
