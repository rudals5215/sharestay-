package com.example.sharestay.dto;

import com.example.sharestay.entity.Ban;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BanResponse {
    private final Long banId;
    private final Long userId;
    private final String reason;
    private final LocalDateTime bannedAt;
    private final LocalDateTime endDate;
    private final boolean isActive;
    private final String memo;

    public BanResponse(Ban ban) {
        this.banId = ban.getId();
        this.userId = ban.getUser().getId();
        this.reason = ban.getReason();
        this.bannedAt = ban.getBannedAt();
        this.endDate = ban.getEndDate();
        this.isActive = ban.isActive();
        this.memo = ban.getMemo();
    }

    public static BanResponse from(Ban ban) {
        return new BanResponse(ban);
    }
}