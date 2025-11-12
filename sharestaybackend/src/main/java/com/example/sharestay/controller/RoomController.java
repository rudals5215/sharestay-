package com.example.sharestay.controller;

import com.example.sharestay.dto.RoomDetailResponse;
import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import com.example.sharestay.entity.Room;
import com.example.sharestay.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room API", description = "방 등록 / 수정 / 삭제 / 검색 관련 API")
public class RoomController {
    private final RoomService roomService;

    // 방 등록
    @Operation(summary = "방 등록", description = "호스트가 새로운 방을 등록합니다.")
    @PostMapping("rooms")
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
//        return ResponseEntity.ok(response);  // 이건 200 ok
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//        201 Created 상태 코드를 반환하는 것이 REST API 설계 원칙에 더 부합
    }

    // 메인화면 검색 (간단 버전)
    @Operation(summary = "메인 방 검색", description = "메인에서 간단하게 검색합니다.")
    @GetMapping("/main")
    public ResponseEntity<List<RoomResponse>> mainSearch(
            @RequestParam(required = false) String region) {
        List<RoomResponse> result = roomService.searchRooms(region, null, null, null, null);
        return ResponseEntity.ok(result);

    }

    // 상세 검색 (필터 포함)
    @Operation(summary = "필터 방 검색", description = "지역, 타입, 가격, 편의시설 조건으로 방을 검색합니다.")
    @GetMapping("/rooms/filter")
    public ResponseEntity<List<RoomResponse>> filterSearch(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String option
    ) {
        // 파라미터가 없으면 전체조회, 있으면 조건부 검색
        List<RoomResponse> rooms = roomService.searchRooms(region,type,minPrice,maxPrice,option);
        return ResponseEntity.ok(rooms);
    }

    // 방 수정
    @Operation(summary = "방 수정", description = "기존 방 정보를 수정합니다.")
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestBody RoomRequest request
    ) {
        RoomResponse response = roomService.updateRoom(roomId, request);
        return ResponseEntity.ok(response);
    }

    // 방 삭제 (호스트 용)
    @Operation(summary = "방 삭제", description = "특정 방 ID로 방을 삭제합니다.")
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
