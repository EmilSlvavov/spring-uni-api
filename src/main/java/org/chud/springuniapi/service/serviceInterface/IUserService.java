package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.request.UpdateUserProfileRequest;
import org.chud.springuniapi.dto.request.UpdateUserRequest;
import org.chud.springuniapi.dto.response.UserDisplayResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.dto.response.UserSoftDeleteResponse;

import java.util.List;

//Single aggregate operations only. Anything that also needs a Course, a Role or the
//refresh token store moved to a facade in service.facade.
public interface IUserService {

    //enabled = null means the ?enabled param was absent, so the courses are not filtered
    List<UserResponse> findAll(Boolean deleted);

    UserResponse findById(Long id, Boolean deleted);

    List<UserDisplayResponse> findDisplayLabels();

    List<UserSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted);

    UserResponse update(Long id, UpdateUserRequest request);

    UserResponse updateProfile(Long id, UpdateUserProfileRequest request);

    void delete(Long id);

    UserSoftDeleteResponse softDelete(Long id);

    UserSoftDeleteResponse restoreSoftDelete(Long id);
}
