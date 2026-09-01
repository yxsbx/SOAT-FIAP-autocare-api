package br.com.autocarehub.interfaces.rest.controller;

import br.com.autocarehub.application.usecase.auth.LoginUseCase;
import br.com.autocarehub.interfaces.rest.generated.api.AuthApi;
import br.com.autocarehub.interfaces.rest.generated.model.LoginRequest;
import br.com.autocarehub.interfaces.rest.generated.model.LoginResponse;
import br.com.autocarehub.interfaces.rest.mapper.AuthRestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        LoginUseCase.Output output = loginUseCase.execute(AuthRestMapper.toCommand(loginRequest));
        return ResponseEntity.ok(AuthRestMapper.toResponse(output));
    }
}
