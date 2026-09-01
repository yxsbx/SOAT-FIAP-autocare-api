package br.com.autocarehub.application.usecase.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.application.port.out.PasswordHasher;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.model.User;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateCustomerUseCaseTest {

    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();

    private static Address address() {
        return new Address("Avenida Paulista", "1000", null, "Bela Vista", "São Paulo", "SP", "01310-100");
    }

    @Test
    void shouldCreateCustomerWhenDocumentDõesNotExist() {
        CreateCustomerUseCase useCase = new CreateCustomerUseCase(repository);

        Customer customer = useCase.execute(new CreateCustomerUseCase.Command(
                "Maria Silva", "52998224725", "11999999999", "maria@example.com", address()));

        assertThat(customer.id()).isNotNull();
        assertThat(repository.findByDocument(Document.from("52998224725"))).isPresent();
    }

    @Test
    void shouldRejectDuplicatedDocument() {
        CreateCustomerUseCase useCase = new CreateCustomerUseCase(repository);
        CreateCustomerUseCase.Command command = new CreateCustomerUseCase.Command(
                "Maria Silva", "52998224725", "11999999999", "maria@example.com", address());
        useCase.execute(command);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Customer document already exists");
    }

    @Test
    void shouldCreateCustomerLoginWhenUserRepositoryIsConfigured() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        CreateCustomerUseCase useCase = new CreateCustomerUseCase(repository, userRepository, new TestPasswordHasher());

        Customer customer = useCase.execute(new CreateCustomerUseCase.Command(
                "Maria Silva", "52998224725", "11999999999", "maria@example.com", address()));

        User user = userRepository.findByUsername("maria@example.com").orElseThrow();
        assertThat(user.customerId()).isEqualTo(customer.id());
        assertThat(user.role().name()).isEqualTo("CUSTOMER");
        assertThat(user.profileType()).isEqualTo("CUSTOMER_OWNER");
        assertThat(user.passwordHash()).isEqualTo("encoded:admin");
    }

    @Test
    void shouldRejectCustomerWhenEmailAlreadyExistsAsLogin() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        userRepository.save(new User(
                UUID.randomUUID(),
                "maria@example.com",
                "encoded:any",
                br.com.autocarehub.domain.enums.UserRole.CUSTOMER,
                null,
                null,
                "Maria",
                "CUSTOMER_OWNER",
                "",
                "",
                "",
                List.of(),
                true,
                java.time.LocalDateTime.now()));
        CreateCustomerUseCase useCase = new CreateCustomerUseCase(repository, userRepository, new TestPasswordHasher());

        assertThatThrownBy(() -> useCase.execute(new CreateCustomerUseCase.Command(
                        "Maria Silva", "52998224725", "11999999999", "maria@example.com", address())))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Username already exists");
        assertThat(repository.findByDocument(Document.from("52998224725"))).isEmpty();
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

    private static class TestPasswordHasher implements PasswordHasher {

        @Override
        public String hash(String plainTextPassword) {
            return "encoded:" + plainTextPassword;
        }

        @Override
        public boolean matches(String plainTextPassword, String passwordHash) {
            return passwordHash.equals("encoded:" + plainTextPassword);
        }
    }
}
