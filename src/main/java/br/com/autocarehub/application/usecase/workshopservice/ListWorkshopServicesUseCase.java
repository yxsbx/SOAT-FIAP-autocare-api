package br.com.autocarehub.application.usecase.workshopservice;

import br.com.autocarehub.application.port.out.WorkshopServiceRepository;
import br.com.autocarehub.domain.model.WorkshopService;
import java.util.List;

public class ListWorkshopServicesUseCase {

    private final WorkshopServiceRepository workshopServiceRepository;

    public ListWorkshopServicesUseCase(WorkshopServiceRepository workshopServiceRepository) {
        this.workshopServiceRepository = workshopServiceRepository;
    }

    public List<WorkshopService> execute() {
        return workshopServiceRepository.findAll();
    }

    public List<WorkshopService> execute(Query query) {
        return workshopServiceRepository.findAll().stream()
                .filter(workshopService -> query.active() == null || workshopService.active() == query.active())
                .toList();
    }

    public record Query(Boolean active) {}
}
