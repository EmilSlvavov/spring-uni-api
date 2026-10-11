package org.chud.springuniapi.repository;

import jakarta.persistence.LockModeType;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.repository.projection.DynamicCourseProjectionMarker;
import org.chud.springuniapi.repository.projection.UserSummaryRow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("select c from Course c join fetch c.department where c.id = :id")
    Optional<Course> findByIdWithDepartment(Long id);


    <T extends DynamicCourseProjectionMarker> List<T> findByDepartmentIdAndDeletedFalseAndDepartmentDeletedFalse(
            Long departmentId, Class<T> type);

    @EntityGraph(attributePaths = "department")
    List<Course> findByDepartmentIdAndDeletedFalseAndDepartmentDeletedFalse(Long departmentId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Course> findWithLockById(Long id);

    List<Course> findCourseByDeleted(boolean isDeleted);

    //Soft delete filter for the users of a course. enabled = null means "do not filter"

    @Query("""
            select new org.chud.springuniapi.repository.projection.UserSummaryRow(c.id, s.id, s.name)
            from Course c
            join c.users s
            where c.id in :courseIds
              and (:deleted is null or s.deleted = :deleted)
            order by c.id, s.id
            """)
    List<UserSummaryRow> findUserSummariesByCourseIds(Collection<Long> courseIds, Boolean deleted);

    @Query("select c from Course c join fetch c.department d where c.deleted = false and d.deleted = false")
    List<Course> findAllActiveWithDepartment();

    @Query("select c from Course c join fetch c.department d where c.id = :id and c.deleted = false and d.deleted = false")
    Optional<Course> findActiveByIdWithDepartment(Long id);

}


