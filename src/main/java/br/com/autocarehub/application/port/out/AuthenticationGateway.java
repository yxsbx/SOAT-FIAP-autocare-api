package br.com.autocarehub.application.port.out;

public interface AuthenticationGateway {

    IssuedAccessToken authenticate(String username, String password);

    record IssuedAccessToken(String accessToken, String tokenType, long expiresIn) {}
}
