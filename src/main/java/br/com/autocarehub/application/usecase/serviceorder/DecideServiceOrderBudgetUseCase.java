package br.com.autocarehub.application.usecase.serviceorder;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.model.ServiceOrder;
import java.util.UUID;

public class DecideServiceOrderBudgetUseCase {

    private final ApproveServiceOrderBudgetUseCase approveServiceOrderBudgetUseCase;
    private final ServiceOrderRepository serviceOrderRepository;
    private final PartRepository partRepository;

    public DecideServiceOrderBudgetUseCase(
            ServiceOrderRepository serviceOrderRepository, PartRepository partRepository) {
        this.approveServiceOrderBudgetUseCase =
                new ApproveServiceOrderBudgetUseCase(serviceOrderRepository, partRepository);
        this.serviceOrderRepository = serviceOrderRepository;
        this.partRepository = partRepository;
    }

    public ServiceOrder execute(Command command) {
        if (command.decision() == Decision.APPROVED) {
            return approveServiceOrderBudgetUseCase.execute(command.serviceOrderId());
        }

        ServiceOrder serviceOrder = serviceOrderRepository
                .findById(command.serviceOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Service order not found"));
        serviceOrder.rejectBudget();
        releaseReservedParts(serviceOrder);
        return serviceOrderRepository.save(serviceOrder);
    }

    private void releaseReservedParts(ServiceOrder serviceOrder) {
        for (ServiceOrder.ServiceOrderPart serviceOrderPart : serviceOrder.parts()) {
            var part = partRepository
                    .findById(serviceOrderPart.partId())
                    .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
            part.releaseReservedStock(serviceOrderPart.quantity());
            partRepository.save(part);
        }
    }

    public record Command(UUID serviceOrderId, Decision decision, String source, String reason) {}

    public enum Decision {
        APPROVED,
        REJECTED
    }
}
