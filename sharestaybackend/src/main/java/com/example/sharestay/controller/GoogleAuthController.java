package com.example.sharestay.controller;


import com.example.sharestay.dto.GoogleTokenDto;
import com.example.sharestay.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    // 프런트엔드에서 Google ID 토큰을 POST 요청으로 받는 엔드포인트
    @PostMapping("/api/auth/google")
    public ResponseEntity<Map<String, String>> googleAuth(@RequestBody GoogleTokenDto googleTokenDto) {
        System.out.println("Google Auth Controller Reached!"); // ✨ 요청 도달 확인 로그 ✨

        try {
            // 1. Google ID 토큰을 서비스로 전달하여 검증하고 JWT를 얻습니다.
            String jwt = googleAuthService.authenticateAndGenerateJwt(googleTokenDto.getIdToken());

            // 2. JWT를 JSON 응답 형태로 클라이언트에 반환합니다.
            // 프런트엔드 (shoppingapi.ts)에서 response.data.token으로 이 값을 읽습니다.
            return ResponseEntity.ok(Collections.singletonMap("token", jwt));

        } catch (IllegalArgumentException e) {
            // 토큰이 유효하지 않은 경우
            System.err.println("Google ID Token Error: " + e.getMessage());
            // 400 Bad Request와 함께 오류 메시지를 반환합니다.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            // 그 외의 서버 내부 오류 (토큰 검증 실패 등)
            System.err.println("Google Authentication Server Error: " + e.getMessage());
            // 401 Unauthorized 또는 500 Internal Server Error를 반환할 수 있습니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Google 인증 중 오류 발생: " + e.getMessage()));
        }
    }
}
