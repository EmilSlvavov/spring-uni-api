package org.chud.springuniapi.repository;

import jakarta.persistence.LockModeType;
import org.chud.springuniapi.entity.Department;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

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
}
