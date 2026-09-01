package br.com.autocarehub.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.model.User;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthorizationServiceTest {

    private final InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
    private final InMemoryServiceOrderRepository serviceOrderRepository = new InMemoryServiceOrderRepository();

    private static Address address() {
        return new Address("Avenida Paulista", "1000", null, "Bela Vista", "São Paulo", "SP", "01310-100");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowCustomerToTrackOnlyOwnDocument() {
        Customer customer = customerRepository.save(new Customer(
                "Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address()));
        authenticateCustomer(customer.id());
        AuthorizationService authorizationService =
                new AuthorizationService(customerRepository, serviceOrderRepository);

        assertThat(authorizationService.canTrackServiceOrders(null, "529.982.247-25"))
                .isTrue();
        assertThat(authorizationService.canTrackServiceOrders(null, "153.509.460-56"))
                .isFalse();
    }

    @Test
    void shouldRejectCustomerAccessWhenAuthenticationIsMissingOrDifferentCustomer() {
        UUID customerId = UUID.randomUUID();
        AuthorizationService authorizationService =
                new AuthorizationService(customerRepository, serviceOrderRepository);

        assertThat(authorizationService.canAccessCustomer(customerId)).isFalse();

        authenticateCustomer(UUID.randomUUID());

        assertThat(authorizationService.canAccessCustomer(customerId)).isFalse();
    }

    @Test
    void shouldAllowCustomerToAccessOwnCustomerAndServiceOrder() {
        UUID customerId = UUID.randomUUID();
        ServiceOrder serviceOrder = serviceOrderRepository.save(
                new ServiceOrder(customerId, UUID.randomUUID(), "Cliente relata ruído ao frear"));
        authenticateCustomer(customerId);
        AuthorizationService authorizationService =
                new AuthorizationService(customerRepository, serviceOrderRepository);

        assertThat(authorizationService.canAccessCustomer(customerId)).isTrue();
        assertThat(authorizationService.canAccessServiceOrder(serviceOrder.id()))
                .isTrue();
        assertThat(authorizationService.canTrackServiceOrders(serviceOrder.id(), null))
                .isTrue();
    }

    @Test
    void shouldRejectServiceOrderAccessWithoutCustomerOrWhenOrderDõesNotBelongToCustomer() {
        ServiceOrder serviceOrder = serviceOrderRepository.save(
                new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata falha elétrica"));
        AuthorizationService authorizationService =
                new AuthorizationService(customerRepository, serviceOrderRepository);

        assertThat(authorizationService.canAccessServiceOrder(serviceOrder.id()))
                .isFalse();

        authenticate(UserRole.ADMIN, null);
        assertThat(authorizationService.canAccessServiceOrder(serviceOrder.id()))
                .isFalse();

        authenticateCustomer(UUID.randomUUID());
        assertThat(authorizationService.canAccessServiceOrder(serviceOrder.id()))
                .isFalse();
        assertThat(authorizationService.canAccessServiceOrder(UUID.randomUUID()))
                .isFalse();
    }

    @Test
    void shouldAllowAdminAndEmployeeToTrackAnyServiceOrder() {
        AuthorizationService authorizationService =
                new AuthorizationService(customerRepository, serviceOrderRepository);

        authenticate(UserRole.ADMIN, null);
        assertThat(authorizationService.canTrackServiceOrders(UUID.randomUUID(), null))
                .isTrue();

        authenticate(UserRole.EMPLOYEE, null);
        assertThat(authorizationService.canTrackServiceOrders(null, "bad-document"))
                .isTrue();
    }

    @Test
    void shouldRejectTrackingWhenAuthenticationOrCustomerContextIsMissing() {
        AuthorizationService authorizationService =
                new AuthorizationService(customerRepository, serviceOrderRepository);

        assertThat(authorizationService.canTrackServiceOrders(null, "52998224725"))
                .isFalse();

        authenticate(UserRole.CUSTOMER, null);
        assertThat(authorizationService.canTrackServiceOrders(null, "52998224725"))
                .isFalse();
        assertThat(authorizationService.canTrackServiceOrders(null, null)).isFalse();
        assertThat(authorizationService.canTrackServiceOrders(null, "   ")).isFalse();
    }

    @Test
    void shouldRejectTrackingWhenCustomerDocumentIsInvalid() {
        Customer customer = customerRepository.save(new Customer(
                "Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address()));
        authenticateCustomer(customer.id());
        AuthorizationService authorizationService =
                new AuthorizationService(customerRepository, serviceOrderRepository);

        assertThat(authorizationService.canTrackServiceOrders(null, "000")).isFalse();
    }

    private void authenticateCustomer(UUID customerId) {
        authenticate(UserRole.CUSTOMER, customerId);
    }

    private void authenticate(UserRole role, UUID customerId) {
        AuthenticatedUser user = new AuthenticatedUser(new User(
                UUID.randomUUID(),
                role.name().toLowerCase() + "@autocarehub.com",
                "$2a$10$hashhashhashhashhashhashhashhashhashhashhashhashhash",
                role,
                customerId,
                true,
                LocalDateTime.now()));
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
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
            return List.of();
        }

        @Override
        public List<ServiceOrder> findByCustomerId(UUID customerId) {
            return List.of();
        }

        @Override
        public List<ServiceOrder> findCompletedWithExecutionTime() {
            return List.of();
        }
    }
}
