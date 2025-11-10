package com.example.sharestay.controller;

import com.example.sharestay.dto.AuthResponse;
import com.example.sharestay.dto.SignupRequest;
import com.example.sharestay.dto.UpdateUserRequest;
import com.example.sharestay.dto.UserProfileResponse;
import com.example.sharestay.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        AuthResponse response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    @GetMapping("/users/{email}")
    public UserProfileResponse getUser(@PathVariable String email) {
        return userService.getUser(email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UserProfileResponse> getAllUser() {
        return userService.getAllUser();
    }

    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    @PutMapping("/users/{email}")
    public UserProfileResponse updateUser(@PathVariable String email, @RequestBody UpdateUserRequest request) {
        return userService.updateUser(email, request);
    }
}
