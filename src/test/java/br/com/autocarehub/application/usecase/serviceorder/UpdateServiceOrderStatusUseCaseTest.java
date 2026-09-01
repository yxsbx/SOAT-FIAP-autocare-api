package br.com.autocarehub.application.usecase.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.exception.InvalidServiceOrderStatusTransitionException;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.domain.valueobject.Money;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpdateServiceOrderStatusUseCaseTest {

    private final InMemoryServiceOrderRepository serviceOrderRepository = new InMemoryServiceOrderRepository();
    private final InMemoryPartRepository partRepository = new InMemoryPartRepository();

    private static ServiceOrder serviceOrderWithGeneratedAndApprovedBudget() {
        ServiceOrder serviceOrder = serviceOrderWithGeneratedBudget();
        serviceOrder.approveBudget();
        return serviceOrder;
    }

    private static ServiceOrder serviceOrderWithGeneratedBudget() {
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrder.addService(
                new WorkshopService("Troca de oleo", "Substituição de oleo e filtro", Money.of("100.00"), 60), 1);
        serviceOrder.generateBudget();
        return serviceOrder;
    }

    @Test
    void shouldMoveApprovedBudgetToExecutionFinishedAndDelivered() {
        ServiceOrder serviceOrder = serviceOrderWithGeneratedAndApprovedBudget();
        serviceOrderRepository.save(serviceOrder);
        UpdateServiceOrderStatusUseCase useCase =
                new UpdateServiceOrderStatusUseCase(serviceOrderRepository, partRepository);

        useCase.execute(new UpdateServiceOrderStatusUseCase.Command(serviceOrder.id(), ServiceOrderStatus.EM_EXECUCAO));
        useCase.execute(new UpdateServiceOrderStatusUseCase.Command(serviceOrder.id(), ServiceOrderStatus.FINALIZADA));
        ServiceOrder delivered = useCase.execute(
                new UpdateServiceOrderStatusUseCase.Command(serviceOrder.id(), ServiceOrderStatus.ENTREGUE));

        assertThat(delivered.status()).isEqualTo(ServiceOrderStatus.ENTREGUE);
        assertThat(delivered.startedAt()).isNotNull();
        assertThat(delivered.finishedAt()).isNotNull();
        assertThat(delivered.deliveredAt()).isNotNull();
    }

    @Test
    void shouldMoveReceivedOrderToDiagnosis() {
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrderRepository.save(serviceOrder);
        UpdateServiceOrderStatusUseCase useCase =
                new UpdateServiceOrderStatusUseCase(serviceOrderRepository, partRepository);

        ServiceOrder updated = useCase.execute(
                new UpdateServiceOrderStatusUseCase.Command(serviceOrder.id(), ServiceOrderStatus.EM_DIAGNOSTICO));

        assertThat(updated.status()).isEqualTo(ServiceOrderStatus.EM_DIAGNOSTICO);
    }

    @Test
    void shouldRejectInvalidTransitionBackToReceived() {
        ServiceOrder serviceOrder = serviceOrderWithGeneratedAndApprovedBudget();
        serviceOrderRepository.save(serviceOrder);
        UpdateServiceOrderStatusUseCase useCase =
                new UpdateServiceOrderStatusUseCase(serviceOrderRepository, partRepository);

        assertThatThrownBy(() -> useCase.execute(
                        new UpdateServiceOrderStatusUseCase.Command(serviceOrder.id(), ServiceOrderStatus.RECEBIDA)))
                .isInstanceOf(InvalidServiceOrderStatusTransitionException.class)
                .hasMessage("Service order cannot return to received status");
    }

    @Test
    void shouldRejectStartingExecutionBeforeBudgetApproval() {
        ServiceOrder serviceOrder = serviceOrderWithGeneratedBudget();
        serviceOrderRepository.save(serviceOrder);
        UpdateServiceOrderStatusUseCase useCase =
                new UpdateServiceOrderStatusUseCase(serviceOrderRepository, partRepository);

        assertThatThrownBy(() -> useCase.execute(
                        new UpdateServiceOrderStatusUseCase.Command(serviceOrder.id(), ServiceOrderStatus.EM_EXECUCAO)))
                .isInstanceOf(DomainException.class)
                .hasMessage("Execution cannot start without budget approval");
    }

    @Test
    void shouldGenerateBudgetAndReservePartsWhenStatusChangesToWaitingApproval() {
        Part part = partRepository.save(Part.create(
                new Part.CatalogData(
                        "Filtro de oleo", "Filtro de oleo do motor", "OIL-STATUS-001", "Filtros", "Oleo", "Bosch"),
                new Part.Pricing(Money.of("25.00"), Money.of("50.00")),
                10,
                2));
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrder.addService(
                new WorkshopService("Troca de oleo", "Substituição de oleo e filtro", Money.of("100.00"), 60), 1);
        serviceOrder.addPart(part, 2);
        serviceOrderRepository.save(serviceOrder);
        UpdateServiceOrderStatusUseCase useCase =
                new UpdateServiceOrderStatusUseCase(serviceOrderRepository, partRepository);

        ServiceOrder updated = useCase.execute(new UpdateServiceOrderStatusUseCase.Command(
                serviceOrder.id(), ServiceOrderStatus.AGUARDANDO_APROVACAO));

        assertThat(updated.status()).isEqualTo(ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(updated.budgetGeneratedAt()).isNotNull();
        assertThat(partRepository.findById(part.id()).orElseThrow().reservedQuantity())
                .isEqualTo(2);
    }

    @Test
    void shouldNotRegenerateBudgetWhenBudgetAlreadyExists() {
        ServiceOrder serviceOrder = serviceOrderWithGeneratedBudget();
        serviceOrderRepository.save(serviceOrder);
        UpdateServiceOrderStatusUseCase useCase =
                new UpdateServiceOrderStatusUseCase(serviceOrderRepository, partRepository);

        ServiceOrder updated = useCase.execute(new UpdateServiceOrderStatusUseCase.Command(
                serviceOrder.id(), ServiceOrderStatus.AGUARDANDO_APROVACAO));

        assertThat(updated.budgetGeneratedAt()).isEqualTo(serviceOrder.budgetGeneratedAt());
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
