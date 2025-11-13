package com.example.sharestay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomImageResponse {
    private Long id;
    private String imageUrl;
}
