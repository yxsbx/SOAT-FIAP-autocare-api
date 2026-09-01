package br.com.autocarehub.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32;
    private static final int SECONDS_PER_MINUTE = 60;

    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes:60}") long expirationMinutes) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be provided through security.jwt.secret or JWT_SECRET");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must have at least 32 bytes");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalStateException("JWT expiration must be greater than zero");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationMinutes * SECONDS_PER_MINUTE;
    }

    public IssuedToken generateToken(AuthenticatedUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);
        UUID customerId = user.customerId();
        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.id().toString())
                .claim("role", user.role())
                .claim("customerId", customerId == null ? null : customerId.toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new IssuedToken(token, "Bearer", expirationSeconds);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token))
                && extractClaims(token).getExpiration().after(new Date());
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record IssuedToken(String accessToken, String tokenType, long expiresIn) {}
}
