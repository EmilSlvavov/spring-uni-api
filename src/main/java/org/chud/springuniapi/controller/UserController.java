package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.AssignRoleRequest;
import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.request.UpdateUserProfileRequest;
import org.chud.springuniapi.dto.request.UpdateUserRequest;
import org.chud.springuniapi.dto.response.UserDisplayResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.dto.response.UserSoftDeleteResponse;
import org.chud.springuniapi.service.facade.IEnrollmentFacade;
import org.chud.springuniapi.service.facade.IUserAccountFacade;
import org.chud.springuniapi.service.serviceInterface.IUserService;
import org.chud.springuniapi.validation.NotAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    //single aggregate reads and writes go straight to the service, the operations
    //that also touch a Course, a Role or the token store go through a facade
    private final IUserService userService;
    private final IUserAccountFacade userAccountFacade;
    private final IEnrollmentFacade enrollmentFacade;

    public UserController(
            IUserService userService,
            IUserAccountFacade userAccountFacade,
            IEnrollmentFacade enrollmentFacade) {
        this.userService = userService;
        this.userAccountFacade = userAccountFacade;
        this.enrollmentFacade = enrollmentFacade;
    }

    @GetMapping
    public List<UserResponse> getAll(@RequestParam(required = false) Boolean deleted) {
        return userService.findAll(deleted);
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isAccessingSelf(#id, authentication)")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id, @RequestParam(required = false) Boolean deleted) {
        return userService.findById(id, deleted);
    }

    @GetMapping("/display")
    public List<UserDisplayResponse> getDisplayLabels() {
        return userService.findDisplayLabels();
    }

    @GetMapping("/softDeleted/{isDeleted}")
    public List<UserSoftDeleteResponse> getBySoftDeleted(@PathVariable boolean isDeleted) {
        return  userService.findAllBySoftDeleted(isDeleted);
    }


    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userAccountFacade.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isAccessingSelf(#userId, authentication)")
    @PostMapping("/{userId}/courses/{courseId}")
    public UserResponse enroll(@NotAdmin @PathVariable Long userId, @PathVariable Long courseId) {
        return enrollmentFacade.enroll(userId, courseId);
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isAccessingSelf(#id, authentication)")
    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return userService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isAccessingSelf(#id, authentication)")
    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileRequest request) {

        return userService.updateProfile(id, request);
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isAccessingSelf(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isAccessingSelf(#userId, authentication)")
    @DeleteMapping("/{userId}/courses/{courseId}")
    public UserResponse withdraw(@PathVariable Long userId, @PathVariable Long courseId) {
        return enrollmentFacade.withdraw(userId, courseId);
    }

    @PreAuthorize("hasRole('ADMIN') || @authorizationService.isAccessingSelf(#id, authentication)")
    @DeleteMapping("/softDeleted/{id}")
    public UserSoftDeleteResponse softDelete(@PathVariable Long id) {
        return userService.softDelete(id);
    }

    @PatchMapping("/softDeleted/{id}")
    public UserSoftDeleteResponse restoreSoftDelete(@PathVariable Long id) {
        return userService.restoreSoftDelete(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/role")
    public UserResponse assignRole(@PathVariable Long id, @Valid @RequestBody AssignRoleRequest request) {
        return userAccountFacade.assignRole(id, request.role());
    }
}
