package br.com.autocarehub.application.usecase.serviceorder;

import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.model.ServiceOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ListServiceOrdersUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public ListServiceOrdersUseCase(ServiceOrderRepository serviceOrderRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
    }

    public List<ServiceOrder> execute() {
        return serviceOrderRepository.findOperationalQueue(null, null, null, null, null, null, null);
    }

    public List<ServiceOrder> execute(Query query) {
        return serviceOrderRepository.findOperationalQueue(
                query.status(),
                query.customerId(),
                query.vehicleId(),
                query.createdFrom(),
                query.createdTo(),
                query.page(),
                query.size());
    }

    static int statusPriority(ServiceOrderStatus status) {
        return switch (status) {
            case EM_EXECUCAO -> 0;
            case AGUARDANDO_APROVACAO -> 1;
            case EM_DIAGNOSTICO -> 2;
            case RECEBIDA -> 3;
            case FINALIZADA, ENTREGUE -> 4;
        };
    }

    public record Query(
            ServiceOrderStatus status,
            UUID customerId,
            UUID vehicleId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Integer page,
            Integer size) {

        public Query(
                ServiceOrderStatus status,
                UUID customerId,
                UUID vehicleId,
                LocalDateTime createdFrom,
                LocalDateTime createdTo) {
            this(status, customerId, vehicleId, createdFrom, createdTo, null, null);
        }
    }
}
