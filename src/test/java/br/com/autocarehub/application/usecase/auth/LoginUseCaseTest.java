package br.com.autocarehub.application.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.autocarehub.application.port.out.AuthenticationGateway;
import org.junit.jupiter.api.Test;

class LoginUseCaseTest {

    @Test
    void shouldAuthenticateThroughApplicationPort() {
        AuthenticationGateway gateway = (username, password) -> {
            assertThat(username).isEqualTo("admin@autocarehub.com");
            assertThat(password).isEqualTo("secret123");
            return new AuthenticationGateway.IssuedAccessToken("jwt-token", "Bearer", 3600);
        };
        LoginUseCase useCase = new LoginUseCase(gateway);

        LoginUseCase.Output output = useCase.execute(new LoginUseCase.Command("admin@autocarehub.com", "secret123"));

        assertThat(output.accessToken()).isEqualTo("jwt-token");
        assertThat(output.tokenType()).isEqualTo("Bearer");
        assertThat(output.expiresIn()).isEqualTo(3600);
    }
}
