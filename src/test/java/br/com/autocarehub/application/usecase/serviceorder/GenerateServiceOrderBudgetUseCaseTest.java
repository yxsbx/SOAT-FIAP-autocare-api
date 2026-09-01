package br.com.autocarehub.application.usecase.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
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

class GenerateServiceOrderBudgetUseCaseTest {

    private final InMemoryServiceOrderRepository serviceOrderRepository = new InMemoryServiceOrderRepository();
    private final InMemoryPartRepository partRepository = new InMemoryPartRepository();

    private static ServiceOrder serviceOrderWithPart(Part part, int quantity) {
        ServiceOrder serviceOrder = new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), "Cliente relata vazamento");
        serviceOrder.addService(
                new WorkshopService("Troca de oleo", "Substituição de oleo e filtro", Money.of("100.00"), 60), 1);
        serviceOrder.addPart(part, quantity);
        return serviceOrder;
    }

    private static Part part(int stockQuantity) {
        return Part.create(
                new Part.CatalogData(
                        "Filtro de oleo",
                        "Filtro de oleo do motor",
                        "OIL-" + UUID.randomUUID(),
                        "Filtros",
                        "Oleo",
                        "Bosch"),
                new Part.Pricing(Money.of("25.00"), Money.of("50.00")),
                stockQuantity,
                2);
    }

    @Test
    void shouldGenerateBudgetAndReserveParts() {
        Part part = partRepository.save(part(10));
        ServiceOrder serviceOrder = serviceOrderWithPart(part, 3);
        serviceOrderRepository.save(serviceOrder);

        ServiceOrder updated = new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository)
                .execute(serviceOrder.id());

        assertThat(updated.status()).isEqualTo(ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(updated.budgetGeneratedAt()).isNotNull();
        assertThat(updated.servicesTotal().value()).isEqualByComparingTo("100.00");
        assertThat(updated.partsTotal().value()).isEqualByComparingTo("150.00");
        assertThat(updated.totalAmount().value()).isEqualByComparingTo("250.00");
        assertThat(partRepository.findById(part.id()).orElseThrow().reservedQuantity())
                .isEqualTo(3);
    }

    @Test
    void shouldNotReservePartsAgainWhenBudgetWasAlreadyGenerated() {
        Part part = partRepository.save(part(10));
        ServiceOrder serviceOrder = serviceOrderWithPart(part, 3);
        serviceOrderRepository.save(serviceOrder);
        GenerateServiceOrderBudgetUseCase useCase =
                new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository);

        useCase.execute(serviceOrder.id());
        useCase.execute(serviceOrder.id());

        assertThat(partRepository.findById(part.id()).orElseThrow().reservedQuantity())
                .isEqualTo(3);
    }

    @Test
    void shouldRejectBudgetGenerationWhenPartStockCannotBeReserved() {
        Part part = partRepository.save(part(2));
        ServiceOrder serviceOrder = serviceOrderWithPart(part, 2);
        serviceOrderRepository.save(serviceOrder);
        part.reserveStock(1);
        partRepository.save(part);

        assertThatThrownBy(() -> new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository)
                        .execute(serviceOrder.id()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Insufficient stock");
    }

    @Test
    void shouldRejectBudgetGenerationWhenServiceOrderDõesNotExist() {
        assertThatThrownBy(() -> new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository)
                        .execute(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service order not found");
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
