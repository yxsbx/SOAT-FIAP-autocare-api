package br.com.autocarehub.application.usecase.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.exception.DomainException;
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

class ApproveServiceOrderBudgetUseCaseTest {

    private final InMemoryServiceOrderRepository serviceOrderRepository = new InMemoryServiceOrderRepository();
    private final InMemoryPartRepository partRepository = new InMemoryPartRepository();

    private static Part part() {
        return Part.create(
                new Part.CatalogData(
                        "Filtro de oleo", "Filtro de oleo do motor", "OIL-001", "Filtros", "Oleo", "Bosch"),
                new Part.Pricing(Money.of("25.00"), Money.of("50.00")),
                10,
                2);
    }

    @Test
    void shouldReservePartWhenBudgetIsGeneratedAndReduceStockWhenBudgetIsApproved() {
        Part part = partRepository.save(part());
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrder.addService(
                new WorkshopService("Troca de oleo", "Substituição de oleo e filtro", Money.of("100.00"), 60), 1);
        serviceOrder.addPart(part, 2);
        serviceOrderRepository.save(serviceOrder);

        new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository).execute(serviceOrder.id());
        assertThat(partRepository.findById(part.id()).orElseThrow().stockQuantity())
                .isEqualTo(10);
        assertThat(partRepository.findById(part.id()).orElseThrow().reservedQuantity())
                .isEqualTo(2);

        new ApproveServiceOrderBudgetUseCase(serviceOrderRepository, partRepository).execute(serviceOrder.id());

        Part updated = partRepository.findById(part.id()).orElseThrow();
        ServiceOrder approvedOrder =
                serviceOrderRepository.findById(serviceOrder.id()).orElseThrow();
        assertThat(approvedOrder.status()).isEqualTo(ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(approvedOrder.approvedAt()).isNotNull();
        assertThat(updated.stockQuantity()).isEqualTo(8);
        assertThat(updated.reservedQuantity()).isZero();
        assertThat(updated.availableQuantity()).isEqualTo(8);
    }

    @Test
    void shouldNotReduceStockAgainWhenBudgetWasAlreadyApproved() {
        Part part = partRepository.save(part());
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrder.addService(
                new WorkshopService("Troca de oleo", "Substituição de oleo e filtro", Money.of("100.00"), 60), 1);
        serviceOrder.addPart(part, 2);
        serviceOrderRepository.save(serviceOrder);
        new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository).execute(serviceOrder.id());
        ApproveServiceOrderBudgetUseCase useCase =
                new ApproveServiceOrderBudgetUseCase(serviceOrderRepository, partRepository);

        useCase.execute(serviceOrder.id());
        useCase.execute(serviceOrder.id());

        Part updated = partRepository.findById(part.id()).orElseThrow();
        assertThat(updated.stockQuantity()).isEqualTo(8);
        assertThat(updated.reservedQuantity()).isZero();
    }

    @Test
    void shouldRejectBudgetApprovalWhenBudgetWasNotGenerated() {
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrder.addService(
                new WorkshopService("Troca de oleo", "Substituição de oleo e filtro", Money.of("100.00"), 60), 1);
        serviceOrderRepository.save(serviceOrder);

        assertThatThrownBy(() -> new ApproveServiceOrderBudgetUseCase(serviceOrderRepository, partRepository)
                        .execute(serviceOrder.id()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Budget can only be approved while waiting approval");
    }

    @Test
    void shouldApplyExternalBudgetApprovalDecision() {
        Part part = partRepository.save(part());
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrder.addService(
                new WorkshopService("Troca de oleo", "Substituição de oleo e filtro", Money.of("100.00"), 60), 1);
        serviceOrder.addPart(part, 2);
        serviceOrderRepository.save(serviceOrder);
        new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository).execute(serviceOrder.id());

        ServiceOrder updated = new DecideServiceOrderBudgetUseCase(serviceOrderRepository, partRepository)
                .execute(new DecideServiceOrderBudgetUseCase.Command(
                        serviceOrder.id(), DecideServiceOrderBudgetUseCase.Decision.APPROVED, "email", null));

        assertThat(updated.approvedAt()).isNotNull();
        assertThat(partRepository.findById(part.id()).orElseThrow().stockQuantity())
                .isEqualTo(8);
    }

    @Test
    void shouldApplyExternalBudgetRejectionDecisionAndReleaseReservedParts() {
        Part part = partRepository.save(part());
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrder.addService(
                new WorkshopService("Troca de oleo", "Substituição de oleo e filtro", Money.of("100.00"), 60), 1);
        serviceOrder.addPart(part, 2);
        serviceOrderRepository.save(serviceOrder);
        new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository).execute(serviceOrder.id());

        ServiceOrder updated = new DecideServiceOrderBudgetUseCase(serviceOrderRepository, partRepository)
                .execute(new DecideServiceOrderBudgetUseCase.Command(
                        serviceOrder.id(), DecideServiceOrderBudgetUseCase.Decision.REJECTED, "email", "Revisar"));

        Part updatedPart = partRepository.findById(part.id()).orElseThrow();
        assertThat(updated.status()).isEqualTo(ServiceOrderStatus.EM_DIAGNOSTICO);
        assertThat(updated.approvedAt()).isNull();
        assertThat(updatedPart.stockQuantity()).isEqualTo(10);
        assertThat(updatedPart.reservedQuantity()).isZero();
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
