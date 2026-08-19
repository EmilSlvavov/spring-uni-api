package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateDepartmentRequest;
import org.chud.springuniapi.dto.request.ReplaceContactsRequest;
import org.chud.springuniapi.dto.request.UpdateDepartmentRequest;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.DepartmentResponse;
import org.chud.springuniapi.dto.response.DepartmentSoftDeleteResponse;
import org.chud.springuniapi.entity.ContactInfo;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.entity.Student;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.DepartmentMapper;
import org.chud.springuniapi.repository.DepartmentRepository;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository,
        DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    public List<DepartmentResponse> findAll(Boolean deleted) {
        List<Department> departments = departmentRepository.findAll();
        Map<Long, List<CourseSummaryResponse>> coursesByDepartment = coursesByDepartment(departments, deleted);

        return departments.stream()
            .map(department -> departmentMapper.toResponse(department,
                coursesOf(coursesByDepartment, department)))
            .toList();
    }

    public DepartmentResponse findById(Long id, Boolean deleted) {
        Department department = departmentRepository.findWithContactsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        return departmentMapper.toResponse(department, coursesOf(department, deleted));
    }

    public List<DepartmentSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted) {
        return departmentRepository
            .findDepartmentByDeleted(isDeleted)
            .stream()
            .map(departmentMapper::toResponseWithSoftDelete)
            .toList();
    }

    //changed so it checks if it won the race condition and throws if not
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException(
                "Department '%s' already exists".formatted(request.name()));
        }

        Department saved;

        try {
            saved = departmentRepository.saveAndFlush(new Department(request.name()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException(
                "Department '%s' already exists".formatted(request.name())
            );
        }

        //a fresh department has no courses yet, so there is nothing to query for
        return departmentMapper.toResponse(saved, List.of());
    }

    @Transactional
    public DepartmentResponse update(Long id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        boolean renamingToADifferentName = !department.getName().equalsIgnoreCase(request.name());
        if (renamingToADifferentName && departmentRepository.existsByNameIgnoreCase(
            request.name())) {
            throw new DuplicateResourceException(
                "Department '%s' already exists".formatted(request.name()));
        }

        department.setName(request.name());
        try {
            departmentRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException(
                "Department '%s' already exists".formatted(request.name()));
        }
        return departmentMapper.toResponse(department, coursesOf(department, null));
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

    @Transactional
    public DepartmentSoftDeleteResponse softDelete(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        department.setDeleted(true);
        return departmentMapper.toResponseWithSoftDelete(department);
    }

    @Transactional
    public DepartmentSoftDeleteResponse restoreSoftDelete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        department.setDeleted(false);
        return departmentMapper.toResponseWithSoftDelete(department);
    }

    @Transactional
    public DepartmentResponse replaceContacts(Long id, ReplaceContactsRequest request) {
        Department department = departmentRepository.findWithContactsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        department.getContacts().clear();
        request.contacts().forEach(c ->
            department.getContacts().add(new ContactInfo(c.type(), c.value())));

        return departmentMapper.toResponse(department, coursesOf(department, null));
    }

    //one query for the whole list instead of one per department
    private Map<Long, List<CourseSummaryResponse>> coursesByDepartment(List<Department> departments,
        Boolean deleted) {

        List<Long> ids = departments.stream().map(Department::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        return CourseSummaryRow.groupByOwner(
            departmentRepository.findCourseSummariesByDepartmentIds(ids, deleted));
    }

    private List<CourseSummaryResponse> coursesOf(Department department, Boolean deleted) {
        return coursesOf(coursesByDepartment(List.of(department), deleted), department);
    }

    private List<CourseSummaryResponse> coursesOf(Map<Long, List<CourseSummaryResponse>> coursesByDepartment,
        Department department) {

        return coursesByDepartment.getOrDefault(department.getId(), List.of());
    }
}
