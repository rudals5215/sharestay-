package com.example.sharestay.securityconfig;

import com.example.sharestay.security.CustomSuccessHandler;
import com.example.sharestay.security.JwtAuthenticationFilter;
import com.example.sharestay.service.CustomUserService;
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;
    private final CustomUserService customUserService;
    private final CustomSuccessHandler customSuccessHandler;

    public SecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            JwtService jwtService,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            CustomUserService customUserService,
            CustomSuccessHandler customSuccessHandler
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.clientRegistrationRepositoryProvider = clientRegistrationRepositoryProvider;
        this.customUserService = customUserService;
        this.customSuccessHandler = customSuccessHandler;
    }

    public void configGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Refresh-Token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
//
//        http.csrf(csrf -> csrf.disable())
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        // Swagger
//                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//
//                        // OAuth2 redirect URLs
//                        .requestMatchers("/oauth2/**").permitAll()
//                        .requestMatchers("/login/oauth2/**").permitAll()
//                        .requestMatchers("/login/oauth2/code/**").permitAll()
//
//                        // Public APIs
//                        .requestMatchers(HttpMethod.GET, "/api/statistics/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/map/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
//                        .requestMatchers("/api/login", "/api/signup").permitAll()
//
//                        // Room (host/admin only)
//                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                        .requestMatchers(HttpMethod.PATCH, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form.disable())
//                .httpBasic(basic -> basic.disable())
//
//                .oauth2Login(oauth -> oauth
//                        .userInfoEndpoint(userInfo ->
//                                userInfo.userService(customUserService)
//                        )
//                        .successHandler(customSuccessHandler)
//                )
//
//                // JWT 필터
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}

//         파이어베이스 테스트용 (지우지 마세요.)
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()   // 🔥 모든 요청 허용
                )

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .oauth2Login(oauth -> oauth.disable()); // OAuth2도 테스트 중 비활성화


        return http.build();
    }
}
