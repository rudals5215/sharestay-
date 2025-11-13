package com.example.sharestay.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
//@Builder
public class RoomResponse {
    private Long id;
    private String title;
    private double rentPrice;
    private String address;
    private String type;
    private int availabilityStatus;
    private String description;
    private List<RoomImageResponse> images;  // 추가
    private String shareLinkUrl;  // 추가
}
