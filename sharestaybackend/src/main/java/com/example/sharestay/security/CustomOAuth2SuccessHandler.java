package com.example.sharestay.security;

import com.example.sharestay.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component //스프링 빈 생성 -> 필드가 있으면 자동으로 해당 빈을 찾아 넣어줌
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;

    // application.properties에서 설정한 리다이렉트 URL 주입
    @Value("${oauth2.success.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        // OAuth2 인증 성공 후 호출됨.
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        // 브라우저 -> google -> spring security 인증 정보가 흘러옴 이게 성공시 authentication 객체를 생성함
        // User 식별 정보 추출 (예를 들어 : email / sub -> 고유 번호 / username)
        // Principal = 사용자 정보

        String username = oauthUser.getAttribute("email");
        if(username == null) {
            logger.warn("OAuth2에서 username을 추출할 수 없습니다.");
            username = "oauth2user_" + oauthUser.getName();
        }
        // json에서 email 키를 꺼내오는것 기본적으로 google은 이메일을 기반으로 하기 때문에 상관없지만
        // 네이버, 카카오 같은 경우 이메일 비공개 이런식으로 되어 있을때 email 값이 null 이기때문에 임시 username을 만들어줌
        // oauthUser.getName() 은 보통 sub 값이 들어옴

        // JWT토큰 생성
        String token = jwtService.generateToken(username);
        // 프론트엔드로 리다이렉트할 URL 생성(토큰을 쿼리 파라미터로 추가해줘야 합니다)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl) // 기본 뼈대 URL을 만듬
                .queryParam("token", token) //URL 뒤에 ?token=xxxx 형식의 쿼리 파라미터를 붙인다.
                .build() // 최종 조립
                .encode(StandardCharsets.UTF_8) // 자동으로 인코딩 추가 프론트가 깨짐
                .toUriString(); // 문자열로 변환
        // 기존의 세션 제거
        clearAuthenticationAttributes(request);
        // 생성된 URL로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);


    }
}
