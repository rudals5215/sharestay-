package com.example.sharestay.dto;

import com.example.sharestay.entity.Host;
import com.example.sharestay.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {   // 방 등록, 수정 할 때 필요함
    private Long hostId;
    private String title;
    private double rentPrice;
    private String address;
    private String type;

    private int availabilityStatus;
    private String description;

    private double latitude;
    private double longitude;

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
    //    private List<MultipartFile> images; // 여러 이미지 업로드 가능
    /*
        📌 이 필드는 프론트에서 FormData로 전송해야 합니다.
예: axios.post("/api/rooms", formData, { headers: { "Content-Type": "multipart/form-data" } })
     */

}


