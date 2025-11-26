package com.example.sharestay.security;

import com.example.sharestay.service.JwtService;
import com.example.sharestay.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        //추가 두 줄
        if (jwtService == null) throw new IllegalArgumentException("jwtService cannot be null");
        if (userDetailsService == null) throw new IllegalArgumentException("userDetailsService cannot be null");
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

//        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
//        String token = null;
        //추가 한 줄
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

//        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
//            token = header.substring(7);
        // 1. Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 즉시 다음 필터로 넘김
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

//        if (StringUtils.hasText(token) && jwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
//            String username = jwtService.extractUsername(token);
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // 2. "Bearer " 부분을 제외한 순수 토큰(JWT)만 추출
        final String jwt = authHeader.substring(7);

//            UsernamePasswordAuthenticationToken auth =
//                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        // 3. 토큰에서 사용자 이메일(username) 추출
        final String userEmail;
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // 토큰 파싱 중 오류 발생 시 (예: 만료, 형식 오류) 요청을 그냥 넘김
            filterChain.doFilter(request, response);
            return;
        }

//            SecurityContextHolder.getContext().setAuthentication(auth);
        // 4. 사용자 이메일이 존재하고, 아직 SecurityContext에 인증 정보가 없는 경우
        if (StringUtils.hasText(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. 토큰이 유효한지 최종 검증 (DB에서 가져온 userDetails와 비교)
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 6. 인증이 성공하면, SecurityContext에 인증 정보 등록
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(request, response);
    }
}
