package com.example.sharestay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
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
    private final String title;

    @Column(nullable = false)
    private final double rentPrice;   // 룸쉐어링 할 때의 가격

    @Column(nullable = false)
    private final String address;

    @Column(nullable = false)
    private final String type;  // 원룸인지 투룸인지

    @Column
    private double latitude;  // 위도

    @Column
    private double longitude;     // 경도

    @Column(nullable = false)
    private final int availabilityStatus;   // 수용 인원

    @Column(nullable = false)
    private final String description;  // 상세 설명 같은 거..?

    // Room 이 저장될 때 함께 저장, 삭제 될 때 함께 삭제
    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShareLink shareLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private final Host host;


    // final에 Required 조합이라 RoomService에서 객체 생성하려면 이거 있어야 하는데... 더 간단하게 쓸 수는 없는 거니
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


}
