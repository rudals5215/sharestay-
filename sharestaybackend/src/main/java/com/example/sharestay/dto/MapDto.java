package com.example.sharestay.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MapDto {
    private Long roomId;              // DB의 Room 고유번호
    private String title;             // 방 제목
    private String address;           // 방 주소 (지번/도로명)
    private String type;              // 원룸/투룸/오피스텔 등 방 종류
    private double latitude;          // 위도
    private double longitude;         // 경도
    private double rentPrice;         // 월세 or 전세 가격
    private int availabilityStatus;   // 예약가능 / 거래완료 상태
    private String description;       // 방 한줄 설명
    private Long hostId;              // host ID
}
