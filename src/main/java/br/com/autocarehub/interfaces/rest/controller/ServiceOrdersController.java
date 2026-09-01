package br.com.autocarehub.interfaces.rest.controller;

import br.com.autocarehub.application.usecase.serviceorder.AddPartToServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.AddServiceToServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ApproveServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.CreateServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.DecideServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.FindServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.GenerateServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.GetAverageServiceOrderExecutionTimeUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ListServiceOrdersByCustomerUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ListServiceOrdersUseCase;
import br.com.autocarehub.application.usecase.serviceorder.TrackServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.UpdateServiceOrderStatusUseCase;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.infrastructure.security.ExternalServiceTokenVerifier;
import br.com.autocarehub.interfaces.rest.generated.api.ServiceOrdersApi;
import br.com.autocarehub.interfaces.rest.generated.model.AddServiceOrderPartRequest;
import br.com.autocarehub.interfaces.rest.generated.model.AddServiceOrderServiceRequest;
import br.com.autocarehub.interfaces.rest.generated.model.AverageExecutionTimeResponse;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ExternalBudgetDecisionRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ExternalBudgetNotificationRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ExternalStatusUpdateRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderResponse;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderStatus;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderTrackingListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateServiceOrderStatusRequest;
import br.com.autocarehub.interfaces.rest.mapper.ServiceOrderRestMapper;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceOrdersController implements ServiceOrdersApi {

    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final FindServiceOrderUseCase findServiceOrderUseCase;
    private final ListServiceOrdersUseCase listServiceOrdersUseCase;
    private final AddServiceToServiceOrderUseCase addServiceToServiceOrderUseCase;
    private final AddPartToServiceOrderUseCase addPartToServiceOrderUseCase;
    private final GenerateServiceOrderBudgetUseCase generateServiceOrderBudgetUseCase;
    private final ApproveServiceOrderBudgetUseCase approveServiceOrderBudgetUseCase;
    private final DecideServiceOrderBudgetUseCase decideServiceOrderBudgetUseCase;
    private final UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase;
    private final ListServiceOrdersByCustomerUseCase listServiceOrdersByCustomerUseCase;
    private final GetAverageServiceOrderExecutionTimeUseCase getAverageServiceOrderExecutionTimeUseCase;
    private final TrackServiceOrderUseCase trackServiceOrderUseCase;
    private final ExternalServiceTokenVerifier externalServiceTokenVerifier;

    public ServiceOrdersController(
            CreateServiceOrderUseCase createServiceOrderUseCase,
            FindServiceOrderUseCase findServiceOrderUseCase,
            ListServiceOrdersUseCase listServiceOrdersUseCase,
            AddServiceToServiceOrderUseCase addServiceToServiceOrderUseCase,
            AddPartToServiceOrderUseCase addPartToServiceOrderUseCase,
            GenerateServiceOrderBudgetUseCase generateServiceOrderBudgetUseCase,
            ApproveServiceOrderBudgetUseCase approveServiceOrderBudgetUseCase,
            DecideServiceOrderBudgetUseCase decideServiceOrderBudgetUseCase,
            UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase,
            ListServiceOrdersByCustomerUseCase listServiceOrdersByCustomerUseCase,
            GetAverageServiceOrderExecutionTimeUseCase getAverageServiceOrderExecutionTimeUseCase,
            TrackServiceOrderUseCase trackServiceOrderUseCase,
            ExternalServiceTokenVerifier externalServiceTokenVerifier) {
        this.createServiceOrderUseCase = createServiceOrderUseCase;
        this.findServiceOrderUseCase = findServiceOrderUseCase;
        this.listServiceOrdersUseCase = listServiceOrdersUseCase;
        this.addServiceToServiceOrderUseCase = addServiceToServiceOrderUseCase;
        this.addPartToServiceOrderUseCase = addPartToServiceOrderUseCase;
        this.generateServiceOrderBudgetUseCase = generateServiceOrderBudgetUseCase;
        this.approveServiceOrderBudgetUseCase = approveServiceOrderBudgetUseCase;
        this.decideServiceOrderBudgetUseCase = decideServiceOrderBudgetUseCase;
        this.updateServiceOrderStatusUseCase = updateServiceOrderStatusUseCase;
        this.listServiceOrdersByCustomerUseCase = listServiceOrdersByCustomerUseCase;
        this.getAverageServiceOrderExecutionTimeUseCase = getAverageServiceOrderExecutionTimeUseCase;
        this.trackServiceOrderUseCase = trackServiceOrderUseCase;
        this.externalServiceTokenVerifier = externalServiceTokenVerifier;
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> addPartToServiceOrder(
            UUID serviceOrderId, AddServiceOrderPartRequest addServiceOrderPartRequest) {
        ServiceOrder serviceOrder = addPartToServiceOrderUseCase.execute(
                ServiceOrderRestMapper.toCommand(serviceOrderId, addServiceOrderPartRequest));
        return ResponseEntity.ok(ServiceOrderRestMapper.toResponse(serviceOrder));
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> addServiceToServiceOrder(
            UUID serviceOrderId, AddServiceOrderServiceRequest addServiceOrderServiceRequest) {
        ServiceOrder serviceOrder = addServiceToServiceOrderUseCase.execute(
                ServiceOrderRestMapper.toCommand(serviceOrderId, addServiceOrderServiceRequest));
        return ResponseEntity.ok(ServiceOrderRestMapper.toResponse(serviceOrder));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE') or @authorizationService.canAccessServiceOrder(#serviceOrderId)")
    public ResponseEntity<ServiceOrderResponse> approveServiceOrderBudget(UUID serviceOrderId) {
        return ResponseEntity.ok(
                ServiceOrderRestMapper.toResponse(approveServiceOrderBudgetUseCase.execute(serviceOrderId)));
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> decideServiceOrderBudget(
            UUID serviceOrderId,
            @Nullable String xExternalServiceToken,
            ExternalBudgetDecisionRequest externalBudgetDecisionRequest) {
        externalServiceTokenVerifier.verify(xExternalServiceToken);
        return ResponseEntity.ok(ServiceOrderRestMapper.toResponse(decideServiceOrderBudgetUseCase.execute(
                ServiceOrderRestMapper.toCommand(serviceOrderId, externalBudgetDecisionRequest))));
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> approveServiceOrderBudgetFromExternalTool(
            UUID serviceOrderId,
            @Nullable String xExternalServiceToken,
            ExternalBudgetNotificationRequest externalBudgetNotificationRequest) {
        externalServiceTokenVerifier.verify(xExternalServiceToken);
        return ResponseEntity.ok(ServiceOrderRestMapper.toResponse(
                decideServiceOrderBudgetUseCase.execute(ServiceOrderRestMapper.toCommand(
                        serviceOrderId,
                        externalBudgetNotificationRequest,
                        DecideServiceOrderBudgetUseCase.Decision.APPROVED))));
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> createServiceOrder(
            CreateServiceOrderRequest createServiceOrderRequest) {
        ServiceOrder serviceOrder =
                createServiceOrderUseCase.execute(ServiceOrderRestMapper.toCommand(createServiceOrderRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceOrderRestMapper.toResponse(serviceOrder));
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> generateServiceOrderBudget(UUID serviceOrderId) {
        return ResponseEntity.ok(
                ServiceOrderRestMapper.toResponse(generateServiceOrderBudgetUseCase.execute(serviceOrderId)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE') or @authorizationService.canAccessServiceOrder(#serviceOrderId)")
    public ResponseEntity<ServiceOrderResponse> getServiceOrderById(UUID serviceOrderId) {
        return ResponseEntity.ok(ServiceOrderRestMapper.toResponse(findServiceOrderUseCase.execute(serviceOrderId)));
    }

    @Override
    public ResponseEntity<AverageExecutionTimeResponse> getAverageServiceOrderExecutionTime() {
        return ResponseEntity.ok(
                ServiceOrderRestMapper.toResponse(getAverageServiceOrderExecutionTimeUseCase.execute()));
    }

    @Override
    public ResponseEntity<ServiceOrderListResponse> listServiceOrders(
            Integer page,
            Integer size,
            @Nullable ServiceOrderStatus status,
            @Nullable UUID customerId,
            @Nullable UUID vehicleId,
            @Nullable OffsetDateTime createdFrom,
            @Nullable OffsetDateTime createdTo) {
        return ResponseEntity.ok(ServiceOrderRestMapper.toListResponse(
                listServiceOrdersUseCase.execute(ServiceOrderRestMapper.toQuery(
                        status, customerId, vehicleId, createdFrom, createdTo, page, size)),
                null,
                null));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE') or @authorizationService.canAccessCustomer(#customerId)")
    public ResponseEntity<ServiceOrderListResponse> listServiceOrdersByCustomer(UUID customerId) {
        return ResponseEntity.ok(ServiceOrderRestMapper.toListResponse(
                listServiceOrdersByCustomerUseCase.execute(customerId), null, null));
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> rejectServiceOrderBudgetFromExternalTool(
            UUID serviceOrderId,
            @Nullable String xExternalServiceToken,
            ExternalBudgetNotificationRequest externalBudgetNotificationRequest) {
        externalServiceTokenVerifier.verify(xExternalServiceToken);
        return ResponseEntity.ok(ServiceOrderRestMapper.toResponse(
                decideServiceOrderBudgetUseCase.execute(ServiceOrderRestMapper.toCommand(
                        serviceOrderId,
                        externalBudgetNotificationRequest,
                        DecideServiceOrderBudgetUseCase.Decision.REJECTED))));
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> updateServiceOrderStatus(
            UUID serviceOrderId, UpdateServiceOrderStatusRequest updateServiceOrderStatusRequest) {
        ServiceOrder serviceOrder = updateServiceOrderStatusUseCase.execute(
                ServiceOrderRestMapper.toCommand(serviceOrderId, updateServiceOrderStatusRequest));
        return ResponseEntity.ok(ServiceOrderRestMapper.toResponse(serviceOrder));
    }

    @Override
    public ResponseEntity<ServiceOrderResponse> updateServiceOrderStatusFromExternalTool(
            UUID serviceOrderId,
            @Nullable String xExternalServiceToken,
            ExternalStatusUpdateRequest externalStatusUpdateRequest) {
        externalServiceTokenVerifier.verify(xExternalServiceToken);
        ServiceOrder serviceOrder = updateServiceOrderStatusUseCase.execute(
                ServiceOrderRestMapper.toCommand(serviceOrderId, externalStatusUpdateRequest));
        return ResponseEntity.ok(ServiceOrderRestMapper.toResponse(serviceOrder));
    }

    @Override
    @PreAuthorize(
            """
                    hasAnyRole('ADMIN','EMPLOYEE') or
                    @authorizationService.canTrackServiceOrders(#serviceOrderId, #customerDocument)
                    """)
    public ResponseEntity<ServiceOrderTrackingListResponse> trackServiceOrders(
            @Nullable UUID serviceOrderId, @Nullable String customerDocument, @Nullable String plate) {
        return ResponseEntity.ok(ServiceOrderRestMapper.toTrackingListResponse(trackServiceOrderUseCase.execute(
                new TrackServiceOrderUseCase.Query(serviceOrderId, customerDocument, plate))));
    }
}
