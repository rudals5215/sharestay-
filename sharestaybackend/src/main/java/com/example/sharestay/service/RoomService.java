// RoomService.java
package com.example.sharestay.service;

import com.example.sharestay.domain.Host;
import com.example.sharestay.domain.HostRepository;
import com.example.sharestay.domain.Room;
import com.example.sharestay.domain.RoomRepository;
import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 검색, 필터만 // Controller에서 메인 간단 검색 / 상세 검색으로 나눔
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HostRepository hostRepository;

    // 방 등록
    @Transactional  // DB 트랜잭션 제어 (내부에서 여러 DB 작업 실행 -> 예외 없이 정상 종료 → commit()) 예외를 안에서 잡는 건 x, service 로직에서만 사용하는 것을 추천
    public RoomResponse createRoom(RoomRequest request) {
        Host host = hostRepository.findById(request.getHostId())
                .orElseThrow(() -> new IllegalArgumentException("Host not found"));

        // 지도 들고 올 건데 저 위도 경도는 대체 어떻게 해야하니.. 여기 있는 게 맞니..
        Room room = new Room(
                host,
                request.getTitle(),
                request.getRentPrice(),
                request.getAddress(),
                request.getType(),
                request.getLatitude(),
                request.getLongitude(),
                request.getAvailabilityStatus(),
                request.getDescription()
        );

        Room saved = roomRepository.save(room);

        return RoomResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .rentPrice(saved.getRentPrice())
                .address(saved.getAddress())
                .type(saved.getType())
                .availabilityStatus(saved.getAvailabilityStatus())
                .description(saved.getDescription())
                .build();

        // return toResponse(saved);
    }

    // 간단 검색 (메인 화면)
    @Transactional(readOnly = true)
    public List<RoomResponse> simpleSearch(String regionKeyword) {
        // 타입/가격/편의시설은 전부 null
        List<Room> rooms = roomRepository.searchRooms(regionKeyword, null, null, null, null);

        return rooms.stream()
                .map(room -> RoomResponse.builder()
                        .id(room.getId())
                        .title(room.getTitle())
                        .rentPrice(room.getRentPrice())
                        .address(room.getAddress())
                        .type(room.getType())
                        .availabilityStatus(room.getAvailabilityStatus())
                        .description(room.getDescription())
                        .build())
                .collect(Collectors.toList());
    }


    // 상세 검색 (필터 페이지)  Repository에 JPQL 짜놓음
    @Transactional(readOnly = true)
    public List<RoomResponse> filterSearch(String region, String type,
                                             Double minPrice, Double maxPrice,
                                             String amenity) {
        List<Room> rooms = roomRepository.searchRooms(region, type, minPrice, maxPrice, amenity);

        return rooms.stream()
                .map(room -> RoomResponse.builder()
                        .id(room.getId())
                        .title(room.getTitle())
                        .rentPrice(room.getRentPrice())
                        .address(room.getAddress())
                        .type(room.getType())
                        .availabilityStatus(room.getAvailabilityStatus())
                        .description(room.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    // ✅ 2. 검색 및 필터  (위에껀 상세랑 심플이랑 나뉘어져 있고 RESTful로 하면 그냥 합치면 된다고 하는데...?
//    @Transactional(readOnly = true)
//    public List<RoomResponse> advancedSearch(String region, String type,
//                                             Double minPrice, Double maxPrice,
//                                             String option) {
//        List<Room> rooms = roomRepository.searchRooms(region, type, minPrice, maxPrice, option);
//        return rooms.stream()
//                .map(r -> new RoomResponse(
//                        r.getId(),
//                        r.getTitle(),
//                        r.getRentPrice(),
//                        r.getAddress(),
//                        r.getType(),
//                        r.getAvailabilityStatus(),
//                        r.getDescription(),
//                        r.getOption()
//                ))
//                .collect(Collectors.toList());
//    }





}
