package br.com.autocarehub.application.usecase.serviceorder;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.application.port.out.WorkshopServiceRepository;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.model.WorkshopService;
import java.util.UUID;

public class AddServiceToServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final WorkshopServiceRepository workshopServiceRepository;

    public AddServiceToServiceOrderUseCase(
            ServiceOrderRepository serviceOrderRepository, WorkshopServiceRepository workshopServiceRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.workshopServiceRepository = workshopServiceRepository;
    }

    public ServiceOrder execute(Command command) {
        ServiceOrder serviceOrder = serviceOrderRepository
                .findById(command.serviceOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Service order not found"));
        WorkshopService workshopService = workshopServiceRepository
                .findById(command.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workshop service not found"));
        serviceOrder.addService(workshopService, command.quantity());
        return serviceOrderRepository.save(serviceOrder);
    }

    public record Command(UUID serviceOrderId, UUID serviceId, int quantity) {}
}
