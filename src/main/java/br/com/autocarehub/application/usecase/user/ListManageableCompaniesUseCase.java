package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.CompanyRepository;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.model.Company;
import br.com.autocarehub.domain.model.User;
import java.util.List;
import java.util.UUID;

public class ListManageableCompaniesUseCase {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserManagementPolicy policy;

    public ListManageableCompaniesUseCase(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.policy = new UserManagementPolicy(companyRepository);
    }

    public List<Company> execute(UUID requesterId) {
        User requester =
                userRepository.findById(requesterId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return companyRepository.findAll().stream()
                .filter(company -> policy.canSeeCompany(requester, company))
                .toList();
    }
}
