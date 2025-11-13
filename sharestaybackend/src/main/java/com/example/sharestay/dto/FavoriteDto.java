package com.example.sharestay.dto;

import com.example.sharestay.entity.Favorite;
import com.example.sharestay.entity.RoomImage;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FavoriteDto {
    private Long roomId;       // DB 참조용
    private String roomName;   // 프론트 표시용
    private List<RoomImage> roomImg;    // 프론트 표시용
    private LocalDateTime likedAt;

    public FavoriteDto(Favorite favorite) {
        this.roomId = favorite.getRoom().getId();
        this.roomName = favorite.getRoom().getTitle();
        this.roomImg = favorite.getRoom().getRoomImages();
        this.likedAt = favorite.getLikedAt();
    }
}
