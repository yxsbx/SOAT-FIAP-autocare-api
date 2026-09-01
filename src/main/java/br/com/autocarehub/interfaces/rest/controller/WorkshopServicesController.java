package br.com.autocarehub.interfaces.rest.controller;

import br.com.autocarehub.application.usecase.workshopservice.CreateWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.DeleteWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.FindWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.ListWorkshopServicesUseCase;
import br.com.autocarehub.application.usecase.workshopservice.UpdateWorkshopServiceUseCase;
import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.interfaces.rest.generated.api.WorkshopServicesApi;
import br.com.autocarehub.interfaces.rest.generated.model.CreateWorkshopServiceRequest;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateWorkshopServiceRequest;
import br.com.autocarehub.interfaces.rest.generated.model.WorkshopServiceListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.WorkshopServiceResponse;
import br.com.autocarehub.interfaces.rest.mapper.WorkshopServiceRestMapper;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkshopServicesController implements WorkshopServicesApi {

    private final CreateWorkshopServiceUseCase createWorkshopServiceUseCase;
    private final UpdateWorkshopServiceUseCase updateWorkshopServiceUseCase;
    private final FindWorkshopServiceUseCase findWorkshopServiceUseCase;
    private final ListWorkshopServicesUseCase listWorkshopServicesUseCase;
    private final DeleteWorkshopServiceUseCase deleteWorkshopServiceUseCase;

    public WorkshopServicesController(
            CreateWorkshopServiceUseCase createWorkshopServiceUseCase,
            UpdateWorkshopServiceUseCase updateWorkshopServiceUseCase,
            FindWorkshopServiceUseCase findWorkshopServiceUseCase,
            ListWorkshopServicesUseCase listWorkshopServicesUseCase,
            DeleteWorkshopServiceUseCase deleteWorkshopServiceUseCase) {
        this.createWorkshopServiceUseCase = createWorkshopServiceUseCase;
        this.updateWorkshopServiceUseCase = updateWorkshopServiceUseCase;
        this.findWorkshopServiceUseCase = findWorkshopServiceUseCase;
        this.listWorkshopServicesUseCase = listWorkshopServicesUseCase;
        this.deleteWorkshopServiceUseCase = deleteWorkshopServiceUseCase;
    }

    @Override
    public ResponseEntity<WorkshopServiceResponse> createWorkshopService(
            CreateWorkshopServiceRequest createWorkshopServiceRequest) {
        WorkshopService service =
                createWorkshopServiceUseCase.execute(WorkshopServiceRestMapper.toCommand(createWorkshopServiceRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(WorkshopServiceRestMapper.toResponse(service));
    }

    @Override
    public ResponseEntity<Void> deleteWorkshopService(UUID serviceId) {
        deleteWorkshopServiceUseCase.execute(serviceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<WorkshopServiceResponse> getWorkshopServiceById(UUID serviceId) {
        return ResponseEntity.ok(WorkshopServiceRestMapper.toResponse(findWorkshopServiceUseCase.execute(serviceId)));
    }

    @Override
    public ResponseEntity<WorkshopServiceListResponse> listWorkshopServices(
            Integer page, Integer size, @Nullable Boolean active) {
        return ResponseEntity.ok(WorkshopServiceRestMapper.toListResponse(
                listWorkshopServicesUseCase.execute(WorkshopServiceRestMapper.toQuery(active)), page, size));
    }

    @Override
    public ResponseEntity<WorkshopServiceResponse> updateWorkshopService(
            UUID serviceId, UpdateWorkshopServiceRequest updateWorkshopServiceRequest) {
        WorkshopService service = updateWorkshopServiceUseCase.execute(
                WorkshopServiceRestMapper.toCommand(serviceId, updateWorkshopServiceRequest));
        return ResponseEntity.ok(WorkshopServiceRestMapper.toResponse(service));
    }
}
