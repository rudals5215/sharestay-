package com.example.sharestay.security;

import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
//import com.example.sharestay.service.CustomUserDetailsService;
import com.example.sharestay.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component   // Spring 빈으로 등록, 스프링 컨테이너가 관리
@Slf4j       // 로그 찍을 수 있게 Lombok 사용
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${oauth2.success.redirect-url}")      // 로그인 성공 후 리다이렉트할 프론트 URL
    private String redirectUrl;

    public CustomSuccessHandler(JwtService jwtService,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 로그인 성공 시 호출되는 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("구글 로그인 석세스 핸들러 진입");  // 핸들러 진입 로그

        // 1️⃣ 인증 객체에서 이메일 가져오기
        String email = authentication.getName();   // 구글 로그인 시 이메일 반환

        User user = userRepository.findByUsername(email)
                .orElseGet(() -> {
                    log.info("New user from Google login: {}", email);
                    String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
                    return userRepository.save(User.createGoogleUser(email, encodedPassword));
                });

        // 2️⃣ 사용자 상태(밴 여부) 확인
        // User 엔티티에 isBanned()와 같은 상태 확인 메서드가 있다고 가정합니다.
        if (user.isBanned()) {
            log.warn("Banned user login attempt: {}", email);
            // 밴 처리된 사용자는 에러 메시지와 함께 리다이렉트
            String redirectUrlWithBanError = UriComponentsBuilder
                    .fromHttpUrl(redirectUrl)
                    .queryParam("error", "banned_user") // 프론트엔드에 에러 코드만 전달
                    .build(true)
                    .toUriString();
            response.sendRedirect(redirectUrlWithBanError);
            return; // 토큰 발급 없이 핸들러 종료
        }

        // 3️⃣ 정상 사용자인 경우, JWT Access Token과 Refresh Token 생성
        String accessToken = jwtService.generateAccessToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        String redirectWithToken = UriComponentsBuilder
                .fromHttpUrl(redirectUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("username", user.getUsername())
                .build(true)
                .toUriString();
        // 4️⃣ 프론트 페이지로 리다이렉트
        response.sendRedirect(redirectWithToken);
    }
}
