package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateDepartmentRequest;
import org.chud.springuniapi.dto.request.UpdateDepartmentRequest;
import org.chud.springuniapi.dto.response.DepartmentResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public DepartmentResponse findById(Long id) {
        return departmentRepository.findById(id)
                .map(DepartmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException(
                    "Department '%s' already exists".formatted(request.name()));
        }

        Department saved = departmentRepository.save(new Department(request.name()));
        return DepartmentResponse.from(saved);
    }

    @Transactional
    public DepartmentResponse update(Long id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        boolean renamingToADifferentName = !department.getName().equalsIgnoreCase(request.name());
        if (renamingToADifferentName && departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException(
                    "Department '%s' already exists".formatted(request.name()));
        }

        department.setName(request.name()); // no save() flush() will save changes
        return DepartmentResponse.from(department);
    }

    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        for (Course course : department.getCourses()) {
            for (Student student : Set.copyOf(course.getStudents())) {
                student.withdraw(course);
            }
        }

        departmentRepository.delete(department);
    }
}
