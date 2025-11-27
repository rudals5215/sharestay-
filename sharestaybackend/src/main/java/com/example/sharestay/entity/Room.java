package com.example.sharestay.entity;

import com.example.sharestay.dto.RoomRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Entity
@NoArgsConstructor(force = true)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false, name = "room_id")
    private Long id;

    // host랑 관계 (host 클래스 만든 곳이랑 머지후 주석 풀기
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "host_id")
//    private final Host host;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private double rentPrice;   // 룸쉐어링 할 때의 가격

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String type;  // 원룸인지 투룸인지

    @Column
    private double latitude;  // 위도

    @Column
    private double longitude;     // 경도

    @Column(nullable = false)
    private int availabilityStatus;   // 수용 인원

    @Column(nullable = false)
    private String description;  // 상세 설명 같은 거..?

    // Room 이 저장될 때 함께 저장, 삭제 될 때 함께 삭제
    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShareLink shareLink;

    public void setShareLink(ShareLink shareLink) {
        this.shareLink = shareLink;
        if (shareLink != null && shareLink.getRoom() != this) {
            shareLink.setRoom(this);  // 주인 쪽도 맞춰줌
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private Host host;

    @JsonIgnoreProperties("room")    // RoomImage 클래스의 필드명이 room이기 때문
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> roomImages = new ArrayList<>();

    /*
        List<RoomImage> roomImages 초기화를 new ArrayList<>() 로 해두면 NPE 방지됨.

RoomImage와 ShareLink의 cascade 관계는 명확히 관리되지만, 순환참조 방지를 위해 @ToString.Exclude 추가 추천.
     */


    public Room(Host host, String title, double rentPrice, String address, String type,
                double latitude, double longitude, int availabilityStatus, String description) {
        this.host = host;
        this.title = title;
        this.rentPrice = rentPrice;
        this.address = address;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availabilityStatus = availabilityStatus;
        this.description = description;
    }

    // 업데이트 메서드 (RoomRequest 기반)
    public void update(RoomRequest request) {
        this.title = request.getTitle();
        this.rentPrice = request.getRentPrice();
        this.address = request.getAddress();
        this.type = request.getType();
        this.latitude = request.getLatitude();
        this.longitude = request.getLongitude();
        this.availabilityStatus = request.getAvailabilityStatus();
        this.description = request.getDescription();
    }
    // update()로, 수정할 때 매번 필드를 하나하나 꺼내서 set 할 필요가 없음.
    // room.update(request) 하면 끝

    // RoomImage 더미데이터 때문에 만들어 놓은 거
    public void addRoomImage(String imageUrl) {
        RoomImage image = new RoomImage();  // Lombok @Data / @NoArgsConstructor 있으니까 기본 생성자 사용
        image.setImageUrl(imageUrl);
        image.setRoom(this);               // 역방향 연관관계 세팅
        this.roomImages.add(image);        // 컬렉션에 추가
    }
}
