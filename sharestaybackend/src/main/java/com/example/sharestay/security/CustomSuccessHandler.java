package com.example.sharestay.security;

import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.service.JwtService;
import jakarta.servlet.http.Cookie;
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

        userRepository.findByUsername(email)
                .orElseGet(() -> {
                    String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
                    return userRepository.save(User.createGoogleUser(email, encodedPassword));
                });
        // jwt 토큰 생성
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        // jwt를 HttpOnly 쿠키로 저장
        Cookie accessCookie = createCookie("accessToken", accessToken, 60 * 60); // 쿠키 유효시간 한시간
        Cookie refreshCookie = createCookie("refreshToken", refreshToken, 7 * 24 * 60 * 60); // 7일

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // 프론트로 리다이렉트 (토큰 전달 x)
        response.sendRedirect(redirectUrl);
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        // 개발용 (HTTP)
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");

        // 배포용 (HTTPS)
//        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
//        cookie.setPath("/");
//        cookie.setMaxAge(maxAge);
//        cookie.setAttribute("SameSite", "None");

       return cookie;
    }

}
