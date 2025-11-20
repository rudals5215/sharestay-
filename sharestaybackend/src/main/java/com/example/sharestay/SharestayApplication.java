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

        Room room1 = new Room(
                host1,
                "서면역 도보 3분 깔끔한 원룸",
                48.0,
                "부산광역시 부산진구 부전동 123-1",
                "OFFICETEL",
                35.157842,
                129.059314,
                2,
                "서면역과 가까워 교통이 편리한 깔끔한 원룸입니다."
        );

        Room room2 = new Room(
                host1,
                "서면 젊음의거리 접근성 좋은 원룸",
                52.5,
                "부산광역시 부산진구 부전동 98-4",
                "ETC",
                35.156912,
                129.060128,
                1,
                "서면 젊음의거리 인근에 위치한 조용한 원룸입니다."
        );

        Room room3 = new Room(
                host1,
                "전포카페거리 근처 조용한 투룸",
                62.0,
                "부산광역시 부산진구 전포동 310-22",
                "APARTMENT",
                35.154234,
                129.064512,
                3,
                "전포카페거리와 가까우면서도 조용한 투룸입니다."
        );

        Room room4 = new Room(
                host1,
                "서면몰 인근 저렴한 원룸",
                45.0,
                "부산광역시 부산진구 부전동 201-15",
                "ONE_ROOM",
                35.158322,
                129.058112,
                1,
                "합리적인 가격의 서면 인근 원룸입니다."
        );

        Room room5 = new Room(
                host1,
                "서면 로데오거리 근처 신축 원룸",
                57.0,
                "부산광역시 부산진구 부전동 77-9",
                "OFFICETEL",
                35.159101,
                129.061442,
                2,
                "신축이라 내부가 매우 깨끗한 원룸입니다."
        );

        Room room6 = new Room(
                host1,
                "서면역 1번출구 바로 앞 원룸",
                58.3,
                "부산광역시 부산진구 부전동 134-8",
                "TWO_ROOM",
                35.157122,
                129.058791,
                2,
                "역세권에 특화된 최상의 위치를 가진 원룸입니다."
        );

        Room room7 = new Room(
                host1,
                "전포역 사이, 접근성 좋은 투룸",
                65.0,
                "부산광역시 부산진구 전포동 295-4",
                "APARTMENT",
                35.155842,
                129.062931,
                3,
                "전포역과 서면역 사이에 있어 접근성이 뛰어납니다."
        );

        Room room8 = new Room(
                host1,
                "서면시장 근처 저렴한 원룸",
                43.5,
                "부산광역시 부산진구 부전동 54-12",
                "ONE_ROOM",
                35.158549,
                129.057466,
                1,
                "서면시장 근처의 조용한 원룸입니다."
        );

        Room room9 = new Room(
                host1,
                "서면역 도보 3분 깔끔한 원룸",
                48.0,
                "부산광역시 부산진구 부전동 123-1",
                "TWO_ROOM",
                35.157842,
                129.059314,
                2,
                "서면역과 가까워 교통이 편리한 깔끔한 원룸입니다."
        );

        Room room10 = new Room(
                host1,
                "서면역 도보 3분 깔끔한 원룸",
                48.0,
                "부산광역시 부산진구 부전동 123-1",
                "TWO_ROOM",
                35.157842,
                129.059314,
                2,
                "서면역과 가까워 교통이 편리한 깔끔한 원룸입니다."
        );



        hostRepository.save(host1);
        roomRepository.save(room);

        roomRepository.save(room1);
        roomRepository.save(room2);
        roomRepository.save(room3);
        roomRepository.save(room4);
        roomRepository.save(room5);
        roomRepository.save(room6);
        roomRepository.save(room7);
        roomRepository.save(room8);
        roomRepository.save(room9);
        roomRepository.save(room10);

        // Favorite 객체 생성
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRoom(room);
        favoriteRepository.save(favorite);

    }
}
