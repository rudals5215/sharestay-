package com.example.sharestay.security;

import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${oauth2.success.redirect-url}")
    private String redirectUrl;

    public CustomSuccessHandler(JwtService jwtService,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String email = authentication.getName(); // username = email

        // 1) 사용자 조회
        User user = userRepository.findByUsername(email).orElse(null);

        // 2) 존재하는 경우 → 밴 체크
        if (user != null && user.isBanned()) {
            log.warn("Banned user login attempt: {}", email);

            String redirectUrlWithError = UriComponentsBuilder
                    .fromHttpUrl(redirectUrl)
                    .queryParam("error", "banned_user")
                    .build(true)
                    .toUriString();

            response.sendRedirect(redirectUrlWithError);
            return;
        }

        // 3) 구글 신규 사용자 자동 생성
        if (user == null) {
            log.info("New Google user detected, auto-creating account: {}", email);

            // 더 안전한 랜덤 비밀번호 생성 후 인코딩
            String randomPassword = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(randomPassword);

            user = User.createGoogleUser(email, encodedPassword);
            userRepository.save(user);
        }

        // 4) 정상 사용자 → JWT 발급
        String accessToken = jwtService.generateAccessToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        // 5) 프론트로 redirect (토큰 + username 포함)
        String redirectWithToken = UriComponentsBuilder
                .fromHttpUrl(redirectUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("username", user.getUsername())
                .build(true)
                .toUriString();

        response.sendRedirect(redirectWithToken);
    }
}
