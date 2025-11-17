package com.example.sharestay.entity;

import com.example.sharestay.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ban")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ban {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false, name = "ban_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)//-> 지연로딩// 한 유저가 여러번 정지 당할 수 있으니까
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String reason; // 정지 사유

    @Column(name = "banned_at", nullable = false)
    private LocalDateTime bannedAt; // 정지된 시간

    @Column(name = "end_date")
    private LocalDateTime endDate; //정지가 만료되는 시간 (null일 경우 영구 정지를 의미)

    @Column(name = "is_active")
    private boolean isActive; //현재 정지가 유효한 상태인지 여부 (true: 활성, false: 비활성/해제됨)

    @Column(columnDefinition = "TEXT")
    private String memo; //정지에 대한 관리자용 추가 메모

    // 외부에서 직접 생성자 호출을 막고, 정적 팩토리 메서드를 사용하도록 유도합니다.
    private Ban(User user, String reason, LocalDateTime endDate, String memo) {
        this.user = user;
        this.reason = reason;
        this.endDate = endDate;
        this.memo = memo;
    }

    //Ban 엔티티를 생성하는 정적 팩토리 메서드 (User 객체를 받도록 수정)
    public static Ban createBan(User user, String reason, LocalDateTime endDate, String memo) {
        return new Ban(user, reason, endDate, memo);
    }

    @PrePersist
    protected void onCreate() {
        this.bannedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // 정지를 비활성 상태로 변경합니다.
    public void deactivate() {
        this.isActive = false;
    }
}
