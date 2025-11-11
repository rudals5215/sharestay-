package com.example.sharestay.controller;

import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import com.example.sharestay.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 방 등록
    @PostMapping("rooms")
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.ok(response);
    }

    // 메인화면 검색 (간단 버전)
    @GetMapping("/main")
    public ResponseEntity<List<RoomResponse>> mainSearch(
            @RequestParam(required = false) String region) {
        List<RoomResponse> result = roomService.searchRooms(region, null, null, null, null);
        return ResponseEntity.ok(result);

    }

    // 상세 검색 (필터 포함)
    @GetMapping("/rooms/filter")
    public ResponseEntity<List<RoomResponse>> filterSearch(
            @RequestParam String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String option
    ) {
        List<RoomResponse> result = roomService.searchRooms(region, type, minPrice, maxPrice, option);
        return ResponseEntity.ok(result);
    }









}
