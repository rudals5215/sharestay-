package com.example.sharestay.controller;

import com.example.sharestay.entity.Favorite;
import com.example.sharestay.entity.Room;
import com.example.sharestay.entity.User;
import com.example.sharestay.repository.RoomRepository;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.service.FavoriteService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/favorites")
@AllArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;


    @PostMapping("/toggle")
    public ResponseEntity<String> toggleFavorite(@RequestParam Long userId, @RequestParam Long roomId) {
        User user = userRepository.findById(userId).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();

        Optional<Favorite> favoriteOpt = favoriteService.getFavorite(user, room);

        if (favoriteOpt.isPresent()) {
            favoriteService.deleteFavorite(user, room);
            return ResponseEntity.ok("즐겨찾기에서 삭제되었습니다.");
        } else {
            favoriteService.addFavorite(user, room);
            return ResponseEntity.ok("즐겨찾기에 추가되었습니다.");
        }
    }
}
