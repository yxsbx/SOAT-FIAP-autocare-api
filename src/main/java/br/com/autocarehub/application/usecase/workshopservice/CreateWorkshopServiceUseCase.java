package br.com.autocarehub.application.usecase.workshopservice;

import br.com.autocarehub.application.port.out.WorkshopServiceRepository;
import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.domain.valueobject.Money;

public class CreateWorkshopServiceUseCase {

    private final WorkshopServiceRepository workshopServiceRepository;

    public CreateWorkshopServiceUseCase(WorkshopServiceRepository workshopServiceRepository) {
        this.workshopServiceRepository = workshopServiceRepository;
    }

    public WorkshopService execute(Command command) {
        WorkshopService workshopService = new WorkshopService(
                command.name(), command.description(), command.basePrice(), command.estimatedTimeInMinutes());
        return workshopServiceRepository.save(workshopService);
    }

    public record Command(String name, String description, Money basePrice, int estimatedTimeInMinutes) {}
}
