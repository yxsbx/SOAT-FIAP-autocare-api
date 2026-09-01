package br.com.autocarehub.application.usecase.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.valueobject.Money;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListServiceOrdersUseCaseTest {

    private final InMemoryServiceOrderRepository repository = new InMemoryServiceOrderRepository();

    private static ServiceOrder serviceOrder(
            UUID customerId, UUID vehicleId, ServiceOrderStatus status, LocalDateTime createdAt) {
        return new ServiceOrder(
                UUID.randomUUID(),
                customerId,
                vehicleId,
                status,
                "Cliente relata falha intermitente",
                List.of(),
                List.of(),
                Money.zero(),
                createdAt,
                null,
                null,
                null,
                null,
                null);
    }

    private static LocalDateTime daysAgo(int days) {
        return LocalDateTime.now().minusDays(days);
    }

    @Test
    void shouldFilterByStatusCustomerVehicleAndPeriod() {
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        ServiceOrder expected =
                serviceOrder(customerId, vehicleId, ServiceOrderStatus.AGUARDANDO_APROVACAO, daysAgo(2));
        repository.save(expected);
        repository.save(
                serviceOrder(UUID.randomUUID(), vehicleId, ServiceOrderStatus.AGUARDANDO_APROVACAO, daysAgo(2)));
        repository.save(serviceOrder(customerId, UUID.randomUUID(), ServiceOrderStatus.RECEBIDA, daysAgo(2)));
        repository.save(serviceOrder(customerId, vehicleId, ServiceOrderStatus.AGUARDANDO_APROVACAO, daysAgo(10)));
        ListServiceOrdersUseCase useCase = new ListServiceOrdersUseCase(repository);

        List<ServiceOrder> result = useCase.execute(new ListServiceOrdersUseCase.Query(
                ServiceOrderStatus.AGUARDANDO_APROVACAO, customerId, vehicleId, daysAgo(3), daysAgo(1)));

        assertThat(result).extracting(ServiceOrder::id).containsExactly(expected.id());
    }

    @Test
    void shouldReturnOpenOrdersSortedByOperationalPriorityAndOldestFirstWhenNoFiltersAreProvided() {
        ServiceOrder first =
                serviceOrder(UUID.randomUUID(), UUID.randomUUID(), ServiceOrderStatus.RECEBIDA, daysAgo(1));
        ServiceOrder second =
                serviceOrder(UUID.randomUUID(), UUID.randomUUID(), ServiceOrderStatus.AGUARDANDO_APROVACAO, daysAgo(2));
        ServiceOrder third =
                serviceOrder(UUID.randomUUID(), UUID.randomUUID(), ServiceOrderStatus.AGUARDANDO_APROVACAO, daysAgo(5));
        ServiceOrder finished =
                serviceOrder(UUID.randomUUID(), UUID.randomUUID(), ServiceOrderStatus.FINALIZADA, daysAgo(10));
        ServiceOrder delivered =
                serviceOrder(UUID.randomUUID(), UUID.randomUUID(), ServiceOrderStatus.ENTREGUE, daysAgo(11));
        repository.save(first);
        repository.save(second);
        repository.save(third);
        repository.save(finished);
        repository.save(delivered);
        ListServiceOrdersUseCase useCase = new ListServiceOrdersUseCase(repository);

        List<ServiceOrder> result = useCase.execute(new ListServiceOrdersUseCase.Query(null, null, null, null, null));

        assertThat(useCase.execute()).extracting(ServiceOrder::id).containsExactly(third.id(), second.id(), first.id());
        assertThat(result).extracting(ServiceOrder::id).containsExactly(third.id(), second.id(), first.id());
    }

    @Test
    void shouldDefineOperationalStatusPriority() {
        assertThat(ListServiceOrdersUseCase.statusPriority(ServiceOrderStatus.EM_EXECUCAO))
                .isZero();
        assertThat(ListServiceOrdersUseCase.statusPriority(ServiceOrderStatus.AGUARDANDO_APROVACAO))
                .isEqualTo(1);
        assertThat(ListServiceOrdersUseCase.statusPriority(ServiceOrderStatus.EM_DIAGNOSTICO))
                .isEqualTo(2);
        assertThat(ListServiceOrdersUseCase.statusPriority(ServiceOrderStatus.RECEBIDA))
                .isEqualTo(3);
        assertThat(ListServiceOrdersUseCase.statusPriority(ServiceOrderStatus.FINALIZADA))
                .isEqualTo(4);
        assertThat(ListServiceOrdersUseCase.statusPriority(ServiceOrderStatus.ENTREGUE))
                .isEqualTo(4);
    }

    @Test
    void shouldIncludeOrdersOnDateBoundariesAndExcludeAfterUpperBound() {
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        LocalDateTime boundary = daysAgo(1);
        ServiceOrder expected = serviceOrder(customerId, vehicleId, ServiceOrderStatus.RECEBIDA, boundary);
        ServiceOrder afterUpperBound =
                serviceOrder(customerId, vehicleId, ServiceOrderStatus.RECEBIDA, boundary.plusMinutes(1));
        repository.save(expected);
        repository.save(afterUpperBound);
        ListServiceOrdersUseCase useCase = new ListServiceOrdersUseCase(repository);

        List<ServiceOrder> result =
                useCase.execute(new ListServiceOrdersUseCase.Query(null, null, null, boundary, boundary));

        assertThat(result).extracting(ServiceOrder::id).containsExactly(expected.id());
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
        public List<ServiceOrder> findOperationalQueue(
                ServiceOrderStatus status,
                UUID customerId,
                UUID vehicleId,
                LocalDateTime createdFrom,
                LocalDateTime createdTo,
                Integer page,
                Integer size) {
            return serviceOrders.values().stream()
                    .filter(serviceOrder -> serviceOrder.status() != ServiceOrderStatus.FINALIZADA)
                    .filter(serviceOrder -> serviceOrder.status() != ServiceOrderStatus.ENTREGUE)
                    .filter(serviceOrder -> status == null || serviceOrder.status() == status)
                    .filter(serviceOrder ->
                            customerId == null || serviceOrder.customerId().equals(customerId))
                    .filter(serviceOrder ->
                            vehicleId == null || serviceOrder.vehicleId().equals(vehicleId))
                    .filter(serviceOrder ->
                            createdFrom == null || !serviceOrder.createdAt().isBefore(createdFrom))
                    .filter(serviceOrder ->
                            createdTo == null || !serviceOrder.createdAt().isAfter(createdTo))
                    .sorted(Comparator.comparingInt((ServiceOrder serviceOrder) ->
                                    ListServiceOrdersUseCase.statusPriority(serviceOrder.status()))
                            .thenComparing(ServiceOrder::createdAt))
                    .toList();
        }

        @Override
        public List<ServiceOrder> findCompletedWithExecutionTime() {
            return List.of();
        }
    }
}
