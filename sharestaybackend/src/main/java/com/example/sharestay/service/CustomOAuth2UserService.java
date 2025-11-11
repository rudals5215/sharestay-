package com.example.sharestay.service;

import com.example.sharestay.domain.User;
import com.example.sharestay.domain.UserRepository;
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
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);

        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"),
                    "Email attribute is missing from OAuth2 provider response");
        }

        String name = oauthUser.getAttribute("name");
        String nickname = (name != null && !name.isBlank()) ? name : email;

        User persisted = userRepository.findByUsername(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(email)
                        .password(null)
                        .loginType(resolveLoginType(userRequest))
                        .nickname(nickname)
                        .address("")
                        .phoneNumber("")
                        .role("USER")
                        .signupDate(new Date())
                        .build()));

        String accessToken = jwtService.generateAccessToken(persisted.getUsername());
        String refreshToken = jwtService.generateRefreshToken(persisted.getUsername());

        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());
        attributes.put("accessToken", accessToken);
        attributes.put("refreshToken", refreshToken);

        return new DefaultOAuth2User(oauthUser.getAuthorities(), attributes, "email");
    }

    private String resolveLoginType(OAuth2UserRequest userRequest) {
        return Optional.ofNullable(userRequest.getClientRegistration())
                .map(reg -> reg.getRegistrationId())
                .map(String::toUpperCase)
                .orElse("OAUTH");
    }
}
