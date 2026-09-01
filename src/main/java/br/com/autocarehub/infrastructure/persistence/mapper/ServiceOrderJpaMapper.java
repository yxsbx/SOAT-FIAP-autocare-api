package br.com.autocarehub.infrastructure.persistence.mapper;

import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.infrastructure.persistence.entity.ServiceOrderJpaEntity;
import br.com.autocarehub.infrastructure.persistence.entity.ServiceOrderPartJpaEntity;
import br.com.autocarehub.infrastructure.persistence.entity.ServiceOrderServiceJpaEntity;
import java.util.List;
import java.util.UUID;

public final class ServiceOrderJpaMapper {

    private ServiceOrderJpaMapper() {}

    public static ServiceOrderJpaEntity toEntity(ServiceOrder serviceOrder) {
        ServiceOrderJpaEntity entity = new ServiceOrderJpaEntity();
        entity.setId(serviceOrder.id());
        entity.setCustomerId(serviceOrder.customerId());
        entity.setVehicleId(serviceOrder.vehicleId());
        entity.setStatus(serviceOrder.status().externalCode());
        entity.setDiagnosticNotes(serviceOrder.diagnosticNotes());
        entity.setTotalAmount(serviceOrder.totalAmount().value());
        entity.setCreatedAt(serviceOrder.createdAt());
        entity.setBudgetGeneratedAt(serviceOrder.budgetGeneratedAt());
        entity.setApprovedAt(serviceOrder.approvedAt());
        entity.setStartedAt(serviceOrder.startedAt());
        entity.setFinishedAt(serviceOrder.finishedAt());
        entity.setDeliveredAt(serviceOrder.deliveredAt());
        entity.replaceServices(serviceOrder.services().stream()
                .map(ServiceOrderJpaMapper::toServiceEntity)
                .toList());
        entity.replaceParts(serviceOrder.parts().stream()
                .map(ServiceOrderJpaMapper::toPartEntity)
                .toList());
        return entity;
    }

    public static ServiceOrder toDomain(ServiceOrderJpaEntity entity) {
        List<ServiceOrder.ServiceOrderService> services = entity.getServices().stream()
                .map(ServiceOrderJpaMapper::toServiceDomain)
                .toList();
        List<ServiceOrder.ServiceOrderPart> parts = entity.getParts().stream()
                .map(ServiceOrderJpaMapper::toPartDomain)
                .toList();
        return new ServiceOrder(
                entity.getId(),
                entity.getCustomerId(),
                entity.getVehicleId(),
                ServiceOrderStatus.fromExternalCode(entity.getStatus()),
                entity.getDiagnosticNotes(),
                services,
                parts,
                new Money(entity.getTotalAmount()),
                entity.getCreatedAt(),
                entity.getBudgetGeneratedAt(),
                entity.getApprovedAt(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getDeliveredAt());
    }

    private static ServiceOrderServiceJpaEntity toServiceEntity(ServiceOrder.ServiceOrderService service) {
        ServiceOrderServiceJpaEntity entity = new ServiceOrderServiceJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setServiceId(service.serviceId());
        entity.setName(service.name());
        entity.setQuantity(service.quantity());
        entity.setUnitPrice(service.unitPrice().value());
        entity.setTotalPrice(service.totalPrice().value());
        return entity;
    }

    private static ServiceOrderPartJpaEntity toPartEntity(ServiceOrder.ServiceOrderPart part) {
        ServiceOrderPartJpaEntity entity = new ServiceOrderPartJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setPartId(part.partId());
        entity.setName(part.name());
        entity.setSku(part.sku());
        entity.setQuantity(part.quantity());
        entity.setUnitPrice(part.unitPrice().value());
        entity.setTotalPrice(part.totalPrice().value());
        return entity;
    }

    private static ServiceOrder.ServiceOrderService toServiceDomain(ServiceOrderServiceJpaEntity entity) {
        return new ServiceOrder.ServiceOrderService(
                entity.getServiceId(),
                entity.getName(),
                entity.getQuantity(),
                new Money(entity.getUnitPrice()),
                new Money(entity.getTotalPrice()));
    }

    private static ServiceOrder.ServiceOrderPart toPartDomain(ServiceOrderPartJpaEntity entity) {
        return new ServiceOrder.ServiceOrderPart(
                entity.getPartId(),
                entity.getName(),
                entity.getSku(),
                entity.getQuantity(),
                new Money(entity.getUnitPrice()),
                new Money(entity.getTotalPrice()));
    }
}
