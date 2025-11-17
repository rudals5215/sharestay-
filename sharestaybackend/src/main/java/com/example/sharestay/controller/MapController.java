package com.example.sharestay.controller;

import com.example.sharestay.dto.MapDto;
import com.example.sharestay.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    // 전체 방 지도 정보
    @GetMapping("/rooms")
    public List<MapDto> getAllRooms() {
        return mapService.getAllRoomsForMap();
    }

    // 근처 방 조회
    @GetMapping("/rooms/near")
    public List<MapDto> getRoomsNear(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radiusKm) {
        return mapService.getRoomsNearLocation(lat, lng, radiusKm);
    }

    // 특정 방 지도 정보
    @GetMapping("/room/{roomId}")
    public MapDto getRoomMapInfo(@PathVariable Long roomId) {
        return mapService.getRoomMapInfo(roomId);
    }
}