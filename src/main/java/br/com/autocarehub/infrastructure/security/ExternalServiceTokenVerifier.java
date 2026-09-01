package br.com.autocarehub.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceTokenVerifier {

    private final String expectedToken;

    public ExternalServiceTokenVerifier(@Value("${app.external-service.token:}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    public void verify(@Nullable String receivedToken) {
        if (expectedToken.isBlank() || receivedToken == null || receivedToken.isBlank()) {
            throw new AccessDeniedException("Access denied");
        }
        byte[] expected = expectedToken.getBytes(StandardCharsets.UTF_8);
        byte[] received = receivedToken.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, received)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
