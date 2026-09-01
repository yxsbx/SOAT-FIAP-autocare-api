package br.com.autocarehub.application.usecase.user;

import static java.util.Objects.requireNonNull;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.port.out.PasswordHasher;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.User;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User execute(Command command) {
        userRepository.findByUsername(command.username()).ifPresent(user -> {
            throw new ApplicationException("Username already exists");
        });
        User user = new User(
                UUID.randomUUID(),
                command.username(),
                requireNonNull(passwordHasher.hash(command.password())),
                UserRole.valueOf(command.role()),
                command.customerId(),
                command.companyId(),
                command.fullName(),
                command.profileType(),
                command.companyName(),
                command.companyType(),
                command.employeeSubRole(),
                command.permissions(),
                command.active(),
                LocalDateTime.now());
        return userRepository.save(user);
    }

    public record Command(
            String username,
            String password,
            String role,
            UUID customerId,
            UUID companyId,
            String fullName,
            String profileType,
            String companyName,
            String companyType,
            String employeeSubRole,
            java.util.List<String> permissions,
            boolean active) {}
}
