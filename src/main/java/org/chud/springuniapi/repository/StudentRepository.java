package org.chud.springuniapi.repository;

import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.chud.springuniapi.repository.projection.StudentDisplayView;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "courses")
    Optional<Student> findWithCoursesById(Long id);

    //Open projection here due to the StudentDisplayView. Difference between
    //this and closed projection is that Spring loads the whole entity since
    //it does not know what you want from it.
    List<StudentDisplayView> findAllProjectedBy();

    List<Student> findStudentsByDeleted(boolean isDeleted);

    //The soft delete filter lives here instead of in the mapper. deleted = null
    //means "do not filter", which is what the ?deleted query param does when it is absent.
    @Query("""
            select new org.chud.springuniapi.repository.projection.CourseSummaryRow(s.id, c.id, c.name)
            from Student s
            join s.courses c
            where s.id in :studentIds
              and (:deleted is null or c.deleted = :deleted)
            order by s.id, c.id
            """)
    List<CourseSummaryRow> findCourseSummariesByStudentIds(Collection<Long> studentIds, Boolean deleted);
}
