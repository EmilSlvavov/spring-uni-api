package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;
import org.chud.springuniapi.service.serviceInterface.ICourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final ICourseService courseService;

    public CourseController(ICourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseResponse> getAll(@RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) Boolean deleted) {
        return departmentId == null
                ? courseService.findAll(deleted)
                : courseService.findByDepartment(departmentId, deleted);
    }

    @GetMapping("/{id}")
    public CourseResponse getById(@PathVariable Long id, @RequestParam(required = false) Boolean deleted) {
        return courseService.findById(id, deleted);
    }

    //endpoint for showcasing projection
    @GetMapping("/summary")
    public List<CourseListItemResponse> getSummariesByDepartment(@RequestParam Long departmentId) {
        return courseService.findSummariesByDepartment(departmentId);
    }

    @GetMapping("/softDeleted/{isDeleted}")
    public List<CourseSoftDeleteResponse> getBySoftDeleted(@PathVariable boolean isDeleted){
        return courseService.findAllBySoftDeleted(isDeleted);
    }

    @PostMapping("/online")
    public ResponseEntity<CourseResponse> createOnline(@Valid @RequestBody CreateOnlineCourseRequest request) {
        CourseResponse created = courseService.createOnline(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/onsite")
    public ResponseEntity<CourseResponse> createOnsite(@Valid @RequestBody CreateOnsiteCourseRequest request) {
        CourseResponse created = courseService.createOnsite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CourseResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {

        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/softDeleted/{id}")
    public CourseSoftDeleteResponse softDelete(@PathVariable Long id){
        return courseService.softDelete(id);
    }

    @PatchMapping("/softDeleted/{id}")
    public CourseSoftDeleteResponse restoreSoftDelete(@PathVariable Long id){
        return courseService.restoreSoftDelete(id);
    }
}
