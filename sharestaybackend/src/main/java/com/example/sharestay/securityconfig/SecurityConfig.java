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
import org.springframework.core.annotation.Order;
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
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Refresh-Token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
//
//        http
//                .csrf(csrf -> csrf.disable()) // 기존 유지
//                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 반영
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 STATELESS로 (JWT 방식)
//                .authorizeHttpRequests(auth -> auth
//                        // Swagger/OpenAPI 관련 URL 모두 허용  // swagger url 403 떠서 이거 추가함
//                        .requestMatchers(
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**",
//                                "/swagger-ui.html"
//                        ).permitAll()
//                        .requestMatchers(HttpMethod.POST,"/api/favorites/**").permitAll()
//                        .requestMatchers(HttpMethod.GET,"/api/map/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/statistics/**").permitAll()
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                        .requestMatchers("/api/auth/google").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                        .requestMatchers(HttpMethod.PATCH, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                        .requestMatchers("/api/login", "/api/signup").permitAll() // 로그인/회원가입은 인증 없이
//                        .requestMatchers("/login-success").permitAll()
//                        .requestMatchers("/oauth2/**").permitAll()
//
//                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
//                )
//                .oauth2Login(oauth -> oauth
//                        .userInfoEndpoint(userInfo ->
//                                userInfo.userService(customUserService)
//                        )
//                        .successHandler(customSuccessHandler)
//                )
//                .formLogin(form -> form.disable())
//                .httpBasic(basic -> basic.disable())
////                .oauth2Login(oauth -> oauth
////                        .userInfoEndpoint(userInfo ->
////                                userInfo.userService(customUserService)
////                        )
////                        .successHandler(customSuccessHandler)
////                )
//                // AJAX 요청에 대해 401 응답을 보내도록 예외 처리 설정
//                .exceptionHandling(e -> e
//                        .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
//                )
//                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//
//        // 파이어베이스 테스트용 (지우지 마세요.)
////        http
////                .csrf(csrf -> csrf.disable())
////                .cors(cors -> cors.disable())  // 테스트용 CORS 풀기
////                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
////                .authorizeHttpRequests(auth -> auth
////                        .anyRequest().permitAll()   // 🔥 모든 요청 허용
////                )
////                .formLogin(form -> form.disable())
////                .httpBasic(basic -> basic.disable())
////                .oauth2Login(oauth -> oauth.disable()); // OAuth2도 테스트 중 비활성화
//
////        return http.build();
//    }
// 현재 개빡치는 버전이 밑에 부분입니다.
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
//
//    http
//            // 1. CSRF, CORS, 세션 관리 등 기본 설정
//            .csrf(csrf -> csrf.disable())
//            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//            // 2. JWT 필터 및 인증 방식(OAuth2 등) 설정
//            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//            .oauth2Login(oauth -> oauth
//                    .userInfoEndpoint(userInfo ->
//                            userInfo.userService(customUserService)
//                    )
//                    .successHandler(customSuccessHandler)
//            )
//            .formLogin(form -> form.disable())
//            .httpBasic(basic -> basic.disable())
//
//            // 3. 경로별 접근 권한 설정 (가장 중요)
//            .authorizeHttpRequests(auth -> auth
//                    // 비회원도 접근 가능한 경로들
//                    .requestMatchers(
//                            "/v3/api-docs/**",
//                            "/swagger-ui/**",
//                            "/swagger-ui.html"
//                    ).permitAll()
//                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/map/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/statistics/**").permitAll()
//                    .requestMatchers("/api/login", "/api/signup").permitAll()
//                    .requestMatchers("/api/auth/google").permitAll()
//                    .requestMatchers("/login-success").permitAll()
//                    .requestMatchers("/oauth2/**").permitAll()
//
//                    // 인증이 필요한 경로들
//                    .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                    .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                    .requestMatchers(HttpMethod.PATCH, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                    .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                    .requestMatchers(HttpMethod.POST,"/api/favorites/**").authenticated() // permitAll()에서 변경
//
//                    // 위에서 지정하지 않은 나머지 모든 경로는 인증을 요구함
//                    .anyRequest().authenticated()
//            )
//
//            // 4. 인증/인가 실패 시 최종 처리
//            .exceptionHandling(e -> e
//                    .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
//            );
//
//        return http.build();
//    }
//@Bean
//public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
//
//    http
//            .csrf(csrf -> csrf.disable())
//            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//            .oauth2Login(oauth -> oauth
//                    .userInfoEndpoint(userInfo ->
//                            userInfo.userService(customUserService)
//                    )
//                    .successHandler(customSuccessHandler)
//            )
//            .formLogin(form -> form.disable())
//            .httpBasic(basic -> basic.disable())
//            .authorizeHttpRequests(auth -> auth
//                    .requestMatchers(
//                            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
//                            "/api/login", "/api/signup", "/api/auth/google",
//                            "/login-success", "/oauth2/**"
//                    ).permitAll()
//                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/map/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/statistics/**").permitAll()
//                    .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                    .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                    .requestMatchers(HttpMethod.PATCH, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                    .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
//                    .requestMatchers(HttpMethod.POST,"/api/favorites/**").authenticated()
//                    .anyRequest().authenticated()
//            );
//
//    return http.build();
//}
    // SecurityConfig.java

// ... (다른 import 구문 및 클래스 선언은 그대로) ...

    // 기존의 filterChain 메서드들을 아래 두 개의 메서드로 완전히 교체합니다.

    /**
     * 인증이 전혀 필요 없는, 외부에 완전히 공개된 경로들을 위한 필터 체인.
     * 이 체인에는 JWT 필터나 OAuth2 설정이 전혀 포함되지 않습니다.
     */
    @Bean
    @Order(1) // 가장 먼저 실행되어야 합니다.
    public SecurityFilterChain publicEndpointsFilterChain(HttpSecurity http) throws Exception {
        http
                // 이 필터 체인이 처리할 요청 경로를 명시적으로 지정합니다.
                .securityMatcher(
                        // 기존 로그인/OAuth2/Swagger 관련 경로에 더해,
                        // 공개적으로 접근해야 할 모든 GET API 경로를 추가합니다.
                        "/api/rooms/**", "/api/map/**", "/uploads/**", "/api/statistics/**",
                        "/api/login", "/api/signup",
                        "/login-success",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                )
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * 위에서 지정한 경로 외의 모든 요청을 처리하는 기본 필터 체인.
     * 공개 GET 요청과 인증이 필요한 모든 요청을 함께 처리합니다.
     */
    @Bean
    @Order(2) // publicEndpointsFilterChain 다음에 실행됩니다.
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
                // 이 필터 체인으로 들어온 모든 요청에 대해 경로별 규칙을 적용합니다.
                .authorizeHttpRequests(auth -> auth
                        // =================== 비회원 접근 허용 경로 ===================
                        .requestMatchers(HttpMethod.GET,
                                "/api/rooms/**",
                                "/api/map/**",
                                "/uploads/**",
                                "/api/statistics/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/login", "/api/signup", "/api/auth/google").permitAll()
                        .requestMatchers("/login-success", "/oauth2/**").permitAll()

                        // =================== 인증 필요 경로 ===================
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/favorites/**").authenticated()

                        // =================== 나머지 모든 경로는 인증 요구 ===================
                        .anyRequest().authenticated()
                )

                // 인증 실패 시 401 Unauthorized 응답을 보내도록 설정합니다.
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
                )

                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // JWT 필터와 OAuth2 설정을 여기에만 포함합니다.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customUserService)
                        )
                        .successHandler(customSuccessHandler)
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

}

