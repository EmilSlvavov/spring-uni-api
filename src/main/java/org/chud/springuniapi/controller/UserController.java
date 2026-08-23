package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.request.UpdateUserProfileRequest;
import org.chud.springuniapi.dto.request.UpdateUserRequest;
import org.chud.springuniapi.dto.response.UserDisplayResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.dto.response.UserSoftDeleteResponse;
import org.chud.springuniapi.service.serviceInterface.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll(@RequestParam(required = false) Boolean deleted) {
        return userService.findAll(deleted);
    }

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
        UserResponse created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{userId}/courses/{courseId}")
    public UserResponse enroll(@PathVariable Long userId, @PathVariable Long courseId) {
        return userService.enroll(userId, courseId);
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return userService.update(id, request);
    }

    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileRequest request) {

        return userService.updateProfile(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/courses/{courseId}")
    public UserResponse withdraw(@PathVariable Long userId, @PathVariable Long courseId) {
        return userService.withdraw(userId, courseId);
    }

    @DeleteMapping("/softDeleted/{id}")
    public UserSoftDeleteResponse softDelete(@PathVariable Long id) {
        return userService.softDelete(id);
    }

    @PatchMapping("/softDeleted/{id}")
    public UserSoftDeleteResponse restoreSoftDelete(@PathVariable Long id) {
        return userService.restoreSoftDelete(id);
    }
}
