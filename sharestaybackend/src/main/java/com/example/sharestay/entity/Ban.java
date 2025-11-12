package com.example.sharestay.entity;

import com.example.sharestay.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ban")
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ban {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_id", nullable = false, updatable = false)
    private Long id;

    // 사용자와 다대일 관계 (1명의 사용자가 여러 번 정지될 수 있음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "banned_at", nullable = false)
    private LocalDateTime bannedAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "admin_id")
    private Long adminId;  // (optional) 관리자 계정 ID

    @Column(columnDefinition = "TEXT")
    private String memo;

    @PrePersist
    protected void onCreate() {
        this.bannedAt = LocalDateTime.now();
        this.isActive = true;
    }

    /**
     * 정지를 비활성 상태로 변경합니다.
     */
    public void deactivate() {
        this.isActive = false;
    }
}
