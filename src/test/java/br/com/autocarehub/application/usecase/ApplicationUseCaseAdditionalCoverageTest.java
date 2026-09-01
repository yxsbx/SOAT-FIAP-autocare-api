package br.com.autocarehub.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.PasswordHasher;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.application.port.out.StockMovementRepository;
import br.com.autocarehub.application.port.out.UserPreferenceRepository;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.application.port.out.WorkshopServiceRepository;
import br.com.autocarehub.application.usecase.customer.DeleteCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.FindCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.ListCustomersUseCase;
import br.com.autocarehub.application.usecase.customer.UpdateCustomerUseCase;
import br.com.autocarehub.application.usecase.part.CommitPartReservationUseCase;
import br.com.autocarehub.application.usecase.part.ConfigurePartReservationUseCase;
import br.com.autocarehub.application.usecase.part.CreatePartUseCase;
import br.com.autocarehub.application.usecase.part.DeletePartUseCase;
import br.com.autocarehub.application.usecase.part.FindPartUseCase;
import br.com.autocarehub.application.usecase.part.ListPartsUseCase;
import br.com.autocarehub.application.usecase.part.ReleasePartReservationUseCase;
import br.com.autocarehub.application.usecase.part.ReservePartStockUseCase;
import br.com.autocarehub.application.usecase.part.UpdatePartStockUseCase;
import br.com.autocarehub.application.usecase.part.UpdatePartUseCase;
import br.com.autocarehub.application.usecase.serviceorder.AddPartToServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.AddServiceToServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ApproveServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.FindServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.GenerateServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ListServiceOrdersByCustomerUseCase;
import br.com.autocarehub.application.usecase.serviceorder.UpdateServiceOrderStatusUseCase;
import br.com.autocarehub.application.usecase.user.ChangeUserPasswordUseCase;
import br.com.autocarehub.application.usecase.user.CreateUserUseCase;
import br.com.autocarehub.application.usecase.user.GetUserPreferenceUseCase;
import br.com.autocarehub.application.usecase.user.GetUserUseCase;
import br.com.autocarehub.application.usecase.user.ListUsersUseCase;
import br.com.autocarehub.application.usecase.user.SaveUserPreferenceUseCase;
import br.com.autocarehub.application.usecase.user.UpdateUserUseCase;
import br.com.autocarehub.application.usecase.vehicle.CreateVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.DeleteVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.FindVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.ListVehiclesByCustomerUseCase;
import br.com.autocarehub.application.usecase.vehicle.ListVehiclesUseCase;
import br.com.autocarehub.application.usecase.vehicle.UpdateVehicleUseCase;
import br.com.autocarehub.application.usecase.workshopservice.CreateWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.DeleteWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.FindWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.ListWorkshopServicesUseCase;
import br.com.autocarehub.application.usecase.workshopservice.UpdateWorkshopServiceUseCase;
import br.com.autocarehub.domain.enums.DocumentType;
import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.model.Company;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.model.User;
import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.domain.valueobject.Plate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class ApplicationUseCaseAdditionalCoverageTest {

    private static final UUID COMPANY_ID = UUID.fromString("90000000-0000-0000-0000-000000000011");

    private static Address address() {
        return new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", "01001000");
    }

    private static Customer customer() {
        return new Customer("Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address());
    }

    private static User user(String username, String fullName, UserRole role, boolean active) {
        return new User(
                UUID.randomUUID(),
                username,
                "{encoded}secret",
                role,
                null,
                COMPANY_ID,
                fullName,
                role == UserRole.ADMIN ? "admin" : "employee",
                "AutoCare",
                "Oficina",
                role == UserRole.ADMIN ? "Gestor" : "Consultor",
                List.of("orders:read", "parts:write"),
                active,
                LocalDateTime.now());
    }

    @Test
    void shouldCoverCustomerFindListUpdateAndDeleteUseCases() {
        InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
        Customer active = repository.save(customer());
        Customer inactive = repository.save(new Customer(
                "Joao Souza", Document.from("11222333000181"), "11888888888", "joao@example.com", address()));
        inactive.deactivate();
        repository.save(inactive);

        assertThat(new FindCustomerUseCase(repository).execute(active.id())).isEqualTo(active);
        assertThat(new ListCustomersUseCase(repository).execute()).hasSize(2);
        assertThat(new ListCustomersUseCase(repository).execute(new ListCustomersUseCase.Query(true)))
                .extracting(Customer::id)
                .containsExactly(active.id());

        Customer updated = new UpdateCustomerUseCase(repository)
                .execute(new UpdateCustomerUseCase.Command(
                        active.id(), "Maria Souza", "11777777777", "souza@example.com", address(), false));

        assertThat(updated.name()).isEqualTo("Maria Souza");
        assertThat(updated.active()).isFalse();

        new DeleteCustomerUseCase(repository).execute(inactive.id());

        assertThat(repository.findById(inactive.id()).orElseThrow().active()).isFalse();
        assertThatThrownBy(() -> new FindCustomerUseCase(repository).execute(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    void shouldCoverVehicleUseCases() {
        InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
        Customer customer = customerRepository.save(customer());
        InMemoryVehicleRepository vehicleRepository = new InMemoryVehicleRepository();
        Vehicle vehicle = new CreateVehicleUseCase(vehicleRepository, customerRepository)
                .execute(new CreateVehicleUseCase.Command(customer.id(), "ABC1D23", "Honda", "Civic", 2020, 35000));

        assertThat(new FindVehicleUseCase(vehicleRepository).execute(vehicle.id()))
                .isEqualTo(vehicle);
        assertThat(new ListVehiclesUseCase(vehicleRepository).execute()).hasSize(1);
        assertThat(new ListVehiclesByCustomerUseCase(vehicleRepository, customerRepository).execute(customer.id()))
                .containsExactly(vehicle);

        Vehicle updated = new UpdateVehicleUseCase(vehicleRepository)
                .execute(new UpdateVehicleUseCase.Command(
                        vehicle.id(), "ABC1D23", "Honda", "Civic", 2020, 36000, false));

        assertThat(updated.plate()).isEqualTo(new Plate("ABC1D23"));
        assertThat(updated.active()).isFalse();
        assertThat(new ListVehiclesUseCase(vehicleRepository).execute(new ListVehiclesUseCase.Query(false)))
                .containsExactly(updated);
        assertThat(new ListVehiclesUseCase(vehicleRepository).execute(new ListVehiclesUseCase.Query(null)))
                .containsExactly(updated);
        assertThat(new ListVehiclesUseCase(vehicleRepository).execute(new ListVehiclesUseCase.Query(true)))
                .isEmpty();

        assertThatThrownBy(() -> new UpdateVehicleUseCase(vehicleRepository)
                        .execute(new UpdateVehicleUseCase.Command(
                                vehicle.id(), "ABC1D23", "Toyota", "Civic", 2020, 37000, true)))
                .isInstanceOf(DomainException.class)
                .hasMessage("Vehicle identity data cannot be changed; deactivate it and create a new vehicle");

        new DeleteVehicleUseCase(vehicleRepository).execute(vehicle.id());

        assertThat(vehicleRepository.findById(vehicle.id()).orElseThrow().active())
                .isFalse();
        assertThatThrownBy(() -> new CreateVehicleUseCase(vehicleRepository, customerRepository)
                        .execute(new CreateVehicleUseCase.Command(
                                UUID.randomUUID(), "GHI3J45", "Fiat", "Argo", 2022, 10000)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    void shouldCoverWorkshopServiceUseCases() {
        InMemoryWorkshopServiceRepository repository = new InMemoryWorkshopServiceRepository();
        WorkshopService service = new CreateWorkshopServiceUseCase(repository)
                .execute(new CreateWorkshopServiceUseCase.Command(
                        "Troca de oleo", "Troca de oleo e filtro", Money.of("120.00"), 60));

        assertThat(new FindWorkshopServiceUseCase(repository).execute(service.id()))
                .isEqualTo(service);
        assertThat(new ListWorkshopServicesUseCase(repository).execute()).containsExactly(service);

        WorkshopService updated = new UpdateWorkshopServiceUseCase(repository)
                .execute(new UpdateWorkshopServiceUseCase.Command(
                        service.id(),
                        "Troca completa",
                        "Troca completa de oleo e filtros",
                        Money.of("180.00"),
                        90,
                        false));

        assertThat(updated.active()).isFalse();
        assertThat(updated.basePrice().value()).isEqualByComparingTo("180.00");
        assertThat(new ListWorkshopServicesUseCase(repository).execute(new ListWorkshopServicesUseCase.Query(false)))
                .containsExactly(updated);
        assertThat(new ListWorkshopServicesUseCase(repository).execute(new ListWorkshopServicesUseCase.Query(null)))
                .containsExactly(updated);
        assertThat(new ListWorkshopServicesUseCase(repository).execute(new ListWorkshopServicesUseCase.Query(true)))
                .isEmpty();

        new DeleteWorkshopServiceUseCase(repository).execute(service.id());

        assertThat(repository.findById(service.id()).orElseThrow().active()).isFalse();
        assertThatThrownBy(() -> new FindWorkshopServiceUseCase(repository).execute(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workshop service not found");
    }

    @Test
    void shouldCoverPartUseCases() {
        InMemoryPartRepository partRepository = new InMemoryPartRepository();
        InMemoryStockMovementRepository movementRepository = new InMemoryStockMovementRepository();
        Part part = new CreatePartUseCase(partRepository)
                .execute(new CreatePartUseCase.Command(
                        "Filtro de oleo",
                        "Filtro de oleo do motor",
                        "OIL-001",
                        "Filtros",
                        "Oleo",
                        "Bosch",
                        Money.of("25.00"),
                        Money.of("50.00"),
                        10,
                        2));

        assertThat(new FindPartUseCase(partRepository).execute(part.id())).isEqualTo(part);
        assertThat(new ListPartsUseCase(partRepository).execute()).containsExactly(part);

        new ReservePartStockUseCase(partRepository).execute(new ReservePartStockUseCase.Command(part.id(), 3));
        assertThat(partRepository.findById(part.id()).orElseThrow().reservedQuantity())
                .isEqualTo(3);

        new ReleasePartReservationUseCase(partRepository)
                .execute(new ReleasePartReservationUseCase.Command(part.id(), 1));
        assertThat(partRepository.findById(part.id()).orElseThrow().reservedQuantity())
                .isEqualTo(2);

        new CommitPartReservationUseCase(partRepository, movementRepository)
                .execute(new CommitPartReservationUseCase.Command(part.id(), 2, "Venda"));
        assertThat(partRepository.findById(part.id()).orElseThrow().stockQuantity())
                .isEqualTo(8);
        assertThat(movementRepository.movements).hasSize(1);

        new ConfigurePartReservationUseCase(partRepository)
                .execute(new ConfigurePartReservationUseCase.Command(part.id(), 5));
        assertThat(partRepository.findById(part.id()).orElseThrow().reservationDays())
                .isEqualTo(5);

        new UpdatePartStockUseCase(partRepository).execute(new UpdatePartStockUseCase.Command(part.id(), 12));
        assertThat(partRepository.findById(part.id()).orElseThrow().stockQuantity())
                .isEqualTo(12);
        new UpdatePartStockUseCase(partRepository).execute(new UpdatePartStockUseCase.Command(part.id(), 9));
        assertThat(partRepository.findById(part.id()).orElseThrow().stockQuantity())
                .isEqualTo(9);

        Part updated = new UpdatePartUseCase(partRepository)
                .execute(new UpdatePartUseCase.Command(
                        part.id(),
                        "Filtro premium",
                        "Filtro premium do motor",
                        "OIL-002",
                        "Filtros",
                        "Oleo",
                        "Bosch",
                        Money.of("30.00"),
                        Money.of("80.00"),
                        11,
                        4,
                        false));
        assertThat(updated.active()).isFalse();
        assertThat(updated.stockQuantity()).isEqualTo(11);

        assertThat(new ListPartsUseCase(partRepository).execute(new ListPartsUseCase.Query(false, false)))
                .containsExactly(updated);
        new UpdatePartStockUseCase(partRepository).execute(new UpdatePartStockUseCase.Command(part.id(), 4));
        assertThat(new ListPartsUseCase(partRepository).execute(new ListPartsUseCase.Query(null, true)))
                .containsExactly(updated);

        new DeletePartUseCase(partRepository).execute(part.id());

        assertThat(partRepository.findById(part.id()).orElseThrow().active()).isFalse();
        assertThatThrownBy(() -> new FindPartUseCase(partRepository).execute(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Part not found");
    }

    @Test
    void shouldCoverUserUseCases() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryPasswordHasher passwordHasher = new InMemoryPasswordHasher();
        InMemoryUserPreferenceRepository preferenceRepository = new InMemoryUserPreferenceRepository();
        User admin = userRepository.save(user("admin", "Admin User", UserRole.ADMIN, true));
        User employee = userRepository.save(user("employee", "Employee User", UserRole.EMPLOYEE, false));

        assertThat(new GetUserUseCase(userRepository).execute(admin.id())).isEqualTo(admin);
        assertThat(new ListUsersUseCase(userRepository).execute(null))
                .extracting(User::id)
                .contains(admin.id(), employee.id());
        assertThat(new ListUsersUseCase(userRepository)
                        .execute(new ListUsersUseCase.Query(true, "ADMIN", "admin", "admin")))
                .extracting(User::id)
                .containsExactly(admin.id());
        assertThat(new ListUsersUseCase(userRepository)
                        .execute(new ListUsersUseCase.Query(false, "EMPLOYEE", "employee", "consultor")))
                .extracting(User::id)
                .containsExactly(employee.id());
        assertThat(new ListUsersUseCase(userRepository).execute(new ListUsersUseCase.Query(null, null, null, "user")))
                .extracting(User::id)
                .contains(admin.id(), employee.id());
        assertThat(new ListUsersUseCase(userRepository)
                        .execute(new ListUsersUseCase.Query(null, null, null, "employee")))
                .extracting(User::id)
                .containsExactly(employee.id());
        assertThat(new ListUsersUseCase(userRepository).execute(new ListUsersUseCase.Query(null, null, null, "admin")))
                .extracting(User::id)
                .containsExactly(admin.id());
        assertThat(new ListUsersUseCase(userRepository).execute(new ListUsersUseCase.Query(null, null, null, "gestor")))
                .extracting(User::id)
                .containsExactly(admin.id());
        assertThat(new ListUsersUseCase(userRepository)
                        .execute(new ListUsersUseCase.Query(null, null, null, "missing")))
                .isEmpty();

        User created = new CreateUserUseCase(userRepository, passwordHasher)
                .execute(new CreateUserUseCase.Command(
                        "consultor",
                        "plain",
                        "EMPLOYEE",
                        null,
                        COMPANY_ID,
                        "Consultor",
                        "employee",
                        "AutoCare",
                        "Oficina",
                        "Consultor",
                        List.of("orders:read"),
                        true));
        assertThat(created.passwordHash()).isEqualTo("encoded:plain");

        User updated = new UpdateUserUseCase(userRepository)
                .execute(new UpdateUserUseCase.Command(
                        created.id(),
                        "consultor2",
                        "ADMIN",
                        null,
                        COMPANY_ID,
                        "Consultor Dois",
                        "admin",
                        "AutoCare",
                        "Oficina",
                        "Gestor",
                        List.of("users:write"),
                        false));
        assertThat(updated.username()).isEqualTo("consultor2");
        assertThat(updated.role()).isEqualTo(UserRole.ADMIN);
        assertThat(updated.active()).isFalse();

        User keptIdentity = new UpdateUserUseCase(userRepository)
                .execute(new UpdateUserUseCase.Command(
                        updated.id(),
                        "   ",
                        "",
                        null,
                        COMPANY_ID,
                        "Consultor Tres",
                        "admin",
                        "AutoCare",
                        "Oficina",
                        "Gestor",
                        List.of("users:read"),
                        true));
        assertThat(keptIdentity.username()).isEqualTo("consultor2");
        assertThat(keptIdentity.role()).isEqualTo(UserRole.ADMIN);
        assertThat(keptIdentity.active()).isTrue();

        User nullIdentity = new UpdateUserUseCase(userRepository)
                .execute(new UpdateUserUseCase.Command(
                        keptIdentity.id(),
                        null,
                        null,
                        null,
                        COMPANY_ID,
                        "Consultor Quatro",
                        "admin",
                        "AutoCare",
                        "Oficina",
                        "Gestor",
                        List.of("users:read"),
                        true));
        assertThat(nullIdentity.username()).isEqualTo("consultor2");
        assertThat(nullIdentity.role()).isEqualTo(UserRole.ADMIN);

        new ChangeUserPasswordUseCase(userRepository, passwordHasher)
                .execute(new ChangeUserPasswordUseCase.Command(updated.id(), "plain", "new-secret", true));
        assertThat(userRepository.findById(updated.id()).orElseThrow().passwordHash())
                .isEqualTo("encoded:new-secret");

        assertThat(new GetUserPreferenceUseCase(preferenceRepository)
                        .execute(admin.id(), "home", "{\"page\":\"dashboard\"}"))
                .isEqualTo("{\"page\":\"dashboard\"}");
        assertThat(new SaveUserPreferenceUseCase(preferenceRepository)
                        .execute(admin.id(), "home", "{\"page\":\"orders\"}"))
                .isEqualTo("{\"page\":\"orders\"}");
        assertThat(new GetUserPreferenceUseCase(preferenceRepository).execute(admin.id(), "home", "{}"))
                .isEqualTo("{\"page\":\"orders\"}");

        assertThatThrownBy(() -> new CreateUserUseCase(userRepository, passwordHasher)
                        .execute(new CreateUserUseCase.Command(
                                "admin",
                                "plain",
                                "ADMIN",
                                null,
                                COMPANY_ID,
                                "Admin",
                                "admin",
                                "",
                                "",
                                "",
                                List.of(),
                                true)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Username already exists");
        assertThatThrownBy(() -> new UpdateUserUseCase(userRepository)
                        .execute(new UpdateUserUseCase.Command(
                                updated.id(),
                                "admin",
                                "ADMIN",
                                null,
                                COMPANY_ID,
                                "Duplicado",
                                "admin",
                                "AutoCare",
                                "Oficina",
                                "Gestor",
                                List.of(),
                                true)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Username already exists");
        assertThatThrownBy(() -> new UpdateUserUseCase(userRepository)
                        .execute(new UpdateUserUseCase.Command(
                                UUID.randomUUID(),
                                "missing",
                                "ADMIN",
                                null,
                                COMPANY_ID,
                                "Missing",
                                "admin",
                                "AutoCare",
                                "Oficina",
                                "Gestor",
                                List.of(),
                                true)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
        assertThatThrownBy(() -> new ChangeUserPasswordUseCase(userRepository, passwordHasher)
                        .execute(new ChangeUserPasswordUseCase.Command(updated.id(), "wrong", "new-secret", true)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Current password is invalid");
    }

    @Test
    void shouldCoverCompanyAndDocumentBranches() {
        Company company = Company.create(" Oficina Teste ", "workshop");

        assertThat(company.name()).isEqualTo("Oficina Teste");
        assertThat(company.type()).isEqualTo("WORKSHOP");
        assertThat(company.active()).isTrue();

        assertThatThrownBy(() -> new Company(UUID.randomUUID(), " ", "WORKSHOP", true, LocalDateTime.now()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Company name is required");
        assertThatThrownBy(() -> Company.create("Empresa", ""))
                .isInstanceOf(DomainException.class)
                .hasMessage("Company type is required");
        assertThatThrownBy(() -> Company.create("Empresa", "INVALID"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid company type");

        assertThatThrownBy(() -> new Document(DocumentType.CPF, "11222333000181"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid document");
        assertThatThrownBy(() -> new Document(DocumentType.CNPJ, "11111111111111"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid document");
        assertThatThrownBy(() -> new Document(null, "52998224725"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("type is required");
    }

    @Test
    void shouldCoverMissingResourceBranchesAcrossUseCases() {
        UUID missingId = UUID.randomUUID();
        InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
        InMemoryVehicleRepository vehicleRepository = new InMemoryVehicleRepository();
        InMemoryWorkshopServiceRepository workshopServiceRepository = new InMemoryWorkshopServiceRepository();
        InMemoryPartRepository partRepository = new InMemoryPartRepository();
        InMemoryServiceOrderRepository serviceOrderRepository = new InMemoryServiceOrderRepository();
        InMemoryStockMovementRepository movementRepository = new InMemoryStockMovementRepository();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryPasswordHasher passwordHasher = new InMemoryPasswordHasher();

        assertThatThrownBy(() -> new UpdateCustomerUseCase(customerRepository)
                        .execute(new UpdateCustomerUseCase.Command(
                                missingId, "Nome", "11999999999", "mail@example.com", address(), true)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
        assertThatThrownBy(() -> new DeleteCustomerUseCase(customerRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
        assertThatThrownBy(() -> new FindVehicleUseCase(vehicleRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");
        assertThatThrownBy(() -> new DeleteVehicleUseCase(vehicleRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");
        assertThatThrownBy(() ->
                        new ListVehiclesByCustomerUseCase(vehicleRepository, customerRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
        assertThatThrownBy(() -> new UpdateVehicleUseCase(vehicleRepository)
                        .execute(new UpdateVehicleUseCase.Command(
                                missingId, "ABC1D23", "Honda", "Civic", 2020, 10000, true)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");

        assertThatThrownBy(() -> new FindWorkshopServiceUseCase(workshopServiceRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workshop service not found");
        assertThatThrownBy(() -> new DeleteWorkshopServiceUseCase(workshopServiceRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workshop service not found");
        assertThatThrownBy(() -> new UpdateWorkshopServiceUseCase(workshopServiceRepository)
                        .execute(new UpdateWorkshopServiceUseCase.Command(
                                missingId, "Serviço", "Descrição", Money.of("10.00"), 30, true)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workshop service not found");

        assertThatThrownBy(() -> new DeletePartUseCase(partRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Part not found");
        assertThatThrownBy(() -> new ReservePartStockUseCase(partRepository)
                        .execute(new ReservePartStockUseCase.Command(missingId, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Part not found");
        assertThatThrownBy(() -> new ReleasePartReservationUseCase(partRepository)
                        .execute(new ReleasePartReservationUseCase.Command(missingId, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Part not found");
        assertThatThrownBy(() -> new ConfigurePartReservationUseCase(partRepository)
                        .execute(new ConfigurePartReservationUseCase.Command(missingId, 3)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Part not found");
        assertThatThrownBy(() -> new UpdatePartStockUseCase(partRepository)
                        .execute(new UpdatePartStockUseCase.Command(missingId, 10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Part not found");
        assertThatThrownBy(() -> new CommitPartReservationUseCase(partRepository, movementRepository)
                        .execute(new CommitPartReservationUseCase.Command(missingId, 1, "Venda")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Part not found");

        assertThatThrownBy(() -> new FindServiceOrderUseCase(serviceOrderRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found");
        assertThatThrownBy(() -> new AddServiceToServiceOrderUseCase(serviceOrderRepository, workshopServiceRepository)
                        .execute(new AddServiceToServiceOrderUseCase.Command(missingId, missingId, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found");
        assertThatThrownBy(() -> new AddPartToServiceOrderUseCase(serviceOrderRepository, partRepository)
                        .execute(new AddPartToServiceOrderUseCase.Command(missingId, missingId, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found");
        assertThatThrownBy(() -> new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository)
                        .execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found");
        assertThatThrownBy(() ->
                        new ApproveServiceOrderBudgetUseCase(serviceOrderRepository, partRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found");
        assertThatThrownBy(() -> new UpdateServiceOrderStatusUseCase(serviceOrderRepository, partRepository)
                        .execute(new UpdateServiceOrderStatusUseCase.Command(missingId, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found");
        assertThatThrownBy(() -> new ListServiceOrdersByCustomerUseCase(serviceOrderRepository, customerRepository)
                        .execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");

        assertThatThrownBy(() -> new GetUserUseCase(userRepository).execute(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
        assertThatThrownBy(() -> new ChangeUserPasswordUseCase(userRepository, passwordHasher)
                        .execute(new ChangeUserPasswordUseCase.Command(missingId, "old", "new", true)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    private static class InMemoryCustomerRepository implements CustomerRepository {

        private final Map<UUID, Customer> customers = new LinkedHashMap<>();

        @Override
        public Customer save(Customer customer) {
            customers.put(customer.id(), customer);
            return customer;
        }

        @Override
        public Optional<Customer> findById(UUID id) {
            return Optional.ofNullable(customers.get(id));
        }

        @Override
        public Optional<Customer> findByDocument(Document document) {
            return customers.values().stream()
                    .filter(customer -> customer.document().equals(document))
                    .findFirst();
        }

        @Override
        public List<Customer> findAll() {
            return List.copyOf(customers.values());
        }
    }

    private static class InMemoryVehicleRepository implements VehicleRepository {

        private final Map<UUID, Vehicle> vehicles = new LinkedHashMap<>();

        @Override
        public Vehicle save(Vehicle vehicle) {
            vehicles.put(vehicle.id(), vehicle);
            return vehicle;
        }

        @Override
        public Optional<Vehicle> findById(UUID id) {
            return Optional.ofNullable(vehicles.get(id));
        }

        @Override
        public List<Vehicle> findAll() {
            return List.copyOf(vehicles.values());
        }

        @Override
        public List<Vehicle> findByCustomerId(UUID customerId) {
            return vehicles.values().stream()
                    .filter(vehicle -> vehicle.customerId().equals(customerId))
                    .toList();
        }
    }

    private static class InMemoryWorkshopServiceRepository implements WorkshopServiceRepository {

        private final Map<UUID, WorkshopService> services = new LinkedHashMap<>();

        @Override
        public WorkshopService save(WorkshopService workshopService) {
            services.put(workshopService.id(), workshopService);
            return workshopService;
        }

        @Override
        public Optional<WorkshopService> findById(UUID id) {
            return Optional.ofNullable(services.get(id));
        }

        @Override
        public List<WorkshopService> findAll() {
            return List.copyOf(services.values());
        }
    }

    private static class InMemoryPartRepository implements PartRepository {

        private final Map<UUID, Part> parts = new LinkedHashMap<>();

        @Override
        public Part save(Part part) {
            parts.put(part.id(), part);
            return part;
        }

        @Override
        public Optional<Part> findById(UUID id) {
            return Optional.ofNullable(parts.get(id));
        }

        @Override
        public List<Part> findAll() {
            return List.copyOf(parts.values());
        }
    }

    private static class InMemoryServiceOrderRepository implements ServiceOrderRepository {

        private final Map<UUID, ServiceOrder> serviceOrders = new LinkedHashMap<>();

        @Override
        public ServiceOrder save(ServiceOrder serviceOrder) {
            serviceOrders.put(serviceOrder.id(), serviceOrder);
            return serviceOrder;
        }

        @Override
        public Optional<ServiceOrder> findById(UUID id) {
            return Optional.ofNullable(serviceOrders.get(id));
        }

        @Override
        public List<ServiceOrder> findAll() {
            return List.copyOf(serviceOrders.values());
        }

        @Override
        public List<ServiceOrder> findByCustomerId(UUID customerId) {
            return serviceOrders.values().stream()
                    .filter(serviceOrder -> serviceOrder.customerId().equals(customerId))
                    .toList();
        }

        @Override
        public List<ServiceOrder> findCompletedWithExecutionTime() {
            return List.of();
        }
    }

    private static class InMemoryStockMovementRepository implements StockMovementRepository {

        private final List<String> movements = new ArrayList<>();

        @Override
        public void register(
                UUID partId,
                String movementType,
                int quantity,
                BigDecimal unitCost,
                BigDecimal unitPrice,
                String reason) {
            movements.add(partId + ":" + movementType + ":" + quantity + ":" + reason);
        }
    }

    private static class InMemoryUserRepository implements UserRepository {

        private final Map<UUID, User> users = new LinkedHashMap<>();

        @Override
        public User save(User user) {
            users.put(user.id(), user);
            return user;
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return users.values().stream()
                    .filter(user -> user.username().equals(username))
                    .findFirst();
        }

        @Override
        public List<User> findAll() {
            return List.copyOf(users.values());
        }
    }

    private static class InMemoryUserPreferenceRepository implements UserPreferenceRepository {

        private final Map<String, String> values = new LinkedHashMap<>();

        @Override
        public Optional<String> findValue(UUID userId, String key) {
            return Optional.ofNullable(values.get(userId + ":" + key));
        }

        @Override
        public String saveValue(UUID userId, String key, String valueJson) {
            values.put(userId + ":" + key, valueJson);
            return valueJson;
        }
    }

    private static class InMemoryPasswordHasher implements PasswordHasher {

        @Override
        public String hash(String plainTextPassword) {
            return "encoded:" + plainTextPassword;
        }

        @Override
        public boolean matches(String plainTextPassword, @Nullable String passwordHash) {
            return passwordHash != null
                    && (passwordHash.equals("encoded:" + plainTextPassword)
                            || passwordHash.equals("{encoded}" + plainTextPassword));
        }
    }
}
