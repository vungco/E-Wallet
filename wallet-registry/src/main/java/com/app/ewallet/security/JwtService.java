package com.app.ewallet.security;

import com.app.ewallet.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_EMAIL = "email";

    private final JwtProperties jwtProperties;

    public String createAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.accessTokenExpirationMinutes() * 60L);
        SecretKey key = signingKey();
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(userId))
                .claim(CLAIM_EMAIL, email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        SecretKey key = signingKey();
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public String getEmail(Claims claims) {
        return claims.get(CLAIM_EMAIL, String.class);
    }

    private SecretKey signingKey() {
        byte[] bytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
