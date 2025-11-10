package com.example.sharestay.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomResponse {
    private Long id;
    private String title;
    private double rentPrice;
    private String address;
    private String type;
    private int availabilityStatus;
    private String description;
}
