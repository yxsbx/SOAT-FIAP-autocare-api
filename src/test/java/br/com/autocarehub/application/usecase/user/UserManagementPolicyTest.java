package br.com.autocarehub.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.port.out.CompanyRepository;
import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.Company;
import br.com.autocarehub.domain.model.User;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserManagementPolicyTest {

    private final InMemoryCompanyRepository companies = new InMemoryCompanyRepository();
    private final UserManagementPolicy policy = new UserManagementPolicy(companies);

    @Test
    void shouldNormalizeMasterAdminManagedProfiles() {
        User requester = user("master@autocarehub.com", UserRole.ADMIN, "MASTER_ADMIN", null, "", "");
        Company workshop = companies.save(Company.create("Oficina Azul", "WORKSHOP"));

        UserManagementPolicy.ManagedUserCommand platform = policy.normalize(
                requester, command("platform@autocarehub.com", "MASTER_ADMIN", "", "", false, null), null);
        UserManagementPolicy.ManagedUserCommand customer = policy.normalize(
                requester, command("customer@example.com", "CUSTOMER_OWNER", "Ignored", "WORKSHOP", true, null), null);
        UserManagementPolicy.ManagedUserCommand existingCompany = policy.normalize(
                requester, command("admin@oficina.com", "WORKSHOP_ADMIN", "", "WORKSHOP", false, workshop.id()), null);
        UserManagementPolicy.ManagedUserCommand createdCompany = policy.normalize(
                requester,
                command("parts@store.com", "PARTS_STORE_ADMIN", "Loja Parts", "PARTS_STORE", true, null),
                null);

        assertThat(platform.role()).isEqualTo("ADMIN");
        assertThat(platform.companyName()).isEqualTo("AutoCare Hub");
        assertThat(platform.companyType()).isEqualTo("PLATFORM");
        assertThat(customer.role()).isEqualTo("CUSTOMER");
        assertThat(customer.companyId()).isNull();
        assertThat(existingCompany.companyId()).isEqualTo(workshop.id());
        assertThat(createdCompany.companyName()).isEqualTo("Loja Parts");
        assertThat(companies.findByName("Loja Parts")).isPresent();
    }

    @Test
    void shouldRejectInvalidMasterAdminCompanyBindings() {
        User requester = user("master@autocarehub.com", UserRole.ADMIN, "MASTER_ADMIN", null, "", "");
        Company workshop = companies.save(Company.create("Oficina Azul", "WORKSHOP"));
        companies.save(Company.create("Loja Existente", "PARTS_STORE"));

        assertThatThrownBy(() -> policy.normalize(
                        requester,
                        command("parts@store.com", "PARTS_STORE_ADMIN", "", "PARTS_STORE", false, UUID.randomUUID()),
                        null))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Company not found");
        assertThatThrownBy(() -> policy.normalize(
                        requester,
                        command("parts@store.com", "PARTS_STORE_ADMIN", "", "PARTS_STORE", false, workshop.id()),
                        null))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Company type dões not match user profile");
        assertThatThrownBy(() -> policy.normalize(
                        requester,
                        command("parts@store.com", "PARTS_STORE_ADMIN", "", "PARTS_STORE", false, null),
                        null))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Company name is required");
        assertThatThrownBy(() -> policy.normalize(
                        requester,
                        command("parts@store.com", "PARTS_STORE_ADMIN", "Loja Existente", "PARTS_STORE", true, null),
                        null))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Company already exists");
    }

    @Test
    void shouldScopeCompanyAdminsToTheirOwnCompany() {
        Company workshop = companies.save(Company.create("Oficina Azul", "WORKSHOP"));
        Company otherWorkshop = companies.save(Company.create("Oficina Verde", "WORKSHOP"));
        User requester =
                user("admin@oficina.com", UserRole.ADMIN, "WORKSHOP_ADMIN", workshop.id(), "Oficina Azul", "WORKSHOP");
        User target = user(
                "employee@oficina.com",
                UserRole.EMPLOYEE,
                "WORKSHOP_EMPLOYEE",
                workshop.id(),
                "Oficina Azul",
                "WORKSHOP");
        User outside = user(
                "other@oficina.com",
                UserRole.EMPLOYEE,
                "WORKSHOP_EMPLOYEE",
                otherWorkshop.id(),
                "Oficina Verde",
                "WORKSHOP");

        UserManagementPolicy.ManagedUserCommand scoped = policy.normalize(
                requester,
                command("ignored@oficina.com", "PARTS_STORE_ADMIN", "Loja", "PARTS_STORE", true, null),
                target);

        assertThat(scoped.role()).isEqualTo("EMPLOYEE");
        assertThat(scoped.profileType()).isEqualTo("WORKSHOP_EMPLOYEE");
        assertThat(scoped.companyId()).isEqualTo(workshop.id());
        assertThat(scoped.employeeSubRole()).isEqualTo("UNSPECIFIED");
        assertThat(policy.canManageUser(requester, target)).isTrue();
        assertThat(policy.canManageUser(requester, outside)).isFalse();
        assertThat(policy.canSeeCompany(requester, workshop)).isTrue();
        assertThat(policy.canSeeCompany(requester, otherWorkshop)).isFalse();

        assertThatThrownBy(() -> policy.normalize(requester, scoped, outside))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("User is outside the current company scope");
        assertThatThrownBy(() -> policy.normalize(
                        requester, command("master@autocarehub.com", "MASTER_ADMIN", "", "", false, null), null))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Only master admin can create or update master admin users");
    }

    @Test
    void shouldRejectInvalidProfileType() {
        User requester = user("master@autocarehub.com", UserRole.ADMIN, "MASTER_ADMIN", null, "", "");

        assertThatThrownBy(() -> policy.normalize(
                        requester, command("invalid@autocarehub.com", "INVALID_PROFILE", "", "", false, null), null))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Invalid user profile type");
    }

    private static UserManagementPolicy.ManagedUserCommand command(
            String username,
            String profileType,
            String companyName,
            String companyType,
            boolean createCompany,
            UUID companyId) {
        return new UserManagementPolicy.ManagedUserCommand(
                username,
                "",
                null,
                companyId,
                "User",
                profileType,
                companyName,
                companyType,
                createCompany,
                "",
                List.of("USERS_READ"),
                true);
    }

    private static User user(
            String username,
            UserRole role,
            String profileType,
            UUID companyId,
            String companyName,
            String companyType) {
        return new User(
                UUID.randomUUID(),
                username,
                "{encoded}secret",
                role,
                null,
                companyId,
                "User",
                profileType,
                companyName,
                companyType,
                "",
                List.of("USERS_READ"),
                true,
                LocalDateTime.now());
    }

    private static class InMemoryCompanyRepository implements CompanyRepository {

        private final Map<UUID, Company> companies = new LinkedHashMap<>();

        @Override
        public Company save(Company company) {
            companies.put(company.id(), company);
            return company;
        }

        @Override
        public Optional<Company> findById(UUID id) {
            return Optional.ofNullable(companies.get(id));
        }

        @Override
        public Optional<Company> findByName(String name) {
            return companies.values().stream()
                    .filter(company -> company.name().equals(name))
                    .findFirst();
        }

        @Override
        public List<Company> findAll() {
            return List.copyOf(companies.values());
        }
    }
}
