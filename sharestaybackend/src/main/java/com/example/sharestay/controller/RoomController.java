package com.example.sharestay.controller;

import com.example.sharestay.dto.RoomDetailResponse;
//import com.example.sharestay.dto.RoomImageResponse;
import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import com.example.sharestay.entity.Room;
import com.example.sharestay.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room API", description = "방 등록/조회/수정/삭제 API")
public class RoomController {

    private final RoomService roomService;

    // 방 등록 (텍스트 + 이미지 파일 같이 받기)
    @Operation(summary = "방 등록", description = "호스트가 방 정보를 입력하고 이미지를 업로드합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomResponse> createRoom(
            @ModelAttribute RoomRequest request,        // 제목/가격/주소 등  @ModelAttribute + @RequestPart 이 조합 스프링에서 잘 안돌아감
            @RequestParam(required = false) List<MultipartFile> files  // 이미지 리스트
    ) {
        RoomResponse response = roomService.createRoom(request, files);
        return ResponseEntity.ok(response);
    }

    // 메인 화면 간단 검색과 필터 검색 통합
    @GetMapping("/search")
    public ResponseEntity<List<RoomResponse>> searchRooms(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String option
    ) {
        return ResponseEntity.ok(
                roomService.searchRooms(region, type, minPrice, maxPrice, option)
        );
    }


//    @Operation(summary = "방 이미지 업로드", description = "특정 방에 이미지를 업로드합니다.")
//    @PostMapping("/{roomId}/images")
//    public ResponseEntity<List<RoomImageResponse>> uploadImages(
//            @PathVariable Long roomId,
//            @RequestPart("files") List<MultipartFile> files
//    ) {
//        return ResponseEntity.ok(roomService.uploadRoomImages(roomId, files));
//    }

    // 방 수정
    @Operation(summary = "방 정보 수정", description = "특정 방의 기본 정보를 수정합니다.")
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestBody RoomRequest request
    ) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, request));
    }

    // 방 삭제
    @Operation(summary = "방 삭제", description = "특정 방을 삭제합니다.")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // 전체/검색 결과 방 목록
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getRoomList();
        return ResponseEntity.ok(rooms);
    }

    // 방 상세 조회
    @Operation(summary = "방 상세 조회", description = "특정 방의 상세 정보를 조회합니다.")
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoomDetail(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomDetail(roomId));
    }









}
