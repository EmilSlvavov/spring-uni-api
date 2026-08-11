package org.chud.springuniapi.repository;

import org.chud.springuniapi.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartmentId(Long departmentId);

    @Query("select c from Course c join fetch c.department where c.id = :id")
    Optional<Course> findByIdWithDepartment(Long id);

    @Query("select c from Course c join fetch c.department")
    List<Course> findAllWithDepartment();
}
