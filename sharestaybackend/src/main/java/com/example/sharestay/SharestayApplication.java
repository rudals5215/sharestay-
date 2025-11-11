package com.example.sharestay;

import com.example.sharestay.domain.Host;
import com.example.sharestay.domain.HostRepository;
import com.example.sharestay.domain.User;
import com.example.sharestay.domain.UserRepository;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@RequiredArgsConstructor
public class SharestayApplication implements CommandLineRunner {
    private final UserRepository userRepository;
    private final HostRepository hostRepository;

    public static void main(String[] args) {
        SpringApplication.run(SharestayApplication.class, args);
    }

    @Override
    public void run(String... args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        if (userRepository.existsByUsername("kim1@test.com")) {
            return;
        }

        User user = User.builder()
                .username("kim1@test.com")
                .password(encoder.encode("user1234"))
                .loginType("LOCAL")
                .nickname("도하 킴")
                .address("인천, 대한민국")
                .phoneNumber("010-1234-5678")
                .role("ADMIN")
                .lifeStyle("금연 · 반려동물 없음 · 조용한 활동 선호")
                .signupDate(new Date())
                .build();
        userRepository.save(user);
    }
}
