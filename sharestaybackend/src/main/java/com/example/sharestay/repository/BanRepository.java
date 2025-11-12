package com.example.sharestay.repository;

import com.example.sharestay.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BanRepository extends JpaRepository<Ban, Long> {
    // 특정 사용자의 모든 정지 기록 조회
    List<Ban> findByUser_Id(Long userId);
    // 모든 활성 상태의 정지 기록 조회
    List<Ban> findByIsActiveTrue();
    // 특정 사용자의 활성 상태 정지 기록 조회
    Optional<Ban> findByUser_IdAndIsActiveTrue(Long userId);
}
