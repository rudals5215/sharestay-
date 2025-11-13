package com.example.sharestay.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FirebaseService {
    public String uploadFile(MultipartFile file) {
        // 실제 Firebase Storage 업로드 로직
        // StorageReference ref = storage.getReference().child("rooms/" + file.getOriginalFilename());
        // ref.putBytes(file.getBytes());
        // return ref.getDownloadUrl().toString();

        // 🔹 지금은 테스트용으로 더미 URL 반환
        return "https://firebase.storage.fake/" + file.getOriginalFilename();
    }
}
