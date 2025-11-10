package com.example.sharestay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    private Long hostId; // 어떤 호스트가 등록하는지
    private String title;
    private double rentPrice;
    private String address;
    private String type;
    private double latitude;
    private double longitude;
    private int availabilityStatus;
    private String description;
}
