package com.example.sharestay.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false, name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private final String username;

    @Column(nullable = false, name = "password_hash")
    private final String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "address")
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private final String role;

    @Column(nullable = false)
    private final Date signupDate;

    @Column(name = "life_style")
    private String lifeStyle;
}
