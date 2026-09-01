package br.com.autocarehub.infrastructure.security;

import static java.util.Objects.requireNonNull;

import br.com.autocarehub.application.port.out.AuthenticationGateway;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuthenticationGateway implements AuthenticationGateway {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public SpringSecurityAuthenticationGateway(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public IssuedAccessToken authenticate(String username, String password) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        JwtService.IssuedToken token =
                jwtService.generateToken((AuthenticatedUser) requireNonNull(authentication.getPrincipal()));
        return new IssuedAccessToken(token.accessToken(), token.tokenType(), token.expiresIn());
    }
}
