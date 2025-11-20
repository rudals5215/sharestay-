package com.example.sharestay.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailResponse {
    private Long id;
    private String title;
    private double rentPrice;
    private String address;
    private String type;
    private int availabilityStatus;
    private String description;
    private double latitude;
    private double longitude;

    // 이미지 URL 리스트
    private List<String> imageUrls;

    // 공유 링크 URL
    private String shareLinkUrl;
}
