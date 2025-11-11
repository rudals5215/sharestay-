package com.example.sharestay;

<<<<<<< HEAD
import com.example.sharestay.repository.HostRepository;
import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
=======
import com.example.sharestay.domain.*;

>>>>>>> main
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
    private final RoomRepository roomRepository;

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


        Host host1 = new Host(
                "깨끗한 방을 좋아하는 호스트입니다.",  // introduction
                true,                                 // termsAgreed
                user                                  // user (연결된 User)
        );

//        // Room 객체 생성
//        Room room = new Room(
//                host1,                          // 어떤 호스트가 등록한 방인지
//                "홍대입구 근처 원룸",             // title
//                55.5,                           // rentPrice
//                "서울특별시 마포구 서교동 12-3",    // address
//                "원룸",                          // type
//                37.557123,                      // latitude (위도)
//                126.923456,                     // longitude (경도)
//                2,                              // availabilityStatus (최대 인원)
//                "깔끔하고 교통 좋은 원룸입니다."      // description
//        );

        hostRepository.save(host1);
//        roomRepository.save(room);






    }
}

