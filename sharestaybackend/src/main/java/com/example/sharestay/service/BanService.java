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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BanService {

    private final BanRepository banRepository;
    private final UserRepository userRepository;

    /**
     * 사용자를 정지 처리합니다.
     */
    @Transactional
    public BanResponse banUser(Long userId, BanRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        getActiveBanByUserId(userId).ifPresent(ban -> {
            throw new IllegalStateException("이미 정지된 사용자입니다.");
        });

        Ban ban = Ban.createBan(user, request.getReason(), request.getExpireAt(), request.getMemo());
        Ban savedBan = banRepository.save(ban);
        return BanResponse.from(savedBan);
    }

    /**
     * 사용자 단위로 정지를 해제합니다.
     */
    @Transactional
    public void unbanUser(Long userId) {
        Ban activeBan = getActiveBanByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자에 활성 정지 기록이 없습니다. ID: " + userId));
        activeBan.deactivate();
    }

    /**
     * 개별 정지 ID로 비활성화합니다.
     */
    @Transactional
    public void unbanByBanId(Long banId) {
        Ban ban = banRepository.findById(banId)
                .orElseThrow(() -> new EntityNotFoundException("해당 정지 ID를 찾을 수 없습니다. ID: " + banId));
        if (ban.isActive()) {
            ban.deactivate();
        }
    }

    /**
     * 활성 정지의 만료일/사유/메모를 수정합니다.
     */
    @Transactional
    public BanResponse updateBan(Long banId, BanRequest request) {
        Ban ban = banRepository.findById(banId)
                .orElseThrow(() -> new EntityNotFoundException("해당 정지 ID를 찾을 수 없습니다. ID: " + banId));
        if (!ban.isActive()) {
            throw new IllegalStateException("해제된 정지는 수정할 수 없습니다.");
        }
        ban.update(request.getReason(), request.getExpireAt(), request.getMemo());
        return BanResponse.from(ban);
    }

    /**
     * 특정 사용자의 정지 기록을 반환합니다.
     */
    public List<BanResponse> getBanHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId);
        }
        return banRepository.findByUserId(userId).stream()
                .map(BanResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 정지 기록을 반환합니다.
     */
    public List<BanResponse> getAllBans() {
        return banRepository.findAll().stream()
                .map(BanResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 만료되지 않은 활성 정지 기록을 조회합니다.
     * 만료되었는데 isActive가 true이면 자동으로 비활성화 후 빈 Optional을 반환합니다.
     */
    @Transactional
    public Optional<Ban> getActiveBanByUserId(Long userId) {
        Optional<Ban> activeBanOpt = banRepository.findActiveBanByUserId(userId);
        if (activeBanOpt.isEmpty()) return Optional.empty();

        Ban activeBan = activeBanOpt.get();
        LocalDateTime endDate = activeBan.getEndDate();
        if (endDate != null && endDate.isBefore(LocalDateTime.now())) {
            activeBan.deactivate();
            return Optional.empty();
        }
        return Optional.of(activeBan);
    }
}