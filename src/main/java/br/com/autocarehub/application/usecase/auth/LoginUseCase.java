package br.com.autocarehub.application.usecase.auth;

import br.com.autocarehub.application.port.out.AuthenticationGateway;

public class LoginUseCase {

    private final AuthenticationGateway authenticationGateway;

    public LoginUseCase(AuthenticationGateway authenticationGateway) {
        this.authenticationGateway = authenticationGateway;
    }

    public Output execute(Command command) {
        AuthenticationGateway.IssuedAccessToken token =
                authenticationGateway.authenticate(command.username(), command.password());
        return new Output(token.accessToken(), token.tokenType(), token.expiresIn());
    }

    public record Command(String username, String password) {}

    public record Output(String accessToken, String tokenType, long expiresIn) {}
}
