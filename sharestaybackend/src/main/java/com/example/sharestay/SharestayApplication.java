package com.example.sharestay;


import com.example.sharestay.repository.FavoriteRepository;
import com.example.sharestay.repository.HostRepository;
import com.example.sharestay.entity.User;
import com.example.sharestay.repository.RoomRepository;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.entity.*;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class SharestayApplication implements CommandLineRunner {
    private final UserRepository userRepository;
    private final HostRepository hostRepository;
    private final RoomRepository roomRepository;
    private final FavoriteRepository favoriteRepository;

    public static void main(String[] args) {
        SpringApplication.run(SharestayApplication.class, args);
    }

    @Override
    public void run(String... args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        if (userRepository.existsByUsername("kim1@test.com")) {
            return;
        }

        User user = new User(
                "kim1@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "도하 킴",
                "인천, 대한민국",
                "010-1234-5678",
                "ADMIN",
                "금연 · 반려동물 없음 · 조용한 활동 선호"
        );
        userRepository.save(user);


        Host host1 = new Host(
                "깨끗한 방을 좋아하는 호스트입니다.",  // introduction
                true,                                 // termsAgreed
                user                                  // user (연결된 User)
        );

        // Room 객체 생성
        Room room = new Room(
                host1,                          // 어떤 호스트가 등록한 방인지
                "홍대입구 근처 원룸",             // title
                55.5,                           // rentPrice
                "서울특별시 마포구 서교동 12-3",    // address
                "원룸",                          // type
                37.557123,                      // latitude (위도)
                126.923456,                     // longitude (경도)
                2,                              // availabilityStatus (최대 인원)
                "깔끔하고 교통 좋은 원룸입니다."      // description
        );


        Room room2 = new Room(
                host1,                          // 어떤 호스트가 등록한 방인지
                "강남역 도보 5분 원룸",            // title
                80.0,                           // rentPrice
                "서울특별시 강남구 역삼동 123-4",   // address
                "원룸",                          // type
                37.497942,                      // latitude (위도)
                127.027621,                     // longitude (경도)
                1,                              // availabilityStatus (최대 인원)
                "강남역 인근 직장인에게 최적의 원룸입니다." // description
        );

        Room room3 = new Room(
                host1,                          // 어떤 호스트가 등록한 방인지
                "신촌역 근처 투룸",               // title
                65.0,                           // rentPrice
                "서울특별시 서대문구 창천동 45-7", // address
                "투룸",                          // type
                37.559819,                      // latitude (위도)
                126.942308,                     // longitude (경도)
                3,                              // availabilityStatus (최대 인원)
                "연세대/이대생에게 인기 많은 조용한 투룸입니다." // description
        );

        Room room4 = new Room(
                host1,                          // 어떤 호스트가 등록한 방인지
                "건대입구역 풀옵션 원룸",          // title
                58.5,                           // rentPrice
                "서울특별시 광진구 화양동 22-10",   // address
                "원룸",                          // type
                37.540408,                      // latitude (위도)
                127.069210,                     // longitude (경도)
                1,                              // availabilityStatus (최대 인원)
                "풀옵션(에어컨, 세탁기, 냉장고) 포함된 신축 원룸입니다." // description
        );

        Room room5 = new Room(
                host1,                          // 어떤 호스트가 등록한 방인지
                "합정역 루프탑 사용 가능 쉐어하우스", // title
                45.0,                           // rentPrice
                "서울특별시 마포구 합정동 321-8",   // address
                "쉐어하우스",                      // type
                37.549911,                      // latitude (위도)
                126.914905,                     // longitude (경도)
                4,                              // availabilityStatus (최대 인원)
                "루프탑과 공용 라운지가 있는 합정 쉐어하우스입니다." // description
        );

        Room room6 = new Room(
                host1,                          // 어떤 호스트가 등록한 방인지
                "잠실새내역 오피스텔 원룸",        // title
                75.0,                           // rentPrice
                "서울특별시 송파구 잠실동 200-15",  // address
                "오피스텔",                       // type
                37.511822,                      // latitude (위도)
                127.086554,                     // longitude (경도)
                2,                              // availabilityStatus (최대 인원)
                "롯데월드, 잠실역 접근성 좋은 오피스텔 원룸입니다." // description
        );




        hostRepository.save(host1);
        roomRepository.save(room);

        List<Room> rooms = Arrays.asList(room, room2, room3, room4, room5, room6);
        roomRepository.saveAll(rooms);


        // Favorite 객체 생성
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRoom(room);
        favoriteRepository.save(favorite);

    }
}
