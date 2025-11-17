package com.example.sharestay.dto;

import com.example.sharestay.entity.Host;
import com.example.sharestay.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {   // 방 등록, 수정 할 때 필요함
    private Long hostId; // 어떤 호스트가 등록하는지
    private String title;
    private double rentPrice;
    private String address;
    private String type;
    private double latitude;
    private double longitude;
    private int availabilityStatus;
    private String description;

    // DTO → Entity 변환 메서드 (방 등록, 수정 시에 코드가 훨씬 간결해짐)
    public Room toEntity(Host host) {
        return new Room(
                host,
                this.title,
                this.rentPrice,
                this.address,
                this.type,
                this.latitude,
                this.longitude,
                this.availabilityStatus,
                this.description
        );
    }

}


