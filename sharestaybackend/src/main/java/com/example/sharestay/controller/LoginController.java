package com.example.sharestay.controller;

import com.example.sharestay.dto.LoginRequest;
//import com.example.sharestay.service.JwtService;
import com.example.sharestay.entity.User;
import com.example.sharestay.exception.BannedUserException;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {

            // 1) 먼저 DB에서 사용자 조회
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2) 밴 계정인지 확인
            if (user.isBanned()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Banned user");
            }

            // 3) 정상 사용자 → 스프링 시큐리티 인증 진행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 4) JWT 발급
            String token = jwtService.generateToken(loginRequest.getUsername());
            return ResponseEntity.ok(Map.of("accessToken", token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}


