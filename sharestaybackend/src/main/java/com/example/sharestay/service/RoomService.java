// RoomService.java
package com.example.sharestay.service;

import com.example.sharestay.entity.Host;
import com.example.sharestay.repository.HostRepository;
import com.example.sharestay.entity.Room;
import com.example.sharestay.repository.RoomRepository;
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

        // 이거 builder 안 쓰는 버전으로 만들고 싶음
//        return RoomResponse.builder()
//                .id(saved.getId())
//                .title(saved.getTitle())
//                .rentPrice(saved.getRentPrice())
//                .address(saved.getAddress())
//                .type(saved.getType())
//                .availabilityStatus(saved.getAvailabilityStatus())
//                .description(saved.getDescription())
//                .build();

         return toResponse(saved);
    }

    // 방 검색
    // 검색 service 로직은 하나로 작성하고 controller에서 나눌 것임
    @Transactional(readOnly = true)
    public List<RoomResponse> searchRooms(
            String region, String type,
            Double minPrice, Double maxPrice,
            String amenity
    ) {
        List<Room> rooms = roomRepository.searchRooms(region, type, minPrice, maxPrice, amenity);

        return rooms.stream()
                .map(room -> new RoomResponse(
                        room.getId(),
                        room.getTitle(),
                        room.getRentPrice(),
                        room.getAddress(),
                        room.getType(),
                        room.getAvailabilityStatus(),
                        room.getDescription()
                ))
                .collect(Collectors.toList());
    }

    // 방 수정
    @Transactional
    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Entity 내부 값 수정 (dirty checking)
        room.setTitle(request.getTitle());
        room.setRentPrice(request.getRentPrice());
        room.setAddress(request.getAddress());
        room.setType(request.getType());
        room.setLatitude(request.getLatitude());
        room.setLongitude(request.getLongitude());
        room.setAvailabilityStatus(request.getAvailabilityStatus());
        room.setDescription(request.getDescription());

        // @Transactional 덕분에 save() 없이 자동 update됨
        return toResponse(room);
    }

    // 방 삭제
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        roomRepository.delete(room);
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return toResponse(room);
    }


    // 공통 변환 메서드 (Entity → DTO)
    private RoomResponse toResponse(Room room) {
    return new RoomResponse(
            room.getId(),
            room.getTitle(),
            room.getRentPrice(),
            room.getAddress(),
            room.getType(),
            room.getAvailabilityStatus(),
            room.getDescription()
    );





    }






}
