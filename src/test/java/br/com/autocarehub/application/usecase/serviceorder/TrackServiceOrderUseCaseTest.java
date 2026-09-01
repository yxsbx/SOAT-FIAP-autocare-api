package br.com.autocarehub.application.usecase.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import br.com.autocarehub.domain.valueobject.Plate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TrackServiceOrderUseCaseTest {

    private final InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
    private final InMemoryVehicleRepository vehicleRepository = new InMemoryVehicleRepository();
    private final InMemoryServiceOrderRepository serviceOrderRepository = new InMemoryServiceOrderRepository();

    private static Address address() {
        return new Address("Avenida Paulista", "1000", null, "Bela Vista", "São Paulo", "SP", "01310-100");
    }

    @Test
    void shouldTrackServiceOrderById() {
        Seed seed = seed();
        TrackServiceOrderUseCase useCase = useCase();

        List<TrackServiceOrderUseCase.Output> outputs = useCase.execute(
                new TrackServiceOrderUseCase.Query(seed.serviceOrder().id(), null, null));

        assertThat(outputs).hasSize(1);
        assertThat(outputs.getFirst().serviceOrder().id())
                .isEqualTo(seed.serviceOrder().id());
        assertThat(outputs.getFirst().customer().document().value()).isEqualTo("52998224725");
        assertThat(outputs.getFirst().vehicle().plate().value()).isEqualTo("ABC1D23");
    }

    @Test
    void shouldTrackServiceOrdersByCustomerDocumentAndPlate() {
        Seed seed = seed();
        TrackServiceOrderUseCase useCase = useCase();

        List<TrackServiceOrderUseCase.Output> outputs = useCase.execute(new TrackServiceOrderUseCase.Query(
                null, seed.customer().document().value(), "ABC1D23"));

        assertThat(outputs)
                .extracting(output -> output.serviceOrder().id())
                .containsExactly(seed.serviceOrder().id());
    }

    @Test
    void shouldTrackServiceOrdersByCustomerDocumentOnly() {
        Seed seed = seed();
        TrackServiceOrderUseCase useCase = useCase();

        List<TrackServiceOrderUseCase.Output> outputs = useCase.execute(new TrackServiceOrderUseCase.Query(
                null, seed.customer().document().value(), null));

        assertThat(outputs)
                .extracting(output -> output.serviceOrder().id())
                .containsExactly(seed.serviceOrder().id());
    }

    @Test
    void shouldTrackServiceOrdersByPlateOnly() {
        Seed seed = seed();
        TrackServiceOrderUseCase useCase = useCase();

        List<TrackServiceOrderUseCase.Output> outputs =
                useCase.execute(new TrackServiceOrderUseCase.Query(null, null, "ABC1D23"));

        assertThat(outputs)
                .extracting(output -> output.serviceOrder().id())
                .containsExactly(seed.serviceOrder().id());
    }

    @Test
    void shouldRejectTrackingWithoutEnoughFilters() {
        TrackServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new TrackServiceOrderUseCase.Query(null, null, null)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Provide serviceOrderId, customerDocument or plate to track a service order");
    }

    @Test
    void shouldRejectTrackingWhenFiltersDoNotMatchServiceOrder() {
        Seed seed = seed();
        TrackServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(
                        new TrackServiceOrderUseCase.Query(seed.serviceOrder().id(), "11222333000181", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found for customer document");
        assertThatThrownBy(() -> useCase.execute(
                        new TrackServiceOrderUseCase.Query(seed.serviceOrder().id(), null, "DEF2G34")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found for vehicle plate");
    }

    @Test
    void shouldRejectTrackingWhenReferencesDoNotExist() {
        Seed seed = seed();
        TrackServiceOrderUseCase useCase = useCase();

        vehicleRepository.vehicles.clear();
        assertThatThrownBy(() -> useCase.execute(
                        new TrackServiceOrderUseCase.Query(seed.serviceOrder().id(), null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");

        assertThatThrownBy(() -> useCase.execute(new TrackServiceOrderUseCase.Query(
                        null, seed.customer().document().value(), null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");
    }

    @Test
    void shouldRejectTrackingWhenCustomerOrPlateDoNotExist() {
        seed();
        TrackServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new TrackServiceOrderUseCase.Query(null, "11222333000181", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
        assertThatThrownBy(() -> useCase.execute(new TrackServiceOrderUseCase.Query(null, null, "DEF2G34")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");
        assertThatThrownBy(() -> useCase.execute(new TrackServiceOrderUseCase.Query(null, "52998224725", "DEF2G34")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");
    }

    private TrackServiceOrderUseCase useCase() {
        return new TrackServiceOrderUseCase(serviceOrderRepository, customerRepository, vehicleRepository);
    }

    private Seed seed() {
        Customer customer = customerRepository.save(new Customer(
                "Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address()));
        Vehicle vehicle =
                vehicleRepository.save(new Vehicle(customer.id(), new Plate("ABC1D23"), "Honda", "Civic", 2020, 35000));
        ServiceOrder serviceOrder = serviceOrderRepository.save(
                new ServiceOrder(customer.id(), vehicle.id(), "Cliente relata barulho no motor"));
        return new Seed(customer, vehicle, serviceOrder);
    }

    private record Seed(Customer customer, Vehicle vehicle, ServiceOrder serviceOrder) {}

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
}
