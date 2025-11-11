package com.example.sharestay.service;

import com.example.sharestay.domain.User;
import com.example.sharestay.domain.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CustomOAuth2UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauthUser = super.loadUser(userRequest);

        // OAuth2 공급자에서 가져온 정보 (예: 구글)
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email attribute is missing from OAuth2 provider response");
        }

        User persisted = userRepository.findByUsername(email)
                .orElseGet(() -> {
                    // [ADD] 신규 사용자 저장
                    User newUser = User.builder()
                            .username(email)
                            .nickname(name != null ? name : "") // null 방지
                            .password(null) // [CHANGE] OAuth2 로그인은 패스워드 null 허용(엔티티 컬럼 nullable이어야 함)
                            .role("USER")
                            .signupDate(new Date())
                            .build();
                    return userRepository.save(newUser);
                });

        // JWT 발급
        String accessToken  = jwtService.generateAccessToken(persisted.getUsername());
        String refreshToken = jwtService.generateRefreshToken(persisted.getUsername());

        // 토큰을 OAuth2User의 속성에 추가 (컨트롤러에서 가져갈 수 있음)
        Map<String, Object> merged = new HashMap<>(oauthUser.getAttributes());
        merged.put("accessToken", accessToken);
        merged.put("refreshToken", refreshToken);

        return new DefaultOAuth2User(oauthUser.getAuthorities(), merged, "email");
    }
}
