// RoomService.java
package com.example.sharestay.service;

import com.example.sharestay.dto.RoomDetailResponse;
//import com.example.sharestay.dto.RoomImageResponse;
import com.example.sharestay.entity.Host;
import com.example.sharestay.entity.RoomImage;
import com.example.sharestay.entity.User;
import com.example.sharestay.repository.FavoriteRepository;
import com.example.sharestay.repository.HostRepository;
import com.example.sharestay.entity.Room;
import com.example.sharestay.repository.RoomImageRepository;
import com.example.sharestay.repository.RoomRepository;
import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 검색, 필터만 // Controller에서 메인 간단 검색 / 상세 검색으로 나눔
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final HostRepository hostRepository;
    private final FirebaseService firebaseService;  // Firebase 업로드용
    private final FavoriteRepository favoriteRepository;

    // 방 등록
    @Transactional  // DB 트랜잭션 제어 (내부에서 여러 DB 작업 실행 -> 예외 없이 정상 종료 → commit()) 예외를 안에서 잡는 건 x, service 로직에서만 사용하는 것을 추천
    public RoomResponse createRoom(RoomRequest request, List<MultipartFile> files) {
        Host host = hostRepository.findById(request.getHostId())
                .orElseThrow(() -> new IllegalArgumentException("Host not found"));

        Room room = request.toEntity(host);

        // 이미지 업로드 및 RoomImage 엔티티 생성
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String imageUrl = firebaseService.uploadFile(file);

                RoomImage image = new RoomImage(room, imageUrl);
                room.getRoomImages().add(image); // room의 이미지 목록에 추가
            }
        }

        Room savedRoom = roomRepository.save(room); // room을 저장하면 roomImages도 함께 저장됨

        return toResponse(savedRoom);


//        // DTO 변환
//        List<RoomImageResponse> imageResponses = saved.getRoomImages().stream()
//                .map(img -> new RoomImageResponse(img.getId(), img.getImageUrl()))
//                .collect(Collectors.toList());
//
//        String shareLinkUrl = saved.getShareLink() != null
//                ? saved.getShareLink().getLinkUrl()
//                : null;

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
                .map(this::toResponse)   // 공통 변환 메서드 사용
                .collect(Collectors.toList());
    }

    // 방 수정
    @Transactional
    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // 수정은 Host를 바꾸지 않음 (방 등록자 고정)
        room.update(request);  // RoomEntity에 update() 만듦

        Room updated = roomRepository.save(room);
        return toResponse(updated);
    }

    // 방 삭제
    @Transactional
    public void deleteRoom(Long roomId) {
        // 1. Favorite 먼저 삭제
        favoriteRepository.deleteByRoomId(roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        roomRepository.delete(room);
    }

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

        // RoomImageRepository 이용해서 이미지 URL 조회
        List<String> imageUrls = roomImageRepository.findByRoomId(roomId)
                .stream()
                .map(RoomImage::getImageUrl)
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

        List<String> imageUrls = room.getRoomImages()
                .stream()
                .map(RoomImage::getImageUrl)
                .toList();

        return new RoomResponse(
                room.getId(),
                room.getTitle(),
                room.getRentPrice(),
                room.getAddress(),
                room.getType(),
                room.getAvailabilityStatus(),
                room.getDescription(),
                imageUrls,
                null
        );
    }
}
