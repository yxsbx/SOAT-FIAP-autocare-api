package br.com.autocarehub.application.usecase.serviceorder;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.model.ServiceOrder;
import java.util.UUID;

public class GenerateServiceOrderBudgetUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final PartRepository partRepository;

    public GenerateServiceOrderBudgetUseCase(
            ServiceOrderRepository serviceOrderRepository, PartRepository partRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.partRepository = partRepository;
    }

    public ServiceOrder execute(UUID serviceOrderId) {
        ServiceOrder serviceOrder = serviceOrderRepository
                .findById(serviceOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Service order not found"));
        if (serviceOrder.budgetGeneratedAt() == null) {
            reserveParts(serviceOrder);
            serviceOrder.generateBudget();
        }
        return serviceOrderRepository.save(serviceOrder);
    }

    private void reserveParts(ServiceOrder serviceOrder) {
        for (ServiceOrder.ServiceOrderPart serviceOrderPart : serviceOrder.parts()) {
            var part = partRepository
                    .findById(serviceOrderPart.partId())
                    .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
            part.reserveStock(serviceOrderPart.quantity());
            partRepository.save(part);
        }
    }
}
