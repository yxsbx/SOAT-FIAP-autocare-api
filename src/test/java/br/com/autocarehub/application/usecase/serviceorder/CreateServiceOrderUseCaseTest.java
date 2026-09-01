package br.com.autocarehub.application.usecase.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.exception.ApplicationException;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.application.port.out.WorkshopServiceRepository;
import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.domain.valueobject.Plate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateServiceOrderUseCaseTest {

    private final InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
    private final InMemoryVehicleRepository vehicleRepository = new InMemoryVehicleRepository();
    private final InMemoryServiceOrderRepository serviceOrderRepository = new InMemoryServiceOrderRepository();
    private final InMemoryWorkshopServiceRepository workshopServiceRepository = new InMemoryWorkshopServiceRepository();
    private final InMemoryPartRepository partRepository = new InMemoryPartRepository();

    private static Address address() {
        return new Address("Avenida Paulista", "1000", null, "Bela Vista", "São Paulo", "SP", "01310-100");
    }

    private static Part part() {
        return Part.create(
                new Part.CatalogData("Filtro de oleo", "Filtro de oleo", "FILTRO-001", "Filtros", "Oleo", "Bosch"),
                Part.Pricing.withoutCost(Money.of("40.00")),
                10,
                2);
    }

    @Test
    void shouldCreateCustomerVehicleServiceOrderAndBudgetInSingleFlow() {
        WorkshopService service = workshopServiceRepository.save(
                new WorkshopService("Troca de oleo", "Substituição de oleo do motor", Money.of("120.00"), 60));
        Part part = partRepository.save(part());
        CreateServiceOrderUseCase useCase = useCase();

        ServiceOrder serviceOrder = useCase.execute(new CreateServiceOrderUseCase.Command(
                "52998224725",
                new CreateServiceOrderUseCase.CustomerInput(
                        "Maria Silva", "11999999999", "maria@example.com", address()),
                null,
                new CreateServiceOrderUseCase.VehicleInput("ABC1D23", "Honda", "Civic", 2020, 35000),
                "Cliente relata barulho no motor",
                List.of(new CreateServiceOrderUseCase.ServiceInput(service.id(), 2)),
                List.of(new CreateServiceOrderUseCase.PartInput(part.id(), 1)),
                true));

        assertThat(customerRepository.findByDocument(Document.from("52998224725")))
                .isPresent();
        assertThat(vehicleRepository.findByCustomerId(serviceOrder.customerId()))
                .hasSize(1);
        assertThat(serviceOrder.status()).isEqualTo(ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(serviceOrder.servicesTotal().value()).isEqualByComparingTo("240.00");
        assertThat(serviceOrder.partsTotal().value()).isEqualByComparingTo("40.00");
        assertThat(serviceOrder.totalAmount().value()).isEqualByComparingTo("280.00");
        assertThat(serviceOrder.budgetGeneratedAt()).isNotNull();
        assertThat(partRepository.findById(part.id()).orElseThrow().reservedQuantity())
                .isEqualTo(1);
        assertThat(partRepository.findById(part.id()).orElseThrow().stockQuantity())
                .isEqualTo(10);
    }

    @Test
    void shouldReuseExistingCustomerVehicleAndSkipBudgetWhenRequested() {
        Customer customer = customerRepository.save(new Customer(
                "Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address()));
        Vehicle vehicle =
                vehicleRepository.save(new Vehicle(customer.id(), new Plate("ABC1D23"), "Honda", "Civic", 2020, 35000));
        WorkshopService service = workshopServiceRepository.save(
                new WorkshopService("Troca de oleo", "Substituição de oleo do motor", Money.of("120.00"), 60));
        CreateServiceOrderUseCase useCase = useCase();

        ServiceOrder serviceOrder = useCase.execute(new CreateServiceOrderUseCase.Command(
                customer.document().value(),
                null,
                null,
                new CreateServiceOrderUseCase.VehicleInput(vehicle.plate().value(), "Honda", "Civic", 2020, 36000),
                "Cliente relata barulho no motor",
                List.of(new CreateServiceOrderUseCase.ServiceInput(service.id(), 1)),
                null,
                false));

        assertThat(serviceOrder.customerId()).isEqualTo(customer.id());
        assertThat(serviceOrder.vehicleId()).isEqualTo(vehicle.id());
        assertThat(serviceOrder.status()).isEqualTo(ServiceOrderStatus.RECEBIDA);
        assertThat(serviceOrder.budgetGeneratedAt()).isNull();
    }

    @Test
    void shouldRejectServiceOrderWithoutRequestedServices() {
        CreateServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        "52998224725",
                        new CreateServiceOrderUseCase.CustomerInput(
                                "Maria Silva", "11999999999", "maria@example.com", address()),
                        null,
                        new CreateServiceOrderUseCase.VehicleInput("ABC1D23", "Honda", "Civic", 2020, 35000),
                        "Cliente relata barulho no motor",
                        List.of(),
                        List.of(),
                        true)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Service order must have at least one requested service");
    }

    @Test
    void shouldRejectServiceOrderWhenCustomerDõesNotExistAndCustomerDataWasNotProvided() {
        WorkshopService service = workshopServiceRepository.save(
                new WorkshopService("Troca de oleo", "Substituição de oleo do motor", Money.of("120.00"), 60));
        CreateServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        "52998224725",
                        null,
                        null,
                        new CreateServiceOrderUseCase.VehicleInput("ABC1D23", "Honda", "Civic", 2020, 35000),
                        "Cliente relata barulho no motor",
                        List.of(new CreateServiceOrderUseCase.ServiceInput(service.id(), 1)),
                        List.of(),
                        true)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Customer not found and customer data was not provided");
    }

    @Test
    void shouldRejectServiceOrderWhenVehicleDõesNotExistAndVehicleDataWasNotProvided() {
        WorkshopService service = workshopServiceRepository.save(
                new WorkshopService("Troca de oleo", "Substituição de oleo do motor", Money.of("120.00"), 60));
        customerRepository.save(
                new Customer("Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", null));
        CreateServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        "52998224725",
                        null,
                        null,
                        null,
                        "Cliente relata barulho no motor",
                        List.of(new CreateServiceOrderUseCase.ServiceInput(service.id(), 1)),
                        List.of(),
                        true)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Vehicle not found and vehicle data was not provided");
    }

    @Test
    void shouldRejectServiceOrderWhenVehicleDõesNotBelongToCustomer() {
        WorkshopService service = workshopServiceRepository.save(
                new WorkshopService("Troca de oleo", "Substituição de oleo do motor", Money.of("120.00"), 60));
        Customer customer = customerRepository.save(
                new Customer("Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", null));
        Customer otherCustomer = customerRepository.save(
                new Customer("Joao Souza", Document.from("45678912364"), "11888888888", "joao@example.com", null));
        Vehicle otherCustomerVehicle = vehicleRepository.save(
                new Vehicle(otherCustomer.id(), new Plate("ABC1D23"), "Honda", "Civic", 2020, 35000));
        CreateServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        customer.document().value(),
                        null,
                        otherCustomerVehicle.id(),
                        null,
                        "Cliente relata barulho no motor",
                        List.of(new CreateServiceOrderUseCase.ServiceInput(service.id(), 1)),
                        List.of(),
                        true)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Vehicle dões not belong to customer");
    }

    @Test
    void shouldRejectInactiveServiceAndInactivePart() {
        Customer customer = customerRepository.save(new Customer(
                "Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address()));
        Vehicle vehicle =
                vehicleRepository.save(new Vehicle(customer.id(), new Plate("ABC1D23"), "Honda", "Civic", 2020, 35000));
        WorkshopService inactiveService = workshopServiceRepository.save(
                new WorkshopService("Troca de oleo", "Substituição de oleo do motor", Money.of("120.00"), 60));
        inactiveService.deactivate();
        Part inactivePart = partRepository.save(part());
        inactivePart.deactivate();
        WorkshopService activeService = workshopServiceRepository.save(
                new WorkshopService("Alinhamento", "Alinhamento completo", Money.of("90.00"), 45));
        CreateServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        customer.document().value(),
                        null,
                        vehicle.id(),
                        null,
                        "Cliente relata barulho no motor",
                        List.of(new CreateServiceOrderUseCase.ServiceInput(inactiveService.id(), 1)),
                        List.of(),
                        false)))
                .isInstanceOf(DomainException.class)
                .hasMessage("Workshop service is inactive");
        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        customer.document().value(),
                        null,
                        vehicle.id(),
                        null,
                        "Cliente relata barulho no motor",
                        List.of(new CreateServiceOrderUseCase.ServiceInput(activeService.id(), 1)),
                        List.of(new CreateServiceOrderUseCase.PartInput(inactivePart.id(), 1)),
                        false)))
                .isInstanceOf(DomainException.class)
                .hasMessage("Part is inactive");
    }

    @Test
    void shouldRejectMissingVehicleServiceAndPartReferences() {
        Customer customer = customerRepository.save(new Customer(
                "Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address()));
        Vehicle vehicle =
                vehicleRepository.save(new Vehicle(customer.id(), new Plate("ABC1D23"), "Honda", "Civic", 2020, 35000));
        WorkshopService service = workshopServiceRepository.save(
                new WorkshopService("Alinhamento", "Alinhamento completo", Money.of("90.00"), 45));
        CreateServiceOrderUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        customer.document().value(),
                        null,
                        UUID.randomUUID(),
                        null,
                        "Cliente relata barulho no motor",
                        List.of(new CreateServiceOrderUseCase.ServiceInput(service.id(), 1)),
                        List.of(),
                        false)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Vehicle not found");
        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        customer.document().value(),
                        null,
                        vehicle.id(),
                        null,
                        "Cliente relata barulho no motor",
                        List.of(new CreateServiceOrderUseCase.ServiceInput(UUID.randomUUID(), 1)),
                        List.of(),
                        false)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Workshop service not found");
        assertThatThrownBy(() -> useCase.execute(new CreateServiceOrderUseCase.Command(
                        customer.document().value(),
                        null,
                        vehicle.id(),
                        null,
                        "Cliente relata barulho no motor",
                        List.of(new CreateServiceOrderUseCase.ServiceInput(service.id(), 1)),
                        List.of(new CreateServiceOrderUseCase.PartInput(UUID.randomUUID(), 1)),
                        false)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Part not found");
    }

    private CreateServiceOrderUseCase useCase() {
        return new CreateServiceOrderUseCase(
                serviceOrderRepository,
                customerRepository,
                vehicleRepository,
                workshopServiceRepository,
                partRepository);
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
}
