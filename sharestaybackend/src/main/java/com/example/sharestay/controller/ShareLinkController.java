package com.example.sharestay.controller;

import com.example.sharestay.dto.ShareLinkResponse;
import com.example.sharestay.service.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "ShareLink API", description = "Room별 공유 링크 생성 및 조회 API") // Swagger UI 상단에 그룹으로 표시됨
public class ShareLinkController {

    private final ShareLinkService shareLinkService;

    // ✅ 특정 방의 공유 링크 생성
    @Operation(
            summary = "공유 링크 생성",
            description = "특정 Room ID에 대한 공유 링크를 자동으로 생성합니다."
    )
    // ✅ 필요하면 수동으로 새로 생성 (실제로는 거의 안 쓸 수도 있음)
    @PostMapping("/{roomId}/share")
    public ResponseEntity<ShareLinkResponse> createShareLink(@PathVariable Long roomId) {
        ShareLinkResponse response = shareLinkService.createShareLink(roomId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "공유 링크 조회", description = "특정 Room ID에 해당하는 기존 공유 링크를 조회합니다.")
    // ✅ 공유 링크 조회: 평소엔 이거만 쓰면 됨
    @GetMapping("/{roomId}/share")
    public ResponseEntity<ShareLinkResponse> getShareLink(@PathVariable Long roomId) {
        try {
            ShareLinkResponse response = shareLinkService.getShareLink(roomId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 정말 예외적인 상황 (DB에 ShareLink가 안 들어갔거나 한 경우)
            return ResponseEntity.notFound().build();
        }
    }
}