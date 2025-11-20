package com.example.sharestay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(force = true)
@Data
public class RoomImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false, name = "image_id")
    private Long id;

    // 1:N
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(nullable = false)
    private String imageUrl;

    public RoomImage(Room room, String imageUrl) {
        this.room = room;
        this.imageUrl = imageUrl;
    }
}
