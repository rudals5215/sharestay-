package com.example.sharestay.service;

import com.example.sharestay.dto.RoomDetailResponse;
import com.example.sharestay.dto.RoomImageResponse;
import com.example.sharestay.dto.RoomRequest;
import com.example.sharestay.dto.RoomResponse;
import com.example.sharestay.entity.Host;
import com.example.sharestay.entity.Room;
import com.example.sharestay.repository.HostRepository;
import com.example.sharestay.repository.RoomImageRepository;
import com.example.sharestay.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final HostRepository hostRepository;
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;
    private Path uploadBasePath;

    @PostConstruct
    void initUploadPath() throws IOException {
        uploadBasePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadBasePath);
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        Host host = hostRepository.findById(request.getHostId())
                .orElseThrow(() -> new IllegalArgumentException("Host not found"));

        // 지도 들고 올 건데 저 위도 경도는 대체 어떻게 해야하니.. 여기 있는 게 맞니..
//        Room room = new Room(   // RoomRequest 에 toEntity 가 있어서 기 코드가 없어도 됨.
//                host,
//                request.getTitle(),
//                request.getRentPrice(),
//                request.getAddress(),
//                request.getType(),
//                request.getLatitude(),
//                request.getLongitude(),
//                request.getAvailabilityStatus(),
//                request.getDescription()
//        );
        // new Room(...) 대신 DTO가 스스로 Entity로 변환
        Room saved = roomRepository.save(request.toEntity(host));

//        Room saved = roomRepository.save(room);  이것도 같이 사라짐
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> searchRooms(
            String region,
            String type,
            Double minPrice,
            Double maxPrice,
            String option
    ) {
        String normalizedRegion = region == null ? "" : region;
        List<Room> rooms = roomRepository.searchRooms(
                normalizedRegion,
                type,
                minPrice,
                maxPrice,
                option
        );

        return rooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        // Entity 내부 값 수정
//        room.setTitle(request.getTitle());
//        room.setRentPrice(request.getRentPrice());
//        room.setAddress(request.getAddress());
//        room.setType(request.getType());
//        room.setLatitude(request.getLatitude());
//        room.setLongitude(request.getLongitude());
//        room.setAvailabilityStatus(request.getAvailabilityStatus());
//        room.setDescription(request.getDescription());

        // 수정은 Host를 바꾸지 않음 (방 등록자 고정)
        room.update(request);  // RoomEntity에 update() 만듦

        Room updated = roomRepository.save(room);
        return toResponse(updated);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
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
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        List<String> imageUrls = roomImageRepository.findByRoomId(roomId).stream()
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

    @Transactional
    public List<RoomImageResponse> uploadRoomImages(Long roomId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        Path roomDir = uploadBasePath.resolve("rooms").resolve(roomId.toString());
        try {
            Files.createDirectories(roomDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create upload directory", ex);
        }

        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> storeImage(room, file, roomDir))
                .collect(Collectors.toList());
    }

    private RoomImageResponse storeImage(Room room, MultipartFile file, Path roomDir) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "_" + originalName;
        Path targetPath = roomDir.resolve(filename);
        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store image file", ex);
        }

        String publicUrl = "/uploads/rooms/" + room.getId() + "/" + filename;
        var roomImage = new com.example.sharestay.entity.RoomImage();
        roomImage.setRoom(room);
        roomImage.setImageUrl(publicUrl);
        roomImage = roomImageRepository.save(roomImage);

        return RoomImageResponse.builder()
                .id(roomImage.getId())
                .imageUrl(roomImage.getImageUrl())
                .build();
    }

    private RoomResponse toResponse(Room room) {
        List<RoomImageResponse> images = roomImageRepository.findByRoomId(room.getId()).stream()
                .map(img -> RoomImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return RoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .rentPrice(room.getRentPrice())
                .address(room.getAddress())
                .type(room.getType())
                .availabilityStatus(room.getAvailabilityStatus())
                .description(room.getDescription())
                .images(images.isEmpty() ? Collections.emptyList() : images)
                .shareLinkUrl(room.getShareLink() != null ? room.getShareLink().getLinkUrl() : null)
                .build();
    }
}
