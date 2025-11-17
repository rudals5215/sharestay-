package com.example.sharestay.dto;

import com.example.sharestay.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor      // JSON 역직렬화, 기본 생성자 필요할 수 있음
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String title;
    private double rentPrice;
    private String address;
    private String type;
    private int availabilityStatus;
    private String description;
}
