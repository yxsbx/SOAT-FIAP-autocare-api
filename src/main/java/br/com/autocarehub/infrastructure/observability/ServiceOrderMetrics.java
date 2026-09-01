package br.com.autocarehub.infrastructure.observability;

import br.com.autocarehub.domain.model.ServiceOrder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ServiceOrderMetrics {

    private final MeterRegistry meterRegistry;

    public ServiceOrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void created(ServiceOrder serviceOrder) {
        Counter.builder("autocare.service_orders.created")
                .description("Total de ordens de servico criadas")
                .tag("status", serviceOrder.status().name())
                .register(meterRegistry)
                .increment();
    }

    public void statusChanged(ServiceOrder serviceOrder) {
        Counter.builder("autocare.service_orders.status_changed")
                .description("Total de alteracoes de status de ordens de servico")
                .tag("status", serviceOrder.status().name())
                .register(meterRegistry)
                .increment();
    }

    public void processingFailed(String operation, RuntimeException exception) {
        Counter.builder("autocare.service_orders.processing_failed")
                .description("Falhas no processamento de ordens de servico")
                .tag("operation", operation)
                .tag("exception", exception.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();
    }
}
