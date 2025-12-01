package com.example.sharestay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String preferredGender;
    private String preferredAge;
    private Integer totalMembers;
    private List<String> lifestyle;
    private List<String> options;
    private double latitude;
    private double longitude;
    private List<String> imageUrls;
    private String shareLinkUrl;
    private Long hostId;
    private Long hostUserId;
}
