package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.request.UpdateStudentProfileRequest;
import org.chud.springuniapi.dto.request.UpdateStudentRequest;
import org.chud.springuniapi.dto.response.StudentDisplayResponse;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.dto.response.StudentSoftDeleteResponse;
import org.chud.springuniapi.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentResponse> getAll() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public StudentResponse getById(@PathVariable Long id) {
        return studentService.findById(id);
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
    public StudentSoftDeleteResponse switchSoftDeleted(@PathVariable Long id) {
        return studentService.switchSoftDelete(id);
    }
}
