package com.example.sharestay.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;


@Service
public class JwtService {
    // 앱 실행 동안 동일 키 유지 (메모리 보관)
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1일
                .signWith(key)
                .compact();
    }

//    public String extractUsername(String token) {
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
//                .getSubject();
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValid(String token) {
        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            // 토큰 파싱 시 서명 검증과 만료 시간 검증을 모두 수행
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // oauth2 관련 로직
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 예: 1시간
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600_000)) // 예: 7일
                .signWith(key)
                .compact();
    }

    // JwtService.java
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // 사용자 이름이 일치하는지, 그리고 토큰이 만료되지 않았는지 모두 확인
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

}