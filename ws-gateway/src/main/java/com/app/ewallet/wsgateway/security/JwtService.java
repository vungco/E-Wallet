package com.app.ewallet.wsgateway.security;

import com.app.ewallet.wsgateway.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

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

    private SecretKey signingKey() {
        byte[] bytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
