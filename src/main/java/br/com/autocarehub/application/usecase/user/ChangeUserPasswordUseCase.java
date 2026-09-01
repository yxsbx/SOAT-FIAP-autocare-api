package br.com.autocarehub.application.usecase.user;

import static java.util.Objects.requireNonNull;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PasswordHasher;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.model.User;
import java.util.UUID;

public class ChangeUserPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public ChangeUserPasswordUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public void execute(Command command) {
        User current = userRepository
                .findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (command.currentPasswordRequired()
                && !passwordHasher.matches(command.currentPassword(), current.passwordHash())) {
            throw new ApplicationException("Current password is invalid");
        }
        User updated = new User(
                current.id(),
                current.username(),
                requireNonNull(passwordHasher.hash(command.newPassword())),
                current.role(),
                current.customerId(),
                current.companyId(),
                current.fullName(),
                current.profileType(),
                current.companyName(),
                current.companyType(),
                current.employeeSubRole(),
                current.permissions(),
                current.active(),
                current.createdAt());
        userRepository.save(updated);
    }

    public record Command(UUID userId, String currentPassword, String newPassword, boolean currentPasswordRequired) {}
}
