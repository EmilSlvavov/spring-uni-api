package org.chud.springuniapi.service;

import java.util.List;
import org.chud.springuniapi.dto.response.RoleResponse;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.RoleMapper;
import org.chud.springuniapi.repository.RoleRepository;
import org.chud.springuniapi.service.serviceInterface.IRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements IRoleService {

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
}
