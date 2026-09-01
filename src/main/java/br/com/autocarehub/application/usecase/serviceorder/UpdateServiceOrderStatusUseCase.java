package br.com.autocarehub.application.usecase.serviceorder;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.exception.InvalidServiceOrderStatusTransitionException;
import br.com.autocarehub.domain.model.ServiceOrder;
import java.util.UUID;

public class UpdateServiceOrderStatusUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final PartRepository partRepository;

    public UpdateServiceOrderStatusUseCase(
            ServiceOrderRepository serviceOrderRepository, PartRepository partRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.partRepository = partRepository;
    }

    public ServiceOrder execute(Command command) {
        ServiceOrder serviceOrder = serviceOrderRepository
                .findById(command.serviceOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Service order not found"));
        switch (command.status()) {
            case EM_DIAGNOSTICO -> serviceOrder.startDiagnosis();
            case AGUARDANDO_APROVACAO -> {
                return new GenerateServiceOrderBudgetUseCase(serviceOrderRepository, partRepository)
                        .execute(command.serviceOrderId());
            }
            case EM_EXECUCAO -> serviceOrder.startExecution();
            case FINALIZADA -> serviceOrder.finish();
            case ENTREGUE -> serviceOrder.deliver();
            case RECEBIDA ->
                throw new InvalidServiceOrderStatusTransitionException(
                        "Service order cannot return to received status");
        }
        return serviceOrderRepository.save(serviceOrder);
    }

    public record Command(UUID serviceOrderId, ServiceOrderStatus status) {}
}
