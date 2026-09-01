package br.com.autocarehub.interfaces.rest.mapper;

import br.com.autocarehub.application.usecase.auth.LoginUseCase;
import br.com.autocarehub.interfaces.rest.generated.model.LoginRequest;
import br.com.autocarehub.interfaces.rest.generated.model.LoginResponse;

public final class AuthRestMapper {

    private AuthRestMapper() {}

    public static LoginUseCase.Command toCommand(LoginRequest request) {
        return new LoginUseCase.Command(request.getUsername(), request.getPassword());
    }

    public static LoginResponse toResponse(LoginUseCase.Output output) {
        return new LoginResponse(output.accessToken(), output.tokenType(), output.expiresIn());
    }
}
