package br.com.autocarehub.application.port.out;

import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.model.ServiceOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository {

    ServiceOrder save(ServiceOrder serviceOrder);

    Optional<ServiceOrder> findById(UUID id);

    List<ServiceOrder> findAll();

    List<ServiceOrder> findByCustomerId(UUID customerId);

    List<ServiceOrder> findCompletedWithExecutionTime();

    default List<ServiceOrder> findOperationalQueue(
            ServiceOrderStatus status,
            UUID customerId,
            UUID vehicleId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Integer page,
            Integer size) {
        return findAll();
    }
}
