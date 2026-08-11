package org.chud.springuniapi.repository;

import org.chud.springuniapi.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "courses")
    Optional<Student> findWithCoursesById(Long id);


    @Override
    @EntityGraph(attributePaths = "courses")
    List<Student> findAll();
}
