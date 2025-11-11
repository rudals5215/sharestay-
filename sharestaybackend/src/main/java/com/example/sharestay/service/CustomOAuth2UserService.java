package com.example.sharestay.service;

import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CustomOAuth2UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauthUser = super.loadUser(userRequest);

        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"),
                    "Email attribute is missing from OAuth2 provider response");
        }

        String name = oauthUser.getAttribute("name");

        // DB에 존재하는지 확인
        Optional<User> optionalUser = userRepository.findByUsername(email);
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            // 신규 사용자라면 DB에 저장
            user = User.builder()
                    .username(email)
                    .nickname(name)
                    .password("") // OAuth2 로그인은 비밀번호 없음
                    .role("USER")
                    .signupDate(new Date())
                    .build();
            userRepository.save(user);
        }

        // JWT 발급
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        // 토큰을 OAuth2User의 속성에 추가 (컨트롤러에서 가져갈 수 있음)
        oauthUser.getAttributes().put("accessToken", accessToken);
        oauthUser.getAttributes().put("refreshToken", refreshToken);

        return oauthUser;
    }
}
