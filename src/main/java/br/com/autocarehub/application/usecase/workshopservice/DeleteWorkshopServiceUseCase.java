package br.com.autocarehub.application.usecase.workshopservice;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.WorkshopServiceRepository;
import br.com.autocarehub.domain.model.WorkshopService;
import java.util.UUID;

public class DeleteWorkshopServiceUseCase {

    private final WorkshopServiceRepository workshopServiceRepository;

    public DeleteWorkshopServiceUseCase(WorkshopServiceRepository workshopServiceRepository) {
        this.workshopServiceRepository = workshopServiceRepository;
    }

    public void execute(UUID serviceId) {
        WorkshopService workshopService = workshopServiceRepository
                .findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workshop service not found"));
        workshopService.deactivate();
        workshopServiceRepository.save(workshopService);
    }
}
