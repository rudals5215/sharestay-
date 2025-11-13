package com.example.sharestay.controller;

import com.example.sharestay.dto.RoomDetailResponse;
import com.example.sharestay.dto.RoomImageResponse;
import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import com.example.sharestay.entity.Room;
import com.example.sharestay.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room API", description = "방 등록/조회/수정/삭제 API")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "방 등록", description = "호스트가 새로운 방을 등록합니다.")
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "메인 추천용 간단 검색", description = "지역 키워드 기반으로 간단 검색을 수행합니다.")
    @GetMapping("/search/simple")
    public ResponseEntity<List<RoomResponse>> simpleSearch(
            @RequestParam(defaultValue = "") String region
    ) {
        return ResponseEntity.ok(
                roomService.searchRooms(region, null, null, null, null)
        );
    }

    @Operation(summary = "필터 검색", description = "지역/타입/가격/옵션 필터를 적용한 검색을 수행합니다.")
    @GetMapping("/search/filter")
    public ResponseEntity<List<RoomResponse>> filterSearch(
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

    @Operation(summary = "방 상세 조회", description = "특정 방의 상세 정보를 조회합니다.")
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoomDetail(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomDetail(roomId));
    }

    @Operation(summary = "방 이미지 업로드", description = "특정 방에 이미지를 업로드합니다.")
    @PostMapping("/{roomId}/images")
    public ResponseEntity<List<RoomImageResponse>> uploadImages(
            @PathVariable Long roomId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(roomService.uploadRoomImages(roomId, files));
    }

    @Operation(summary = "방 정보 수정", description = "특정 방의 기본 정보를 수정합니다.")
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestBody RoomRequest request
    ) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, request));
    }

    @Operation(summary = "방 삭제", description = "특정 방을 삭제합니다.")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // 방 상세 조회 이거 수정해야함
//    @Operation(summary = "방 상세 조회", description = "사용자가 특정 방의 상세 정보를 조회합니다.")
//    @GetMapping("/rooms/{roomId}")
//    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {
//        RoomResponse response = roomService.getRoomById(roomId);
//        return ResponseEntity.ok(response);
//    }

    // 전체/검색 결과 방 목록
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getRoomList();
        return ResponseEntity.ok(rooms);
    }

    // 방 상세 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoomDetail(@PathVariable Long roomId) {
        RoomDetailResponse detail = roomService.getRoomDetail(roomId);
        return ResponseEntity.ok(detail);
    }









}
