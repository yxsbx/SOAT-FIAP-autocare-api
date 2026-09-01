package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.port.out.CompanyRepository;
import br.com.autocarehub.domain.model.Company;
import br.com.autocarehub.domain.model.User;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class UserManagementPolicy {

    private static final String MASTER_ADMIN = "MASTER_ADMIN";
    private static final String CUSTOMER_OWNER = "CUSTOMER_OWNER";
    private static final String PARTS_STORE = "PARTS_STORE";
    private static final String WORKSHOP = "WORKSHOP";

    private final CompanyRepository companyRepository;

    UserManagementPolicy(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    ManagedUserCommand normalize(User requester, ManagedUserCommand command, @Nullable User current) {
        if (current != null && !canManageUser(requester, current)) {
            throw new ApplicationException("User is outside the current company scope");
        }
        ManagedUserCommand normalized = normalizeRoleProfile(command);
        if (isMasterAdmin(requester)) {
            return withResolvedCompany(normalized);
        }
        if (MASTER_ADMIN.equals(normalized.profileType())) {
            throw new ApplicationException("Only master admin can create or update master admin users");
        }
        String expectedProfile =
                PARTS_STORE.equals(requester.companyType()) ? "PARTS_STORE_EMPLOYEE" : "WORKSHOP_EMPLOYEE";
        String expectedCompanyType = PARTS_STORE.equals(requester.companyType()) ? PARTS_STORE : WORKSHOP;
        ManagedUserCommand scoped = new ManagedUserCommand(
                normalized.username(),
                "EMPLOYEE",
                normalized.customerId(),
                requester.companyId(),
                normalized.fullName(),
                expectedProfile,
                requester.companyName(),
                expectedCompanyType,
                false,
                normalizeEmployeeSubRole(normalized.employeeSubRole()),
                normalized.permissions(),
                normalized.active());
        if (current != null && !canManageUser(requester, userWithManagementData(current, scoped))) {
            throw new ApplicationException("User is outside the current company scope");
        }
        return scoped;
    }

    private User userWithManagementData(User current, ManagedUserCommand command) {
        return new User(
                current.id(),
                command.username(),
                current.passwordHash(),
                current.role(),
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
    }

    boolean canManageUser(User requester, User target) {
        if (isMasterAdmin(requester)) {
            return true;
        }
        if (MASTER_ADMIN.equals(target.profileType())) {
            return false;
        }
        return requester.companyId() != null && requester.companyId().equals(target.companyId());
    }

    boolean canSeeCompany(User requester, Company company) {
        return isMasterAdmin(requester) || company.id().equals(requester.companyId());
    }

    private ManagedUserCommand withResolvedCompany(ManagedUserCommand command) {
        if (MASTER_ADMIN.equals(command.profileType())) {
            Company platform = companyRepository
                    .findByName("AutoCare Hub")
                    .orElseGet(() -> companyRepository.save(Company.create("AutoCare Hub", "PLATFORM")));
            return command.withCompany(platform);
        }
        if (CUSTOMER_OWNER.equals(command.profileType())) {
            return command.withoutCompany();
        }
        if (command.companyId() != null && !command.createCompany()) {
            Company company = companyRepository
                    .findById(command.companyId())
                    .orElseThrow(() -> new ApplicationException("Company not found"));
            if (!company.type().equals(command.companyType())) {
                throw new ApplicationException("Company type dões not match user profile");
            }
            return command.withCompany(company);
        }
        if (command.companyName().isBlank()) {
            throw new ApplicationException("Company name is required");
        }
        if (!command.createCompany()) {
            Company company = companyRepository
                    .findByName(command.companyName())
                    .orElseThrow(() -> new ApplicationException("Company not found"));
            if (!company.type().equals(command.companyType())) {
                throw new ApplicationException("Company type dões not match user profile");
            }
            return command.withCompany(company);
        }
        companyRepository.findByName(command.companyName()).ifPresent(company -> {
            throw new ApplicationException("Company already exists");
        });
        return command.withCompany(
                companyRepository.save(Company.create(command.companyName(), command.companyType())));
    }

    private ManagedUserCommand normalizeRoleProfile(ManagedUserCommand command) {
        return switch (command.profileType()) {
            case MASTER_ADMIN -> command.withRole("ADMIN").withoutCompany().withoutEmployeeSubRole();
            case "WORKSHOP_ADMIN" ->
                command.withRole("ADMIN").withCompanyType(WORKSHOP).withoutEmployeeSubRole();
            case "PARTS_STORE_ADMIN" ->
                command.withRole("ADMIN").withCompanyType(PARTS_STORE).withoutEmployeeSubRole();
            case "WORKSHOP_EMPLOYEE" ->
                command.withRole("EMPLOYEE")
                        .withCompanyType(WORKSHOP)
                        .withEmployeeSubRole(normalizeEmployeeSubRole(command.employeeSubRole()));
            case "PARTS_STORE_EMPLOYEE" ->
                command.withRole("EMPLOYEE")
                        .withCompanyType(PARTS_STORE)
                        .withEmployeeSubRole(normalizeEmployeeSubRole(command.employeeSubRole()));
            case CUSTOMER_OWNER -> command.withRole("CUSTOMER").withoutCompany().withoutEmployeeSubRole();
            default -> throw new ApplicationException("Invalid user profile type");
        };
    }

    private boolean isMasterAdmin(User user) {
        return MASTER_ADMIN.equals(user.profileType());
    }

    private String normalizeEmployeeSubRole(String value) {
        return value == null || value.isBlank() ? "UNSPECIFIED" : value;
    }

    record ManagedUserCommand(
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

        ManagedUserCommand withRole(String nextRole) {
            return new ManagedUserCommand(
                    username,
                    nextRole,
                    customerId,
                    companyId,
                    fullName,
                    profileType,
                    companyName,
                    companyType,
                    createCompany,
                    employeeSubRole,
                    permissions,
                    active);
        }

        ManagedUserCommand withCompanyType(String nextCompanyType) {
            return new ManagedUserCommand(
                    username,
                    role,
                    customerId,
                    companyId,
                    fullName,
                    profileType,
                    companyName,
                    nextCompanyType,
                    createCompany,
                    employeeSubRole,
                    permissions,
                    active);
        }

        ManagedUserCommand withEmployeeSubRole(String nextEmployeeSubRole) {
            return new ManagedUserCommand(
                    username,
                    role,
                    customerId,
                    companyId,
                    fullName,
                    profileType,
                    companyName,
                    companyType,
                    createCompany,
                    nextEmployeeSubRole,
                    permissions,
                    active);
        }

        ManagedUserCommand withoutCompany() {
            return new ManagedUserCommand(
                    username,
                    role,
                    customerId,
                    null,
                    fullName,
                    profileType,
                    "",
                    "",
                    false,
                    employeeSubRole,
                    permissions,
                    active);
        }

        ManagedUserCommand withoutEmployeeSubRole() {
            return new ManagedUserCommand(
                    username,
                    role,
                    customerId,
                    companyId,
                    fullName,
                    profileType,
                    companyName,
                    companyType,
                    createCompany,
                    "",
                    permissions,
                    active);
        }

        ManagedUserCommand withCompany(Company company) {
            return new ManagedUserCommand(
                    username,
                    role,
                    customerId,
                    company.id(),
                    fullName,
                    profileType,
                    company.name(),
                    company.type(),
                    false,
                    employeeSubRole,
                    permissions,
                    active);
        }
    }
}
