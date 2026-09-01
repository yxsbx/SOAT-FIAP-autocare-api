package br.com.autocarehub.application.usecase.workshopservice;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.WorkshopServiceRepository;
import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.domain.valueobject.Money;
import java.util.UUID;

public class UpdateWorkshopServiceUseCase {

    private final WorkshopServiceRepository workshopServiceRepository;

    public UpdateWorkshopServiceUseCase(WorkshopServiceRepository workshopServiceRepository) {
        this.workshopServiceRepository = workshopServiceRepository;
    }

    public WorkshopService execute(Command command) {
        WorkshopService workshopService = workshopServiceRepository
                .findById(command.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workshop service not found"));
        workshopService.update(
                command.name(), command.description(), command.basePrice(), command.estimatedTimeInMinutes());
        if (command.active()) {
            workshopService.activate();
        } else {
            workshopService.deactivate();
        }
        return workshopServiceRepository.save(workshopService);
    }

    public record Command(
            UUID serviceId,
            String name,
            String description,
            Money basePrice,
            int estimatedTimeInMinutes,
            boolean active) {}
}
