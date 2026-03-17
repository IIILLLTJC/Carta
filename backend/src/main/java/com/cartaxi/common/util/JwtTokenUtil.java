package com.cartaxi.common.util;

import com.cartaxi.common.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {

    @Value("${cartaxi.jwt.secret}")
    private String secret;

    @Value("${cartaxi.jwt.expire-seconds}")
    private Long expireSeconds;

    @Value("${cartaxi.jwt.issuer}")
    private String issuer;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(LoginUser loginUser) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(loginUser.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiration)
                .claim("userId", loginUser.getUserId())
                .claim("role", loginUser.getRole())
                .claim("displayName", loginUser.getDisplayName())
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getExpireSeconds() {
        return expireSeconds;
    }
}
