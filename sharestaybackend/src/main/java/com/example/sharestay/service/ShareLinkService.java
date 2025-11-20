package com.example.sharestay.service;

import com.example.sharestay.entity.Room;
import com.example.sharestay.repository.RoomRepository;
import com.example.sharestay.entity.ShareLink;
import com.example.sharestay.repository.ShareLinkRepository;
import com.example.sharestay.dto.ShareLinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShareLinkService {
    private final RoomRepository roomRepository;
    private final ShareLinkRepository shareLinkRepository;

    @Transactional  // 스프링프레임 워크껄로 import
    public ShareLinkResponse createShareLink(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (room.getShareLink() != null) {
//            return room.getShareLink(); // 이미 존재하면 재사용

            return ShareLinkResponse.builder()
                    .linkUrl(room.getShareLink().getLinkUrl())
                    .build();
        }

        ShareLink link = new ShareLink();
        link.setRoom(room);
        shareLinkRepository.save(link);
        room.setShareLink(link);

        return ShareLinkResponse.builder()
                .linkUrl(link.getLinkUrl())
                .build();
    }

    public ShareLinkResponse getShareLink(Long roomId) {
        ShareLink shareLink = shareLinkRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("공유 링크가 존재하지 않습니다."));
        return ShareLinkResponse.builder()
                .linkUrl(shareLink.getLinkUrl())
                .build();
    }



}
