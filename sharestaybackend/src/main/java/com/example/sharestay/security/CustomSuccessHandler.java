package com.example.sharestay.security;

import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component   // Spring 빈으로 등록, 스프링 컨테이너가 관리
@Slf4j       // 로그 찍을 수 있게 Lombok 사용
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;           // JWT 토큰 생성 서비스
    private final UserRepository userRepository;   // 유저 조회/저장 레포지토리

    @Value("${oauth2.success.redirect-url}")      // 로그인 성공 후 리다이렉트할 프론트 URL
    private String redirectUrl;

    // 생성자: DI(의존성 주입)로 JwtService와 UserRepository 주입
    public CustomSuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // 로그인 성공 시 호출되는 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("구글 로그인 석세스 핸들러 진입");  // 핸들러 진입 로그

        // 1️⃣ 인증 객체에서 이메일 가져오기
        String email = authentication.getName();   // 구글 로그인 시 이메일 반환

        // 2️⃣ DB에서 유저 조회, 없으면 새로 생성
        User user = userRepository.findByUsername(email)
                .orElseGet(() -> userRepository.save(User.createGoogleUser(email)));

        // 3️⃣ JWT Access Token과 Refresh Token 생성
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        // 4️⃣ 리다이렉트 URL에 토큰 붙이기
        String redirectWithToken = redirectUrl +
                "?accessToken=" + accessToken +
                "&refreshToken=" + refreshToken;

        // 5️⃣ 프론트 페이지로 리다이렉트
        response.sendRedirect(redirectWithToken);
    }
}
