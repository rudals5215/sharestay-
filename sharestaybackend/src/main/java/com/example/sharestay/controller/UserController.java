package com.example.sharestay.controller;

import com.example.sharestay.dto.*;
import com.example.sharestay.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.signup(request));
    }

    // 특정 유저 정보 조회
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    @GetMapping("/users/{email}")
    public UserProfileResponse getUser(@PathVariable String email) {
        return userService.getUser(email);
    }

    // 모든 유저 정보 조회 (관리자 전용)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UserProfileResponse> getAllUser() {
        return userService.getAllUser();
    }

    // 유저 정보 수정
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    @PutMapping("/users/{email}")
    public UserProfileResponse updateUser(@PathVariable String email, @RequestBody UpdateUserRequest request) {
        return userService.updateUser(email, request);
    }

    // --------------------------
    // 🔥 Ban / Unban 로직 추가 (관리자 전용)
    // --------------------------

    // 유저 Ban (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{email}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable String email,
            @RequestBody BanRequest request) {

        userService.banUser(email, request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{email}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable String email) {
        userService.unbanUser(email);
        return ResponseEntity.ok().build();
    }
}
