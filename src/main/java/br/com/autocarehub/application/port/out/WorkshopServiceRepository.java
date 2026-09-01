package br.com.autocarehub.application.port.out;

import br.com.autocarehub.domain.model.WorkshopService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkshopServiceRepository {

    WorkshopService save(WorkshopService workshopService);

    Optional<WorkshopService> findById(UUID id);

    List<WorkshopService> findAll();
}
