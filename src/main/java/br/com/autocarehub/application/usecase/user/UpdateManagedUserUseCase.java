package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.CompanyRepository;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.model.User;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class UpdateManagedUserUseCase {

    private final UserRepository userRepository;
    private final UpdateUserUseCase updateUserUseCase;
    private final UserManagementPolicy policy;

    public UpdateManagedUserUseCase(
            UserRepository userRepository, CompanyRepository companyRepository, UpdateUserUseCase updateUserUseCase) {
        this.userRepository = userRepository;
        this.updateUserUseCase = updateUserUseCase;
        this.policy = new UserManagementPolicy(companyRepository);
    }

    public User execute(Command command) {
        User requester = userRepository
                .findById(command.requesterId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User current = userRepository
                .findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserManagementPolicy.ManagedUserCommand normalized = policy.normalize(requester, command.user(), current);
        return updateUserUseCase.execute(ManagedUserMapper.toUpdateCommand(current, normalized));
    }

    public record Command(UUID requesterId, UUID userId, UserManagementPolicy.ManagedUserCommand user) {

        public Command(
                UUID requesterId,
                UUID userId,
                String username,
                String role,
                @Nullable UUID customerId,
                @Nullable UUID companyId,
                String fullName,
                String profileType,
                String companyName,
                String companyType,
                boolean createCompany,
                String employeeSubRole,
                List<String> permissions,
                boolean active) {
            this(
                    requesterId,
                    userId,
                    new UserManagementPolicy.ManagedUserCommand(
                            username,
                            role,
                            customerId,
                            companyId,
                            fullName,
                            profileType,
                            companyName,
                            companyType,
                            createCompany,
                            employeeSubRole,
                            permissions,
                            active));
        }
    }
}
