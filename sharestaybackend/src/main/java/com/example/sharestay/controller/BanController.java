package com.example.sharestay.controller;

import com.example.sharestay.dto.BanRequest;
import com.example.sharestay.dto.BanResponse;
import com.example.sharestay.service.BanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 이 클래스가 REST API 컨트롤러
@RequestMapping("/api/bans")
@RequiredArgsConstructor
public class BanController {
    private final BanService banService;

    /**
     * 사용자 정지 (관리자용)
     * @param userId 정지할 사용자의 ID
     * @param request 정지 사유, 기간 등 정보
     * @return 생성된 정지 정보
     */
    @PostMapping("/users/{userId}")
    public ResponseEntity<BanResponse> banUser(@PathVariable Long userId, @RequestBody BanRequest request) {
        // TODO: 현재 인증된 관리자(Admin)의 ID를 가져오는 로직이 필요합니다.
        // 예: Long adminId = SecurityUtil.getCurrentAdminId();
        Long adminId = 1L; // 임시 관리자 ID

        BanResponse response = banService.banUser(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 정지 해제 (관리자용)
     * @param userId 정지를 해제할 사용자의 ID
     * @return 응답 없음
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> unbanUser(@PathVariable Long userId) {
        banService.unbanUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 사용자의 정지 기록 전체 조회
     * @param userId 조회할 사용자의 ID
     * @return 사용자의 모든 정지 기록 리스트
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<BanResponse>> getBanHistory(@PathVariable Long userId) {
        List<BanResponse> history = banService.getBanHistory(userId);
        return ResponseEntity.ok(history);
    }
}