package com.example.sharestay.service;

import com.example.sharestay.dto.BanRequest;
import com.example.sharestay.dto.BanResponse;
import com.example.sharestay.entity.Ban;
import com.example.sharestay.entity.User;
import com.example.sharestay.repository.BanRepository;
import com.example.sharestay.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BanService {

    private final BanRepository banRepository;
    private final UserRepository userRepository; // UserRepository가 필요합니다.

    /**
     * 사용자를 정지 처리합니다.
     * @param userId 정지할 사용자 ID
     * @param request 정지 요청 정보
     * @return 생성된 정지 정보
     */
    @Transactional
    public BanResponse banUser(Long userId, BanRequest request) {
        // 1. 사용자가 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        // 2. 이미 활성 상태의 정지 기록이 있는지 확인
        banRepository.findActiveBanByUserId(userId).ifPresent(ban -> {
            throw new IllegalStateException("이미 정지된 사용자입니다.");
        });

        // 3. Ban 엔티티 생성
        Ban ban = Ban.createBan(user, request.getReason(), request.getExpireAt(), request.getMemo());

        // 4. 저장 후 DTO로 변환하여 반환
        Ban savedBan = banRepository.save(ban);
        return BanResponse.from(savedBan);
    }
    /**
     * 사용자 정지를 해제합니다.
     * @param userId 정지를 해제할 사용자 ID
     */
    @Transactional
    public void unbanUser(Long userId) {
        // 1. 활성 상태의 정지 기록을 조회
        Ban activeBan = banRepository.findActiveBanByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자에 대한 활성 정지 기록이 없습니다. ID: " + userId));

        // 2. 정지 기록을 비활성화
        activeBan.deactivate();
    }

    /**
     * 특정 사용자의 모든 정지 기록을 조회합니다.
     * @param userId 사용자 ID
     * @return 정지 기록 리스트
     */
    public List<BanResponse> getBanHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId);
        }
        return banRepository.findByUserId(userId).stream()
                .map(BanResponse::from)
                .collect(Collectors.toList());
    }
}