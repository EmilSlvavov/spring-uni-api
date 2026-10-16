package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;
import org.chud.springuniapi.enums.CourseType;
import org.chud.springuniapi.security.MyUserDetails;
import org.chud.springuniapi.service.facade.ICourseCatalogFacade;
import org.chud.springuniapi.service.serviceInterface.ICourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final ICourseService courseService;
    private final ICourseCatalogFacade courseCatalogFacade;

    public CourseController(ICourseService courseService, ICourseCatalogFacade courseCatalogFacade) {
        this.courseService = courseService;
        this.courseCatalogFacade = courseCatalogFacade;
    }

    @GetMapping
    public Page<CourseResponse> getAll(@RequestParam(required = false) Long departmentId,
                                       @RequestParam(required = false) Boolean deleted,
                                       @PageableDefault(size = 20, sort = {"name", "id"}) Pageable pageable,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false) CourseType type,
                                       @RequestParam(required = false) Long lecturerId) {
        org.chud.springuniapi.controller.support.SortWhitelist.check(pageable, Set.of("name", "id"));
        return courseCatalogFacade.search(q, type, departmentId, lecturerId, deleted, pageable);
    }

    @GetMapping("/{id}")
    public CourseResponse getById(@PathVariable Long id, @RequestParam(required = false) Boolean deleted) {
        return courseService.findById(id, deleted);
    }

    //endpoint for showcasing projection
    @GetMapping("/summary")
    public List<CourseListItemResponse> getSummariesByDepartment(@RequestParam Long departmentId) {
        return courseCatalogFacade.findSummariesByDepartment(departmentId);
    }

    @GetMapping("/softDeleted")
    public Page<CourseSoftDeleteResponse> getBySoftDeleted(@RequestParam(required = false) Boolean deleted
            , @AuthenticationPrincipal MyUserDetails principal
            , @PageableDefault(size = 20, sort = {"name", "id"}) Pageable pageable
    ) {
        org.chud.springuniapi.controller.support.SortWhitelist.check(pageable, Set.of("name", "id"));

        boolean admin = principal.getAuthorities().stream().anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));

        Long lecturerId = admin ? null : principal.id();

        return courseService.findAllBySoftDeleted(deleted, lecturerId, pageable);
    }

    @PostMapping("/online")
    public ResponseEntity<CourseResponse> createOnline(@Valid @RequestBody CreateOnlineCourseRequest request
            , @AuthenticationPrincipal MyUserDetails principal
    ) {
        CourseResponse created = courseCatalogFacade.createOnline(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/onsite")
    public ResponseEntity<CourseResponse> createOnsite(@Valid @RequestBody CreateOnsiteCourseRequest request
            , @AuthenticationPrincipal MyUserDetails principal
    ) {
        CourseResponse created = courseCatalogFacade.createOnsite(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isCourseOwner(#id, authentication)")
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

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isCourseOwner(#id, authentication)")
    @DeleteMapping("/softDeleted/{id}")
    public CourseSoftDeleteResponse softDelete(@PathVariable Long id) {
        return courseService.softDelete(id);
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isCourseOwner(#id, authentication)")
    @PatchMapping("/softDeleted/{id}")
    public CourseSoftDeleteResponse restoreSoftDelete(@PathVariable Long id) {
        return courseService.restoreSoftDelete(id);
    }
}
