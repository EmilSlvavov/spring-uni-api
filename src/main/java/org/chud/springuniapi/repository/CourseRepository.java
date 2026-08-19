package org.chud.springuniapi.repository;

import jakarta.persistence.LockModeType;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.repository.projection.DynamicCourseProjectionMarker;
import org.chud.springuniapi.repository.projection.StudentSummaryRow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = "department")
    List<Course> findByDepartmentId(Long departmentId);

    @Query("select c from Course c join fetch c.department where c.id = :id")
    Optional<Course> findByIdWithDepartment(Long id);

    @Query("select c from Course c join fetch c.department")
    List<Course> findAllWithDepartment();

    //Can call it both with findByDepartmentId(1L, CourseSummaryView.class);
    //and also with findByDepartmentId(1L, Course.class); One returns the more
    //narrow proxy version with only department name(not managed) and the other
    // one gives you enteties which are being managed so
    //This is a dynamic projection since I can use both versions. Closed is if I
    //made the method take only CourseSumaryView like:
    // List<CourseSummaryView> find(by what I choose here)
    <T extends DynamicCourseProjectionMarker> List<T> findByDepartmentId(Long departmentId, Class<T> type);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Course> findWithLockById(Long id);

    List<Course> findCourseByDeleted(boolean isDeleted);

    //Soft delete filter for the students of a course. deleted = null means "do not filter"
    @Query("""
            select new org.chud.springuniapi.repository.projection.StudentSummaryRow(c.id, s.id, s.name)
            from Course c
            join c.students s
            where c.id in :courseIds
              and (:deleted is null or s.deleted = :deleted)
            order by c.id, s.id
            """)
    List<StudentSummaryRow> findStudentSummariesByCourseIds(Collection<Long> courseIds, Boolean deleted);
}
