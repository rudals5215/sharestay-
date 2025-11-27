package com.example.sharestay.service;

import com.example.sharestay.dto.MapDto;
import com.example.sharestay.entity.Room;
import com.example.sharestay.repository.RoomRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
public class MapService {

    private final RoomRepository roomRepository;

    // 전체 방 지도 정보 조회
    public List<MapDto> getAllRoomsForMap() {
        return roomRepository.findAll().stream()
                .map(room -> MapDto.builder()
                        .roomId(room.getId())
                        .title(room.getTitle())
                        .address(room.getAddress())
                        .type(room.getType())
                        .latitude(room.getLatitude())
                        .longitude(room.getLongitude())
                        .rentPrice(room.getRentPrice())
                        .availabilityStatus(room.getAvailabilityStatus())
                        .description(room.getDescription())
                        .build())
                .toList();
    }

    // 특정 좌표 기준 근처 방 조회 (거리 km 기준)
    public List<MapDto> getRoomsNearLocation(double userLat, double userLng, double radiusKm) {
        // DB에서 직접 필터링된 결과를 가져옵니다.
        List<Room> rooms = roomRepository.findRoomsNearLocation(userLat, userLng, radiusKm);

        return rooms.stream()
                .map(room -> MapDto.builder()
                        .roomId(room.getId())
                        .title(room.getTitle())
                        .address(room.getAddress())
                        .type(room.getType())
                        .latitude(room.getLatitude())
                        .longitude(room.getLongitude())
                        .rentPrice(room.getRentPrice())
                        .availabilityStatus(room.getAvailabilityStatus())
                        .description(room.getDescription())
                        .build())
                .toList();
    }

    // 지도 경계 기반 근처 방 조회 (사각형 기준)
    public List<MapDto> getRoomsInBoundary(
            double swLat, double swLng, double neLat, double neLng,
            double minPrice, double maxPrice, String type, List<String> options
    ) {
        // 1. Repository를 통해 위치와 가격 기준으로 1차 필터링된 방 목록을 가져옵니다.
        List<Room> rooms = roomRepository.findRoomsInBoundary(swLat, swLng, neLat, neLng, minPrice, maxPrice);

        // 2. 추가 필터(방 종류, 편의시설)를 적용합니다.
        List<Room> filteredRooms = rooms.stream()
                .filter(room -> type == null || type.isEmpty() || room.getType().equalsIgnoreCase(type))
                .filter(room -> {
                    if (options == null || options.isEmpty()) {
                        return true; // 편의시설 필터가 없으면 모두 통과
                    }
                    String description = room.getDescription() != null ? room.getDescription() : "";
                    // 모든 편의시설 옵션을 포함하는지 확인
                    return options.stream().allMatch(description::contains);
                })
                .collect(Collectors.toList());

        // 3. 필터링된 결과를 MapDto로 변환하여 반환합니다.
        return filteredRooms.stream()
                .map(room -> MapDto.builder()
                        .roomId(room.getId())
                        .title(room.getTitle())
                        .address(room.getAddress())
                        .type(room.getType())
                        .latitude(room.getLatitude())
                        .longitude(room.getLongitude())
                        .rentPrice(room.getRentPrice())
                        .availabilityStatus(room.getAvailabilityStatus())
                        .description(room.getDescription())
                        .build())
                .toList();
    }

    // 두 좌표 사이 거리 계산 (Haversine 공식)
    private double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371; // 지구 반지름 km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 특정 방 지도 정보 조회
    public MapDto getRoomMapInfo(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("해당 방이 없습니다."));
        return MapDto.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .address(room.getAddress())
                .type(room.getType())
                .latitude(room.getLatitude())
                .longitude(room.getLongitude())
                .rentPrice(room.getRentPrice())
                .availabilityStatus(room.getAvailabilityStatus())
                .description(room.getDescription())
                .build();
    }
}