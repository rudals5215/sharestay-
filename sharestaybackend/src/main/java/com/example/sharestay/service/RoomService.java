// RoomService.java
package com.example.sharestay.service;

import com.example.sharestay.dto.RoomDetailResponse;
import com.example.sharestay.entity.Host;
import com.example.sharestay.entity.RoomImage;
import com.example.sharestay.entity.User;
import com.example.sharestay.repository.HostRepository;
import com.example.sharestay.entity.Room;
import com.example.sharestay.repository.RoomImageRepository;
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
    private final RoomImageRepository roomImageRepository;
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
        return toResponse(saved);
    }

    // 방 검색
    // 검색 service 로직은 하나로 작성하고 controller에서 나눌 것임
    @Transactional(readOnly = true)
    public List<RoomResponse> searchRooms(
            String region, String type,
            Double minPrice, Double maxPrice,
            String option
    ) {
        List<Room> rooms = roomRepository.searchRooms(region, type, minPrice, maxPrice, option);

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

    // 방 검색  (단일 조회용으로만 쓰이는데, 필터/검색 결과에서는 여러 개의 방을 리스트로 가져와야 하므로 이 메서드는 필요없음
//    @Transactional(readOnly = true)
//    public RoomResponse getRoomById(Long roomId) {
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
//
//        return toResponse(room);
//    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomList() {
        return roomRepository.findAll()   // 전체 조회 or 나중에 필터 조건 추가 가능
                .stream()
                .map(this::toResponse)    // Room → RoomResponse 변환
                .collect(Collectors.toList());
    }

    // 방 상세보기
    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // 이미지 URL을 방 이미지 엔티티/리포지토리에서 가져왔다고 가정
        List<String> imageUrls = roomImageRepository.findByRoomId(roomId)
                .stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());

        String shareLinkUrl = room.getShareLink() != null
                ? room.getShareLink().getLinkUrl()
                : null;

        return new RoomDetailResponse(
                room.getId(),
                room.getTitle(),
                room.getRentPrice(),
                room.getAddress(),
                room.getType(),
                room.getAvailabilityStatus(),
                room.getDescription(),
                room.getLatitude(),
                room.getLongitude(),
                imageUrls,
                shareLinkUrl
        );
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
