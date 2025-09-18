package com.example.notification.controller;

import com.example.notification.dto.UserPreferenceRequest;
import com.example.notification.dto.UserPreferenceResponse;
import com.example.notification.dto.UserRequest;
import com.example.notification.entity.User;
import com.example.notification.enums.NotificationType;
import com.example.notification.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createOrUpdateUser(@Valid @RequestBody UserRequest request) {
        User user = userService.createOrUpdateUser(request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<User> getUser(@PathVariable String tenantId) {
        User user = userService.getUserByTenantId(tenantId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{tenantId}/preferences")
    public ResponseEntity<UserPreferenceResponse> saveUserPreference(
            @PathVariable String tenantId,
            @Valid @RequestBody UserPreferenceRequest request) {
        UserPreferenceResponse response = userService.saveUserPreference(tenantId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tenantId}/preferences")
    public ResponseEntity<List<UserPreferenceResponse>> getUserPreferences(@PathVariable String tenantId) {
        List<UserPreferenceResponse> preferences = userService.getUserPreferences(tenantId);
        return ResponseEntity.ok(preferences);
    }

    @GetMapping("/{tenantId}/preferences/{notificationType}")
    public ResponseEntity<UserPreferenceResponse> getUserPreference(
            @PathVariable String tenantId,
            @PathVariable NotificationType notificationType) {
        UserPreferenceResponse preference = userService.getUserPreference(tenantId, notificationType);
        if (preference == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(preference);
    }

    @DeleteMapping("/{tenantId}/preferences/{notificationType}")
    public ResponseEntity<Void> deleteUserPreference(
            @PathVariable String tenantId,
            @PathVariable NotificationType notificationType) {
        userService.deleteUserPreference(tenantId, notificationType);
        return ResponseEntity.ok().build();
    }
}