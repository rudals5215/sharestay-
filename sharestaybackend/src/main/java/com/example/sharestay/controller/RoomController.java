package com.example.sharestay.controller;

import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import com.example.sharestay.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 방 등록
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.ok(response);
    }

    // 메인화면 검색 (간단 버전)
    @GetMapping("/search/simple")
    public ResponseEntity<List<RoomResponse>> simpleSearch(
            @RequestParam String region) {
        return ResponseEntity.ok(roomService.simpleSearch(region));
    }

    // 상세 검색 (필터 포함)
    @GetMapping("/search/filter")
    public ResponseEntity<List<RoomResponse>> advancedSearch(
            @RequestParam String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String option
    ) {
        return ResponseEntity.ok(roomService.filterSearch(region, type, minPrice, maxPrice, option));
    }









}
