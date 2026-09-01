package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.CompanyRepository;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.model.User;
import java.util.List;
import java.util.UUID;

public class ListManageableUsersUseCase {

    private final UserRepository userRepository;
    private final ListUsersUseCase listUsersUseCase;
    private final UserManagementPolicy policy;

    public ListManageableUsersUseCase(
            UserRepository userRepository, CompanyRepository companyRepository, ListUsersUseCase listUsersUseCase) {
        this.userRepository = userRepository;
        this.listUsersUseCase = listUsersUseCase;
        this.policy = new UserManagementPolicy(companyRepository);
    }

    public List<User> execute(Query query) {
        User requester = userRepository
                .findById(query.requesterId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return listUsersUseCase.execute(query.users()).stream()
                .filter(user -> policy.canManageUser(requester, user))
                .toList();
    }

    public record Query(UUID requesterId, ListUsersUseCase.Query users) {}
}
