package com.example.sharestay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
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

    @PrePersist
    public void prePersist() {
        if (this.signupDate == null) this.signupDate = new Date();
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
