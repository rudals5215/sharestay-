package com.example.sharestay;


import com.example.sharestay.repository.FavoriteRepository;
import com.example.sharestay.repository.HostRepository;
import com.example.sharestay.entity.User;
import com.example.sharestay.repository.RoomRepository;
import com.example.sharestay.repository.UserRepository;
import com.example.sharestay.entity.*;


import com.example.sharestay.service.RoomService;
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
    private final RoomService roomService;

    public static void main(String[] args) {
        SpringApplication.run(SharestayApplication.class, args);
    }

    @Override
    public void run(String... args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        // 이미 초기 데이터 있으면 다시 안 넣기
        if (userRepository.existsByUsername("kim1@test.com")) {
            return;
        }

    /* -------------------------
       1. ADMIN 계정 2개
    -------------------------- */
        User admin1 = new User(
                "kim1@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "도하 킴",
                "인천, 대한민국",
                "010-1234-5678",
                "ADMIN",
                "금연 · 반려동물 없음 · 조용한 활동 선호"
        );
        userRepository.save(admin1);

        User admin2 = new User(
                "admin@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "관리자2",
                "서울, 대한민국",
                "010-2222-9999",
                "ADMIN",
                "시스템 운영 및 모니터링 담당 관리자 계정"
        );
        userRepository.save(admin2);


    /* -------------------------
       2. HOST 계정 3개
    -------------------------- */
        User hostUser1 = new User(
                "host1@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "홍대 호스트",
                "서울 마포구",
                "010-1111-1111",
                "HOST",
                "깨끗한 방을 좋아하는 호스트입니다."
        );
        userRepository.save(hostUser1);

        User hostUser2 = new User(
                "host2@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "강남 호스트",
                "서울 강남구",
                "010-2222-1111",
                "HOST",
                "직장인과 장기 거주자를 선호합니다."
        );
        userRepository.save(hostUser2);

        User hostUser3 = new User(
                "host3@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "잠실 호스트",
                "서울 송파구",
                "010-3333-1111",
                "HOST",
                "교통과 치안을 중시하는 호스트입니다."
        );
        userRepository.save(hostUser3);


    /* -------------------------
       3. GUEST 계정 3개
    -------------------------- */
        User guest1 = new User(
                "guest1@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "김게스트",
                "부산 해운대",
                "010-4444-4444",
                "GUEST",
                "조용한 환경과 청결한 방을 선호합니다."
        );
        userRepository.save(guest1);

        User guest2 = new User(
                "guest2@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "박여행",
                "대구 수성구",
                "010-5555-5555",
                "GUEST",
                "가성비 좋은 방을 선호합니다."
        );
        userRepository.save(guest2);

        User guest3 = new User(
                "guest3@test.com",
                encoder.encode("user1234"),
                "LOCAL",
                "최출장",
                "서울 노원구",
                "010-6666-5555",
                "GUEST",
                "주차 가능 숙소를 선호합니다."
        );
        userRepository.save(guest3);


    /* -------------------------
       4. Host 엔티티 3개
    -------------------------- */
        Host host1 = new Host(
                "홍대/합정 인근에서 조용한 생활을 돕는 호스트입니다.",
                true,
                hostUser1
        );

        Host host2 = new Host(
                "강남 직장인을 위한 최적의 숙소를 제공합니다.",
                true,
                hostUser2
        );

        Host host3 = new Host(
                "잠실 야경과 편리한 교통을 자랑하는 호스트입니다.",
                true,
                hostUser3
        );

        hostRepository.save(host1);
        hostRepository.save(host2);
        hostRepository.save(host3);


    /* -------------------------
       5. Room 6개 (호스트별로 분배)
          availabilityStatus:
          0 = 모집중, 1 = 예약중, 2 = 마감
    -------------------------- */

        // host1 방 2개
        Room room1 = new Room(
                host1,
                "홍대입구 근처 원룸",
                550000,
                "서울특별시 마포구 서교동 12-3",
                "원룸",
                37.557123,
                126.923456,
                0, // 모집중
                "깔끔하고 교통 좋은 원룸입니다."
        );
        room1.setOptions("에어컨, 냉장고, 세탁기, 엘리베이터");

        Room room2 = new Room(
                host1,
                "합정역 루프탑 사용 가능 쉐어하우스",
                450000,
                "서울특별시 마포구 합정동 321-8",
                "쉐어하우스",
                37.549911,
                126.914905,
                0, // 모집중
                "루프탑과 공용 라운지가 있는 합정 쉐어하우스입니다."
        );
        room2.setOptions("와이파이, TV, 주차장, 침대");

        // host2 방 2개
        Room room3 = new Room(
                host2,
                "강남역 도보 5분 원룸",
                300000,
                "서울특별시 강남구 역삼동 123-4",
                "원룸",
                37.497942,
                127.027621,
                1, // 예약중
                "강남역 인근 직장인에게 최적의 원룸입니다."
        );
        room3.setOptions("엘리베이터, 주차장, 와이파이, 전자렌지");

        Room room4 = new Room(
                host2,
                "건대입구역 풀옵션 원룸",
                250000,
                "서울특별시 광진구 화양동 22-10",
                "원룸",
                37.540408,
                127.069210,
                1, // 예약중
                "풀옵션(에어컨, 세탁기, 냉장고) 포함된 신축 원룸입니다."
        );

        room3.setOptions("에어컨, 냉장고, 세탁기, 엘리베이터, TV");

        // host3 방 2개
        Room room5 = new Room(
                host3,
                "신촌역 근처 투룸",
                400000,
                "서울특별시 서대문구 창천동 45-7",
                "투룸",
                37.559819,
                126.942308,
                2, // 마감
                "연세대/이대생에게 인기 많은 조용한 투룸입니다."
        );

        Room room6 = new Room(
                host3,
                "잠실새내역 오피스텔 원룸",
                350000,
                "서울특별시 송파구 잠실동 200-15",
                "오피스텔",
                37.511822,
                127.086554,
                2, // 마감
                "롯데월드, 잠실역 접근성 좋은 오피스텔 원룸입니다."
        );

        room1.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2F3d-kwaejeoghan-dogseosil.jpg?alt=media&token=6aa1cc21-20e5-4bc6-b5b5-4552daecd01d");

        room2.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2F51edef840db1a.png?alt=media&token=720e627b-8d23-4e7e-8fa1-6bd748a83ad9");
        room2.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2Fliving-room-2732939_1280.jpg?alt=media&token=e2bf5e11-398c-4b87-80a1-28b988045d5f");

        room3.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2Fbedroom-416062_640.jpg?alt=media&token=e8d9aceb-72c4-4b22-93d6-885e51b7f5e9");

        room4.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2Fintelieo-sigmul-iissneun-minimeollijeum-chimdae.jpg?alt=media&token=7ac951f1-b017-4084-8d91-1e8feb5699a6");
        room4.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2Fkitchen-6916200_640.jpg?alt=media&token=5d616e87-bf0a-4ac8-8253-d8f3f40c7d5f");

        room5.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2Froom-boy-2132349_1280.jpg?alt=media&token=e9f7131f-4be7-4987-be4e-fe88a5d9b197");

        room6.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2Fhome-820389_1280.jpg?alt=media&token=e2d18205-4b4c-4200-a4d5-abe38d720185");
        room6.addRoomImage("https://firebasestorage.googleapis.com/v0/b/sharestay-4d2c6.firebasestorage.app/o/rooms%2Fbedroom-1872196_1280.jpg?alt=media&token=78d11423-e245-4b8a-8bd0-54e31587c66c");

        List<Room> rooms = Arrays.asList(room1, room2, room3, room4, room5, room6);
        roomRepository.saveAll(rooms);


    /* -------------------------
       6. Favorite 샘플
    -------------------------- */
        Favorite favorite1 = new Favorite();
        favorite1.setUser(guest1);
        favorite1.setRoom(room1);

        Favorite favorite2 = new Favorite();
        favorite2.setUser(guest2);
        favorite2.setRoom(room3);

        Favorite favorite3 = new Favorite();
        favorite3.setUser(guest3);
        favorite3.setRoom(room5);

        favoriteRepository.save(favorite1);
        favoriteRepository.save(favorite2);
        favoriteRepository.save(favorite3);


        // ⚠ 개발용: 더미 데이터에만 한 번 돌리고, 끝난 뒤에는 주석 처리해 두기
        roomService.backfillShareLinks();
    }

}
