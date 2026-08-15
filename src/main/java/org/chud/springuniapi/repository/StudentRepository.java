package org.chud.springuniapi.repository;

import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.repository.projection.StudentDisplayView;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "courses")
    Optional<Student> findWithCoursesById(Long id);


    @Override
    @NullMarked
    @EntityGraph(attributePaths = "courses")
    List<Student> findAll();

    //Open projection here due to the StudentDisplayView. Difference between
    //this and closed projection is that Spring loads the whole entity since
    //it does not know what you want from it.
    List<StudentDisplayView> findAllProjectedBy();

    @EntityGraph(attributePaths = "courses")
    List<Student> findStudentsByDeleted(boolean isDeleted);
}
