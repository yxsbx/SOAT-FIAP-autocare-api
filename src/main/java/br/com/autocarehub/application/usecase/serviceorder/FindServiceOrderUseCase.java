package br.com.autocarehub.application.usecase.serviceorder;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.model.ServiceOrder;
import java.util.UUID;

public class FindServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public FindServiceOrderUseCase(ServiceOrderRepository serviceOrderRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
    }

    public ServiceOrder execute(UUID serviceOrderId) {
        return serviceOrderRepository
                .findById(serviceOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Service order not found"));
    }
}
