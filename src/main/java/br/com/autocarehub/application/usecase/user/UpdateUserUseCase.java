package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.User;
import java.util.UUID;

public class UpdateUserUseCase {

    private final UserRepository userRepository;

    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(Command command) {
        User current = userRepository
                .findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (command.username() != null
                && !command.username().isBlank()
                && !command.username().equals(current.username())) {
            userRepository.findByUsername(command.username()).ifPresent(user -> {
                throw new ApplicationException("Username already exists");
            });
        }
        User updated = new User(
                current.id(),
                command.username() == null || command.username().isBlank() ? current.username() : command.username(),
                current.passwordHash(),
                command.role() == null || command.role().isBlank() ? current.role() : UserRole.valueOf(command.role()),
                command.customerId(),
                command.companyId(),
                command.fullName(),
                command.profileType(),
                command.companyName(),
                command.companyType(),
                command.employeeSubRole(),
                command.permissions(),
                command.active(),
                current.createdAt());
        return userRepository.save(updated);
    }

    public record Command(
            UUID userId,
            String username,
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
