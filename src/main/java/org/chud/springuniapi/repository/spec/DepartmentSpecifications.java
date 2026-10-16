package org.chud.springuniapi.repository.spec;

import org.chud.springuniapi.entity.Department;
import org.springframework.data.jpa.domain.Specification;

public final class DepartmentSpecifications {

    private DepartmentSpecifications() { }

    public static Specification<Department> isActive() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Department> nameContains(String q) {
        return (root, query, cb) -> q == null || q.isBlank()
                ? null
                : cb.like(cb.lower(root.get("name")), "%" + q.trim().toLowerCase() + "%");
    }
}
