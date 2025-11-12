// RoomService.java
package com.example.sharestay.service;

import com.example.sharestay.dto.RoomDetailResponse;
import com.example.sharestay.entity.Host;
import com.example.sharestay.entity.RoomImage;
import com.example.sharestay.repository.HostRepository;
import com.example.sharestay.entity.Room;
import com.example.sharestay.repository.RoomImageRepository;
import com.example.sharestay.repository.RoomRepository;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

// 검색, 필터만 // Controller에서 메인 간단 검색 / 상세 검색으로 나눔
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final HostRepository hostRepository;
    private final UserRepository userRepository;

    // 방 등록
    @Transactional  // DB 트랜잭션 제어 (내부에서 여러 DB 작업 실행 -> 예외 없이 정상 종료 → commit()) 예외를 안에서 잡는 건 x, service 로직에서만 사용하는 것을 추천
    public RoomResponse createRoom(RoomRequest request) {
        Host host = resolveHost(request);

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
        String normalizedRegion = region == null ? "" : region;
        List<Room> rooms = roomRepository.searchRooms(
                normalizedRegion,
                type,
                minPrice,
                maxPrice,
                amenity
        );

        return rooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        return toResponse(room);
    }

    private Host resolveHost(RoomRequest request) {
        if (request.getHostId() == null) {
            throw new IllegalArgumentException("호스트 식별자가 필요합니다.");
        }

        return hostRepository.findById(request.getHostId())
                .orElseGet(() -> resolveHostByUserId(request.getHostId()));
    }

    private Host resolveHostByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        return hostRepository.findByUser(user)
                .orElseGet(() -> {
                    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                        throw new IllegalArgumentException("호스트 프로필이 존재하지 않습니다. 호스트 전환을 먼저 완료해 주세요.");
                    }
                    Host adminHost = Host.builder()
                            .user(user)
                            .introduction("관리자 자동 생성 호스트")
                            .termsAgreed(true)
                            .build();
                    return hostRepository.save(adminHost);
                });
    }

    private RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .rentPrice(room.getRentPrice())
                .address(room.getAddress())
                .type(room.getType())
                .availabilityStatus(room.getAvailabilityStatus())
                .description(room.getDescription())
                .build();
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return toResponse(room);
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // 이미지 URL을 방 이미지 엔티티/리포지토리에서 가져왔다고 가정
        List<String> imageUrls = roomImageRepository.findByRoomId(roomId)
                .stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());

        String shareLinkUrl = null;
        if (room.getShareLink() != null) {
            shareLinkUrl = room.getShareLink().getLinkUrl();
        }

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
