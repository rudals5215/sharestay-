package com.example.sharestay.securityconfig;

import com.example.sharestay.security.JwtAuthenticationFilter;
import com.example.sharestay.service.JwtService;
import com.example.sharestay.service.UserDetailsServiceImpl;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
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
                        .requestMatchers("/api/login", "/api/signup").permitAll() // 로그인/회원가입은 인증 없이
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(AbstractHttpConfigurer::disable)
//            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(new AntPathRequestMatcher("/api/signup"), new AntPathRequestMatcher("/api/login")).permitAll()
//                .anyRequest().authenticated()
//            )
//            .formLogin(AbstractHttpConfigurer::disable)
//            .httpBasic(AbstractHttpConfigurer::disable);
//        return http.build();
//    }
}
