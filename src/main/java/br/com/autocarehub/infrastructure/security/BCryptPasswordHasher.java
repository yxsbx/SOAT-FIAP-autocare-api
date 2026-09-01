package br.com.autocarehub.infrastructure.security;

import br.com.autocarehub.application.port.out.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String plainTextPassword) {
        return passwordEncoder.encode(plainTextPassword);
    }

    @Override
    public boolean matches(String plainTextPassword, String passwordHash) {
        return passwordEncoder.matches(plainTextPassword, passwordHash);
    }
}
