package org.chud.springuniapi.service.facade;

import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.request.RegisterRequest;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.entity.Role;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.service.serviceInterface.IRefreshTokenService;
import org.chud.springuniapi.service.serviceInterface.internal.IRoleServiceInternal;
import org.chud.springuniapi.service.serviceInterface.internal.IUserServiceInternal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//create and assignRole needed a managed Role.
// Separately, assignRole had to revoke refresh tokens
@Service
public class UserAccountFacadeImpl implements IUserAccountFacade {

    private final IUserServiceInternal userService;
    private final IRoleServiceInternal roleService;
    private final IRefreshTokenService refreshTokenService;

    public UserAccountFacadeImpl(
        IUserServiceInternal userService,
        IRoleServiceInternal roleService,
        IRefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.roleService = roleService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        Role role = roleService.require(request.role());
        return userService.create(request, role);
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        return create(new CreateUserRequest(
            request.name(),
            request.email(),
            request.password(),
            RoleName.STUDENT));
    }

    @Override
    @Transactional
    public UserResponse assignRole(Long id, RoleName roleName) {
        Role role = roleService.require(roleName);
        UserResponse response = userService.assignRole(id, role);

        //we do this so the jwts with the old role dont
        // keep getting renewed by the refresh token
        refreshTokenService.revokeAllFor(id);

        return response;
    }
}
