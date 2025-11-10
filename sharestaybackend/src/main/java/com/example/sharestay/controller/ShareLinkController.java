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
@Tag(name = "ShareLink API", description = "Room별 공유 링크 생성 및 조회 API") // Swagger UI 상단에 그룹으로 표시됨됨됨됨됨됨
public class ShareLinkController {

    private final ShareLinkService shareLinkService;

    // ✅ 특정 방의 공유 링크 생성
    @Operation(
            summary = "공유 링크 생성",
            description = "특정 Room ID에 대한 공유 링크를 자동으로 생성합니다."
    )
    @PostMapping("/{roomId}/share")
    public ResponseEntity<ShareLinkResponse> createShareLink(@PathVariable Long roomId) {
        ShareLinkResponse response = shareLinkService.createShareLink(roomId);
        return ResponseEntity.ok(response);
    }

    // ✅ 특정 방의 공유 링크 조회
    @Operation(
            summary = "공유 링크 조회",
            description = "특정 Room ID에 해당하는 기존 공유 링크를 조회합니다."
    )
    @GetMapping("/{roomId}/share")
    public ResponseEntity<ShareLinkResponse> getShareLink(@PathVariable Long roomId) {
        ShareLinkResponse response = shareLinkService.getShareLink(roomId);
        return ResponseEntity.ok(response);
    }
}
