package com.example.sharestay.repository;

import com.example.sharestay.entity.Favorite;
import com.example.sharestay.entity.Room;
import com.example.sharestay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserAndRoom(User user, Room room);

    Optional<Favorite> findByUserAndRoom(User user, Room room);

    List<Favorite> findAllByUser(User user);

    void deleteByRoomId(Long roomId);
}
