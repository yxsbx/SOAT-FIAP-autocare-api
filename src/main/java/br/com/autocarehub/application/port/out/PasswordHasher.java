package br.com.autocarehub.application.port.out;

public interface PasswordHasher {

    String hash(String plainTextPassword);

    boolean matches(String plainTextPassword, String passwordHash);
}
