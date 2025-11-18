package com.example.sharestay.security;

import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

//@Component
//@Slf4j
//public class CustomSuccessHandler implements AuthenticationSuccessHandler {
//
//    private final JwtService jwtService;
//    private final UserRepository userRepository;
//
//    @Value("${oauth2.success.redirect-url}")
//    private String redirectUrl;
//
//    public CustomSuccessHandler(JwtService jwtService, UserRepository userRepository) {
//        this.jwtService = jwtService;
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication) throws IOException {
//        log.info("구글 로그인 석세스 핸들러 진입");
//        // OAuth2 유저 정보(email)
//        String email = authentication.getName();
//
//        // DB에 없으면 생성
//        User user = userRepository.findByUsername(email)
//                .orElseGet(() -> userRepository.save(User.createGoogleUser(email)));
//
//        // JWT 발급
//        String accessToken = jwtService.generateAccessToken(email);
//        String refreshToken = jwtService.generateRefreshToken(email);
//
//        // React 페이지로 리다이렉트 + query param으로 토큰 전달
//        String redirectWithToken = redirectUrl + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
//        response.sendRedirect(redirectWithToken);
//    }
//}
@Component
@Slf4j
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${oauth2.success.redirect-url}")
    private String redirectUrl;

    public CustomSuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("구글 로그인 석세스 핸들러 진입");

        String email = authentication.getName();   // = "email"

        User user = userRepository.findByUsername(email)
                .orElseGet(() -> userRepository.save(User.createGoogleUser(email)));

        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        String redirectWithToken = redirectUrl +
                "?accessToken=" + accessToken +
                "&refreshToken=" + refreshToken;

        response.sendRedirect(redirectWithToken);
    }
}
