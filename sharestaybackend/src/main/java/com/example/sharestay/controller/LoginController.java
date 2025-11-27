package com.example.sharestay.controller;

import com.example.sharestay.dto.LoginRequest;
import com.example.sharestay.entity.Ban;
import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.service.BanService;
import com.example.sharestay.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BanService banService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Optional<Ban> activeBan = banService.getActiveBanByUserId(user.getId());
            if (activeBan.isPresent()) {
                Ban ban = activeBan.get();

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "정지된 계정입니다.",
                        "banId", ban.getId(),
                        "endDate", ban.getEndDate()
                ));
            }

            String token = jwtService.generateToken(user.getUsername());

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(Map.of("accessToken", token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }
}
