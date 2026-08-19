package org.chud.springuniapi.repository;

import jakarta.persistence.LockModeType;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByNameIgnoreCase(String name);


    //find with contacts
    @Override
    @EntityGraph(attributePaths = "contacts")
    List<Department> findAll();

    //find with contacts by id
    @EntityGraph(attributePaths = "contacts")
    Optional<Department> findWithContactsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Department> findWithLockById(Long id);

    @EntityGraph(attributePaths = "contacts")
    List<Department> findDepartmentByDeleted(boolean isDeleted);

    //Soft delete filter for the courses of a department. deleted = null means "do not filter"
    @Query("""
            select new org.chud.springuniapi.repository.projection.CourseSummaryRow(d.id, c.id, c.name)
            from Department d
            join d.courses c
            where d.id in :departmentIds
              and (:deleted is null or c.deleted = :deleted)
            order by d.id, c.id
            """)
    List<CourseSummaryRow> findCourseSummariesByDepartmentIds(Collection<Long> departmentIds, Boolean deleted);
}
