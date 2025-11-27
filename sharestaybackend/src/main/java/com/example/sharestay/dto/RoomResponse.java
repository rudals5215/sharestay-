package com.example.sharestay.dto;

import java.util.List;

import com.example.sharestay.entity.Room;
import com.example.sharestay.entity.RoomImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String title;
    private double rentPrice;
    private String address;
    private String type;
    private int availabilityStatus;
    private String description;
    private String options;
    private List<RoomImageResponse> images;
    private String shareLinkUrl;
}
