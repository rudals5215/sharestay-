package com.example.sharestay.repository;

import java.util.List;

public interface RoomImageRepository <RoomImage, Long> {
    List<RoomImage> findByRoomId(Long roomId);
}
