package com.example.sharestay.service;

import com.example.sharestay.entity.Favorite;
import com.example.sharestay.repository.FavoriteRepository;
import com.example.sharestay.entity.Room;
import com.example.sharestay.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    // 즐겨찾기 추가
    public Favorite addFavorite(User user, Room room) {
        return favoriteRepository.findByUserAndRoom(user, room)
                .orElseGet(() -> {
                    Favorite favorite = new Favorite();
                    favorite.setUser(user);
                    favorite.setRoom(room);
                    return favoriteRepository.save(favorite);
                });
    }

    // 즐겨찾기 삭제
    public void deleteFavorite(User user, Room room) {
        favoriteRepository.findByUserAndRoom(user, room)
                .ifPresent(favoriteRepository::delete);
    }

    // 즐겨찾기 단건 조회
    public Optional<Favorite> getFavorite(User user, Room room) {
        return favoriteRepository.findByUserAndRoom(user, room);
    }

    // 사용자별 즐겨찾기 전체 조회
    public List<Favorite> getFavoritesByUser(User user) {
        return favoriteRepository.findAllByUser(user);
    }
}
