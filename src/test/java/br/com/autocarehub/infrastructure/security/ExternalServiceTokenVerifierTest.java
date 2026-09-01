package br.com.autocarehub.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class ExternalServiceTokenVerifierTest {

    @Test
    void shouldAcceptMatchingToken() {
        ExternalServiceTokenVerifier verifier = new ExternalServiceTokenVerifier("external-token");

        assertThatNoException().isThrownBy(() -> verifier.verify("external-token"));
    }

    @Test
    void shouldRejectMissingReceivedToken() {
        ExternalServiceTokenVerifier verifier = new ExternalServiceTokenVerifier("external-token");

        assertThatThrownBy(() -> verifier.verify(null)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldRejectBlankReceivedToken() {
        ExternalServiceTokenVerifier verifier = new ExternalServiceTokenVerifier("external-token");

        assertThatThrownBy(() -> verifier.verify(" ")).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldRejectInvalidReceivedToken() {
        ExternalServiceTokenVerifier verifier = new ExternalServiceTokenVerifier("external-token");

        assertThatThrownBy(() -> verifier.verify("wrong-token")).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldRejectWhenExpectedTokenIsNotConfigured() {
        ExternalServiceTokenVerifier verifier = new ExternalServiceTokenVerifier("");

        assertThatThrownBy(() -> verifier.verify("external-token")).isInstanceOf(AccessDeniedException.class);
    }
}
