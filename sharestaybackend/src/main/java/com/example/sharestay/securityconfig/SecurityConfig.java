package com.example.sharestay.securityconfig;

import com.example.sharestay.security.CustomOAuth2SuccessHandler;
import com.example.sharestay.security.JwtAuthenticationFilter;
import com.example.sharestay.service.JwtService;
import com.example.sharestay.service.UserDetailsServiceImpl;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtService jwtService, CustomOAuth2SuccessHandler customOAuth2SuccessHandler,
                          ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
        this.clientRegistrationRepositoryProvider = clientRegistrationRepositoryProvider;
    }

    public void configGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
                .csrf(csrf -> csrf.disable()) // 기존 유지
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 반영
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 STATELESS로 (JWT 방식)
                .authorizeHttpRequests(auth -> auth
                        // Swagger/OpenAPI 관련 URL 모두 허용  // swagger url 403 떠서 이거 추가함
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/google").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers("/api/login", "/api/signup").permitAll() // 로그인/회원가입은 인증 없이
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // oauth2 로그인 설정 (커스텀 핸들러 연결)
        ClientRegistrationRepository registrations = clientRegistrationRepositoryProvider.getIfAvailable();
        if (registrations != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .successHandler(customOAuth2SuccessHandler) // 핸들러에서 사용자 정보 처리
            );
        }

        return http.build();
    }
}
