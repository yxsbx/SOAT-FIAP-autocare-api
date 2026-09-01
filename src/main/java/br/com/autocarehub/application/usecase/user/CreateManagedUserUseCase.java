package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.application.port.out.CompanyRepository;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.model.User;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class CreateManagedUserUseCase {

    private final UserRepository userRepository;
    private final CreateUserUseCase createUserUseCase;
    private final UserManagementPolicy policy;

    public CreateManagedUserUseCase(
            UserRepository userRepository, CompanyRepository companyRepository, CreateUserUseCase createUserUseCase) {
        this.userRepository = userRepository;
        this.createUserUseCase = createUserUseCase;
        this.policy = new UserManagementPolicy(companyRepository);
    }

    public User execute(Command command) {
        User requester = userRepository
                .findById(command.requesterId())
                .orElseThrow(
                        () -> new br.com.autocarehub.application.exception.ResourceNotFoundException("User not found"));
        UserManagementPolicy.ManagedUserCommand normalized = policy.normalize(requester, command.user(), null);
        return createUserUseCase.execute(ManagedUserMapper.toCreateCommand(normalized, command.password()));
    }

    public record Command(UUID requesterId, String password, UserManagementPolicy.ManagedUserCommand user) {

        public Command(
                UUID requesterId,
                String password,
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
                    password,
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
