package com.example.sharestay.dto;


import com.example.sharestay.domain.Host;
import com.example.sharestay.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String nickname;
    private String role;    // String 기준

    private String introduction;
    private Boolean termsAgreed;

    public static AuthResponse from(User user, Host host) {
        String accessToken = "fake-jwt-token-for-" + user.getUsername();
        String refreshToken = "fake-refresh-token";

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .introduction(host != null ? host.getIntroduction() : null)
                .termsAgreed(host != null && host.isTermsAgreed())
                .build();
    }
}
