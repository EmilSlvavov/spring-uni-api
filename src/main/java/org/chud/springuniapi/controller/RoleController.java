package org.chud.springuniapi.controller;

import java.util.List;
import org.chud.springuniapi.dto.response.RoleResponse;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.service.serviceInterface.IRoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final IRoleService roleService;

    public RoleController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponse> getAll() {
        return roleService.findAll();
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable Long id) {
        return roleService.findById(id);
    }

    @GetMapping("/name/{roleName}")
    public RoleResponse getByName(@PathVariable RoleName roleName) {
        return roleService.findByName(roleName);
    }
}
