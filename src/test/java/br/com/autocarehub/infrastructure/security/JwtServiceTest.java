package br.com.autocarehub.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-with-at-least-32-bytes";

    @Test
    void shouldRejectMissingSecret() {
        assertThatThrownBy(() -> new JwtService(null, 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be provided through security.jwt.secret or JWT_SECRET");
        assertThatThrownBy(() -> new JwtService("", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be provided through security.jwt.secret or JWT_SECRET");
    }

    @Test
    void shouldRejectShortSecret() {
        assertThatThrownBy(() -> new JwtService("short-secret", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must have at least 32 bytes");
    }

    @Test
    void shouldRejectNonPositiveExpiration() {
        assertThatThrownBy(() -> new JwtService(SECRET, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT expiration must be greater than zero");
    }

    @Test
    void shouldGenerateSignedTokenWithExpiration() {
        JwtService jwtService = new JwtService(SECRET, 60);
        AuthenticatedUser user = new AuthenticatedUser(new User(
                UUID.randomUUID(),
                "admin@autocarehub.com",
                "$2a$10$hashhashhashhashhashhashhashhashhashhashhashhashhash",
                UserRole.ADMIN,
                null,
                UUID.fromString("90000000-0000-0000-0000-000000000011"),
                "Admin",
                "WORKSHOP_ADMIN",
                "Oficina",
                "WORKSHOP",
                "",
                List.of(),
                true,
                LocalDateTime.now()));

        JwtService.IssuedToken token = jwtService.generateToken(user);

        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresIn()).isEqualTo(3600);
        assertThat(jwtService.extractUsername(token.accessToken())).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(token.accessToken(), user)).isTrue();
    }

    @Test
    void shouldGenerateTokenWithCustomerClaimAndRejectDifferentUser() {
        JwtService jwtService = new JwtService(SECRET, 1);
        UUID customerId = UUID.randomUUID();
        AuthenticatedUser customer = new AuthenticatedUser(new User(
                UUID.randomUUID(),
                "cliente@autocarehub.com",
                "$2a$10$hashhashhashhashhashhashhashhashhashhashhashhashhash",
                UserRole.CUSTOMER,
                customerId,
                true,
                LocalDateTime.now()));
        AuthenticatedUser other = new AuthenticatedUser(new User(
                UUID.randomUUID(),
                "outro@autocarehub.com",
                "$2a$10$hashhashhashhashhashhashhashhashhashhashhashhashhash",
                UserRole.CUSTOMER,
                customerId,
                true,
                LocalDateTime.now()));

        JwtService.IssuedToken token = jwtService.generateToken(customer);

        assertThat(jwtService.extractClaims(token.accessToken()).get("customerId", String.class))
                .isEqualTo(customerId.toString());
        assertThat(jwtService.isTokenValid(token.accessToken(), other)).isFalse();
        assertThatThrownBy(() -> jwtService.extractClaims(token.accessToken() + "tampered"))
                .isInstanceOf(RuntimeException.class);
    }
}
