package org.chud.springuniapi.service.facade;

import org.chud.springuniapi.dto.request.ChangePasswordRequest;
import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.request.RegisterRequest;
import org.chud.springuniapi.dto.request.ResetPasswordRequest;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.enums.RoleName;

public interface IUserAccountFacade {

    UserResponse create(CreateUserRequest request);

    UserResponse register(RegisterRequest request);

    UserResponse assignRole(Long id, RoleName roleName);

    void changePassword(Long id, ChangePasswordRequest request);

    void resetPassword(Long id, ResetPasswordRequest request);

    void delete(Long id);
}
