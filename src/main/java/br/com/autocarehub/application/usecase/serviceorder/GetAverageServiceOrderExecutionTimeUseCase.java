package br.com.autocarehub.application.usecase.serviceorder;

import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.model.ServiceOrder;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class GetAverageServiceOrderExecutionTimeUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public GetAverageServiceOrderExecutionTimeUseCase(ServiceOrderRepository serviceOrderRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
    }

    public Output execute() {
        List<Long> durations = serviceOrderRepository.findCompletedWithExecutionTime().stream()
                .map(this::durationInMinutes)
                .toList();
        double average = durations.stream().mapToLong(Long::longValue).average().orElse(0);
        return new Output(durations.size(), average);
    }

    private long durationInMinutes(ServiceOrder serviceOrder) {
        return Duration.between(Objects.requireNonNull(serviceOrder.startedAt()), serviceOrder.finishedAt())
                .toMinutes();
    }

    public record Output(long completedOrders, double averageExecutionTimeInMinutes) {}
}
