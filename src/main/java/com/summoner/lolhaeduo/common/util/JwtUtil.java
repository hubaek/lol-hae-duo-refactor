package com.summoner.lolhaeduo.common.util;

import com.summoner.lolhaeduo.domain.member.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    // Authorization 헤더에서 사용되는 Bearer 토큰 접두사
    private static final String BEARER_PREFIX = "Bearer ";

    // 토큰 만료 시간(ms)
    private static final long TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 24시간

    // 환경 설정 파일에 저장된 secret key 값
    @Value("${jwt.secret.key}")
    String secretKey;

    // HMAC-SHA 알고리즘에 사용될 SecretKey 객체
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Long memberId, String username, UserRole userRole) {
        Date now = new Date();

        String jwt = Jwts.builder()
                .subject(memberId.toString())
                .claim("username", username)
                .claim("role", userRole.toString())
                .signWith(key)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_EXPIRATION_TIME))
                .compact();

        return BEARER_PREFIX + jwt;
    }

    public String substringToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        throw new JwtException("유효하지 않은 형식의 토큰입니다.");
    }

    public Jws<Claims> validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (MalformedJwtException e) {
            log.error("유효하지 않은 형식의 토큰입니다.");
            throw new JwtException(e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.");
            throw new JwtException(e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 토큰입니다.");
            throw new JwtException(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("잘못된 토큰입니다.");
            throw new JwtException(e.getMessage());
        }
    }

    public Claims getClaimsFromToken(String token) {
        Jws<Claims> claims = validateToken(token);
        return claims.getPayload();
    }
}