package org.chud.springuniapi.service;

import java.util.List;
import org.chud.springuniapi.dto.response.RoleResponse;
import org.chud.springuniapi.entity.Role;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.RoleMapper;
import org.chud.springuniapi.repository.RoleRepository;
import org.chud.springuniapi.service.serviceInterface.IRoleService;
import org.chud.springuniapi.service.serviceInterface.internal.IRoleServiceInternal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

//The only class allowed to touch RoleRepository.
@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements IRoleService, IRoleServiceInternal {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
            .map(roleMapper::toResponse).toList();
    }

    @Override
    public RoleResponse findById(Long id) {
        return roleRepository.findById(id)
            .map(roleMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Role ", id));
    }

    @Override
    public RoleResponse findByName(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
            .map(roleMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Role ", roleName.name()));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Role require(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Role ", roleName.name()));
    }
}
