package br.com.autocarehub.application.usecase.customer;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.application.port.out.PasswordHasher;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.model.User;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class CreateCustomerUseCase {

    private static final String INITIAL_CUSTOMER_PASSWORD = "admin";

    private final CustomerRepository customerRepository;
    private final @Nullable UserRepository userRepository;
    private final @Nullable PasswordHasher passwordHasher;

    public CreateCustomerUseCase(CustomerRepository customerRepository) {
        this(customerRepository, null, null);
    }

    public CreateCustomerUseCase(
            CustomerRepository customerRepository,
            @Nullable UserRepository userRepository,
            @Nullable PasswordHasher passwordHasher) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public Customer execute(Command command) {
        Document document = Document.from(command.document());
        customerRepository.findByDocument(document).ifPresent(customer -> {
            throw new ApplicationException("Customer document already exists");
        });
        if (userRepository != null) {
            userRepository.findByUsername(command.email()).ifPresent(user -> {
                throw new ApplicationException("Username already exists");
            });
        }
        Customer customer = new Customer(command.name(), document, command.phone(), command.email(), command.address());
        Customer savedCustomer = customerRepository.save(customer);
        if (userRepository != null && passwordHasher != null) {
            userRepository.save(new User(
                    UUID.randomUUID(),
                    savedCustomer.email(),
                    passwordHasher.hash(INITIAL_CUSTOMER_PASSWORD),
                    UserRole.CUSTOMER,
                    savedCustomer.id(),
                    null,
                    savedCustomer.name(),
                    "CUSTOMER_OWNER",
                    "",
                    "",
                    "",
                    List.of(),
                    true,
                    LocalDateTime.now()));
        }
        return savedCustomer;
    }

    public record Command(String name, String document, String phone, String email, Address address) {}
}
