package org.chud.springuniapi.repository;

import jakarta.persistence.LockModeType;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.repository.projection.DynamicCourseProjectionMarker;
import org.chud.springuniapi.repository.projection.UserSummaryRow;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    @EntityGraph(attributePaths = {"department", "lecturer"})
    Page<Course> findAll(@NonNull Specification<Course> spec, @NonNull Pageable pageable);

    @Query("select c from Course c join fetch c.department where c.id = :id")
    Optional<Course> findByIdWithDepartment(Long id);


    <T extends DynamicCourseProjectionMarker> List<T> findByDepartmentIdAndDeletedFalseAndDepartmentDeletedFalse(
            Long departmentId, Class<T> type);


    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Course> findWithLockById(Long id);

    boolean existsByIdAndLecturerId(Long id, Long lecturerId);

    @Query("""
            select c from Course c
            join fetch c.department d
            left join fetch c.lecturer l
            where (:deleted is null or c.deleted = :deleted)
              and (:lecturerId is null or l.id = :lecturerId)
            order by c.id
            """)
    List<Course> findForSoftDeleteList(Boolean deleted, Long lecturerId);

    @Query("""
            select new org.chud.springuniapi.repository.projection.UserSummaryRow(c.id, s.id, s.name)
            from Course c
            join c.users s
            where c.id in :courseIds
              and (:deleted is null or s.deleted = :deleted)
            order by c.id, s.id
            """)
    List<UserSummaryRow> findUserSummariesByCourseIds(Collection<Long> courseIds, Boolean deleted);


    @Query("select c from Course c join fetch c.department d where c.id = :id and c.deleted = false and d.deleted = false")
    Optional<Course> findActiveByIdWithDepartment(Long id);

}


