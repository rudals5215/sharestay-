package com.example.sharestay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false, name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, name = "password_hash")
    private String password; //OAuth2 유저는 null이 가능

    @Column(nullable = false)
    private String loginType; // local or google

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "address")
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private Date signupDate;

    @Column(name = "life_style")
    private String lifeStyle;

    // JPA가 엔티티를 데이터베이스에 저장하기 직전에 호출
    @PrePersist
    public void prePersist() {
        if (this.signupDate == null) this.signupDate = new Date();
    }

    // local 회원가입 시 사용될 명확한 생성자
    // 생성자의 직접적인 외부 호출을 막기 위해 private로 변경
    public User(String username, String password, String loginType, String nickname, String address, String phoneNumber, String role, String lifeStyle) {
        this.username = username;
        this.password = password;
        this.loginType = loginType;
        this.nickname = nickname;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.lifeStyle = lifeStyle;
    }

    // Google 전용 생성자는 private, 정적 메서드로 생성
    private User(String username, String loginType, String role, String nickname) {
        this.username = username;
        this.password = null; // Google은 비밀번호 없음
        this.loginType = loginType;
        this.role = role != null ? role : "GUEST";
        this.nickname = nickname != null ? nickname : "GoogleUser";
    }

    // Google 회원가입용 정적 팩토리 메서드
    public static User createGoogleUser(String email) {
        return new User(email, "google", "GUEST", "GoogleUser");
    }
}
