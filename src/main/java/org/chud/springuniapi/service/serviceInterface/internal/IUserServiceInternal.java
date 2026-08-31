package org.chud.springuniapi.service.serviceInterface.internal;

import java.util.Optional;
import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.entity.Role;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.enums.RoleName;


public interface IUserServiceInternal {


    User loadWithCourses(Long id);

    Optional<User> loadById(Long id);

    Optional<User> loadByEmail(String email);

    Optional<RoleName> roleOf(Long id);

    UserResponse describe(User user);

    UserResponse create(CreateUserRequest request, Role role);

    UserResponse assignRole(Long id, Role role);
}
