package br.com.autocarehub.infrastructure.persistence.mapper;

import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.infrastructure.persistence.entity.WorkshopServiceJpaEntity;

public final class WorkshopServiceJpaMapper {

    private WorkshopServiceJpaMapper() {}

    public static WorkshopServiceJpaEntity toEntity(WorkshopService workshopService) {
        WorkshopServiceJpaEntity entity = new WorkshopServiceJpaEntity();
        entity.setId(workshopService.id());
        entity.setName(workshopService.name());
        entity.setDescription(workshopService.description());
        entity.setBasePrice(workshopService.basePrice().value());
        entity.setEstimatedTimeInMinutes(workshopService.estimatedTimeInMinutes());
        entity.setActive(workshopService.active());
        return entity;
    }

    public static WorkshopService toDomain(WorkshopServiceJpaEntity entity) {
        return new WorkshopService(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                new Money(entity.getBasePrice()),
                entity.getEstimatedTimeInMinutes(),
                entity.isActive());
    }
}
