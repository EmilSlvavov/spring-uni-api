package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.request.RegisterRequest;
import org.chud.springuniapi.dto.request.UpdateUserProfileRequest;
import org.chud.springuniapi.dto.request.UpdateUserRequest;
import org.chud.springuniapi.dto.response.UserDisplayResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.dto.response.UserSoftDeleteResponse;

import java.util.List;
import org.chud.springuniapi.enums.RoleName;

public interface IUserService {

    //enabled = null means the ?enabled param was absent, so the courses are not filtered
    List<UserResponse> findAll(Boolean deleted);

    UserResponse findById(Long id, Boolean deleted);

    List<UserDisplayResponse> findDisplayLabels();

    List<UserSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted);

    UserResponse create(CreateUserRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

    UserResponse updateProfile(Long id, UpdateUserProfileRequest request);

    UserResponse enroll(Long userId, Long courseId);

    UserResponse withdraw(Long userId, Long courseId);

    UserResponse register(RegisterRequest request);

    void delete(Long id);

    UserSoftDeleteResponse softDelete(Long id);

    UserSoftDeleteResponse restoreSoftDelete(Long id);

    UserResponse assignRole(Long id, RoleName roleName);
}
