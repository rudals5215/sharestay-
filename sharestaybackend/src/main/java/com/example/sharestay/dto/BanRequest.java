package com.example.sharestay.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BanRequest {
    private String reason;
    private LocalDateTime expireAt; // 영구 정지가 아닐 경우 설정
    private String memo;
}