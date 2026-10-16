package org.chud.springuniapi.repository.spec;

import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.enums.RoleName;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    private UserSpecifications() { }

    public static Specification<User> matches(String q) {
        return (root, query, cb) -> q == null || q.isBlank()
                ? null
                :cb.or(cb.like(cb.lower(root.get("name")), "%" + q.trim().toLowerCase() + "%"),
                cb.like(cb.lower(root.get("email")), "%" + q.trim().toLowerCase() + "%"));
    }

    public static Specification<User> hasRole(RoleName role) {
        return (root, query, cb) -> role == null
                ? null
                : cb.equal((root.get("role").get("roleName")), role);
    }

}
