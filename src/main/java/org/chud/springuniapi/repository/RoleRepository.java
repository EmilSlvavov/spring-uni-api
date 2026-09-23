package org.chud.springuniapi.repository;

import org.chud.springuniapi.entity.Role;
import org.chud.springuniapi.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(RoleName name);
}
