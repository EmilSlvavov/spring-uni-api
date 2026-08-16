package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.CreateDepartmentRequest;
import org.chud.springuniapi.dto.request.ReplaceContactsRequest;
import org.chud.springuniapi.dto.request.UpdateDepartmentRequest;
import org.chud.springuniapi.dto.response.DepartmentResponse;
import org.chud.springuniapi.dto.response.DepartmentSoftDeleteResponse;
import org.chud.springuniapi.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<DepartmentResponse> getAll(@RequestParam(required = false) Boolean deleted) {
        return departmentService.findAll(deleted);
    }

    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable Long id, @RequestParam(required = false) Boolean deleted) {
        return departmentService.findById(id, deleted);
    }

    @GetMapping("/softDeleted/{isDeleted}")
    public List<DepartmentSoftDeleteResponse> getBySoftDeleted(@PathVariable boolean isDeleted){
        return departmentService.findAllBySoftDeleted(isDeleted);
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse created = departmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {

        return departmentService.update(id, request);
    }

    @PutMapping("/{id}/contacts")
    public DepartmentResponse replaceContacts(
        @PathVariable Long id,
        @Valid @RequestBody ReplaceContactsRequest request) {

        return departmentService.replaceContacts(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/softDeleted/{id}")
    public DepartmentSoftDeleteResponse softDelete(@PathVariable Long id){
        return departmentService.softDelete(id);
    }

    @PatchMapping("/softDeleted/{id}")
    public DepartmentSoftDeleteResponse restoreSoftDelete(@PathVariable Long id){
        return departmentService.restoreSoftDelete(id);
    }
}
