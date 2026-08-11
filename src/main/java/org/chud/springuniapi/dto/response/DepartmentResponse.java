package org.chud.springuniapi.dto.response;

import org.chud.springuniapi.entity.Department;

public record DepartmentResponse(Long id, String name) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName());
    }
}
