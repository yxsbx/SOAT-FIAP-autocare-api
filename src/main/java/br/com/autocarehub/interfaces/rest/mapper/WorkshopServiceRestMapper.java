package br.com.autocarehub.interfaces.rest.mapper;

import br.com.autocarehub.application.usecase.workshopservice.CreateWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.ListWorkshopServicesUseCase;
import br.com.autocarehub.application.usecase.workshopservice.UpdateWorkshopServiceUseCase;
import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.interfaces.rest.generated.model.CreateWorkshopServiceRequest;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateWorkshopServiceRequest;
import br.com.autocarehub.interfaces.rest.generated.model.WorkshopServiceListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.WorkshopServiceResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class WorkshopServiceRestMapper {

    private WorkshopServiceRestMapper() {}

    public static CreateWorkshopServiceUseCase.Command toCommand(CreateWorkshopServiceRequest request) {
        return new CreateWorkshopServiceUseCase.Command(
                request.getName(),
                request.getDescription(),
                new Money(BigDecimal.valueOf(request.getBasePrice())),
                request.getEstimatedTimeInMinutes());
    }

    public static UpdateWorkshopServiceUseCase.Command toCommand(UUID serviceId, UpdateWorkshopServiceRequest request) {
        return new UpdateWorkshopServiceUseCase.Command(
                serviceId,
                request.getName(),
                request.getDescription(),
                new Money(BigDecimal.valueOf(request.getBasePrice())),
                request.getEstimatedTimeInMinutes(),
                Boolean.TRUE.equals(request.getActive()));
    }

    public static ListWorkshopServicesUseCase.Query toQuery(Boolean active) {
        return new ListWorkshopServicesUseCase.Query(active);
    }

    public static WorkshopServiceResponse toResponse(WorkshopService service) {
        return new WorkshopServiceResponse(
                service.id(),
                service.name(),
                service.description(),
                service.basePrice().value().doubleValue(),
                service.estimatedTimeInMinutes(),
                service.active());
    }

    public static WorkshopServiceListResponse toListResponse(
            List<WorkshopService> services, Integer page, Integer size) {
        return new WorkshopServiceListResponse(RestMapperSupport.page(services, page, size).stream()
                .map(WorkshopServiceRestMapper::toResponse)
                .toList());
    }
}
