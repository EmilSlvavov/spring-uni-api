package org.chud.springuniapi.repository.spec;

import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.OnlineCourse;
import org.chud.springuniapi.entity.OnsiteCourse;
import org.chud.springuniapi.enums.CourseType;
import org.springframework.data.jpa.domain.Specification;

public final class CourseSpecifications {

    private CourseSpecifications() {
    }

    public static Specification<Course> isActive() {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.isFalse(root.get("department").get("deleted")));
    }

    public static Specification<Course> nameContains(String q) {
        return (root, query, cb) -> q == null || q.isBlank()
                ? null
                : cb.like(cb.lower(root.get("name")), "%" + q.trim().toLowerCase() + "%");
    }

    public static Specification<Course> hasType(CourseType type) {
        return (root, query, cb) -> type == null
                ? null
                : cb.equal(root.type(), type == CourseType.ONLINE ? OnlineCourse.class : OnsiteCourse.class);
    }

    public static Specification<Course> inDepartment(Long departmentId) {
        return (root, query, cb) -> departmentId == null
                ? null
                : cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<Course> taughtBy(Long lecturerId) {
        return (root, query, cb) -> lecturerId == null
                ? null
                : cb.equal(root.get("lecturer").get("id"), lecturerId);
    }
}