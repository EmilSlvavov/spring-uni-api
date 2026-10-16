package org.chud.springuniapi.repository;

import jakarta.persistence.LockModeType;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    boolean existsByNameIgnoreCase(String name);


    //find with contacts by id
    @EntityGraph(attributePaths = "contacts")
    Optional<Department> findWithContactsById(Long id);

    @EntityGraph(attributePaths = "contacts")
    Optional<Department> findWithContactsByIdAndDeleted(Long id, boolean deleted);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Department> findWithLockById(Long id);

    Page<Department> findDepartmentByDeleted(boolean isDeleted, Pageable pageable);

    //Soft delete filter for the courses of a department. enabled = null means "do not filter"
    @Query("""
            select new org.chud.springuniapi.repository.projection.CourseSummaryRow(d.id, c.id, c.name)
            from Department d
            join d.courses c
            where d.id in :departmentIds
              and (:deleted is null or c.deleted = :deleted)
            order by d.id, c.id
            """)
    List<CourseSummaryRow> findCourseSummariesByDepartmentIds(Collection<Long> departmentIds, Boolean deleted);

    boolean existsByIdAndDeletedFalse(Long id);
}
