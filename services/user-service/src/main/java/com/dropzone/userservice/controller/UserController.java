package com.dropzone.userservice.controller;

import jakarta.validation.Valid;
import com.dropzone.userservice.dto.CreateUserRequest;
import com.dropzone.userservice.dto.UserDto;
import com.dropzone.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/users", "/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean paged) {
        if (Boolean.TRUE.equals(paged)) {
            return ResponseEntity.ok(userService.getAllUsersPaged(page, size));
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String xUserIdHeader) {
        if ((request.getKeycloakUserId() == null || request.getKeycloakUserId().isBlank()) && xUserIdHeader != null && !xUserIdHeader.isBlank()) {
            request.setKeycloakUserId(xUserIdHeader);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
