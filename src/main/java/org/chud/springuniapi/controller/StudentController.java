package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.request.UpdateStudentProfileRequest;
import org.chud.springuniapi.dto.request.UpdateStudentRequest;
import org.chud.springuniapi.dto.response.StudentDisplayResponse;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.dto.response.StudentSoftDeleteResponse;
import org.chud.springuniapi.service.serviceInterface.IStudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final IStudentService studentService;

    public StudentController(IStudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentResponse> getAll(@RequestParam(required = false) Boolean deleted) {
        return studentService.findAll(deleted);
    }

    @GetMapping("/{id}")
    public StudentResponse getById(@PathVariable Long id, @RequestParam(required = false) Boolean deleted) {
        return studentService.findById(id, deleted);
    }

    @GetMapping("/display")
    public List<StudentDisplayResponse> getDisplayLabels() {
        return studentService.findDisplayLabels();
    }

    @GetMapping("/softDeleted/{isDeleted}")
    public List<StudentSoftDeleteResponse> getBySoftDeleted(@PathVariable boolean isDeleted) {
        return  studentService.findAllBySoftDeleted(isDeleted);
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse created = studentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{studentId}/courses/{courseId}")
    public StudentResponse enroll(@PathVariable Long studentId, @PathVariable Long courseId) {
        return studentService.enroll(studentId, courseId);
    }

    @PutMapping("/{id}")
    public StudentResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request) {

        return studentService.update(id, request);
    }

    @PutMapping("/{id}/profile")
    public StudentResponse updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentProfileRequest request) {

        return studentService.updateProfile(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{studentId}/courses/{courseId}")
    public StudentResponse withdraw(@PathVariable Long studentId, @PathVariable Long courseId) {
        return studentService.withdraw(studentId, courseId);
    }

    @DeleteMapping("/softDeleted/{id}")
    public StudentSoftDeleteResponse softDelete(@PathVariable Long id) {
        return studentService.softDelete(id);
    }

    @PatchMapping("/softDeleted/{id}")
    public StudentSoftDeleteResponse restoreSoftDelete(@PathVariable Long id) {
        return studentService.restoreSoftDelete(id);
    }
}
