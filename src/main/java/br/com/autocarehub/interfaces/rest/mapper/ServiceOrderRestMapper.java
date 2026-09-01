package br.com.autocarehub.interfaces.rest.mapper;

import br.com.autocarehub.application.usecase.serviceorder.AddPartToServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.AddServiceToServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.CreateServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.DecideServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.GetAverageServiceOrderExecutionTimeUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ListServiceOrdersUseCase;
import br.com.autocarehub.application.usecase.serviceorder.TrackServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.UpdateServiceOrderStatusUseCase;
import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.interfaces.rest.generated.model.AddServiceOrderPartRequest;
import br.com.autocarehub.interfaces.rest.generated.model.AddServiceOrderServiceRequest;
import br.com.autocarehub.interfaces.rest.generated.model.AverageExecutionTimeResponse;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderCustomerRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderPartRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderServiceRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderVehicleRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ExternalBudgetDecisionRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ExternalBudgetNotificationRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ExternalStatusUpdateRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderBudgetTrackingResponse;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderPartItem;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderResponse;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderServiceItem;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderStatusHistoryItem;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderTrackingListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderTrackingResponse;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderTrackingStatus;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateServiceOrderStatusRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class ServiceOrderRestMapper {

    private ServiceOrderRestMapper() {}

    public static CreateServiceOrderUseCase.Command toCommand(CreateServiceOrderRequest request) {
        String customerDocument = request.getCustomerDocument();
        return new CreateServiceOrderUseCase.Command(
                customerDocument,
                toCustomerInput(request.getCustomer()),
                request.getVehicleId(),
                toVehicleInput(request.getVehicle()),
                request.getDiagnosticNotes(),
                request.getServices().stream()
                        .map(ServiceOrderRestMapper::toServiceInput)
                        .toList(),
                request.getParts() == null
                        ? List.of()
                        : request.getParts().stream()
                                .map(ServiceOrderRestMapper::toPartInput)
                                .toList(),
                request.getGenerateBudget() == null || Boolean.TRUE.equals(request.getGenerateBudget()));
    }

    public static AddServiceToServiceOrderUseCase.Command toCommand(
            UUID serviceOrderId, AddServiceOrderServiceRequest request) {
        return new AddServiceToServiceOrderUseCase.Command(
                serviceOrderId, request.getServiceId(), request.getQuantity());
    }

    public static AddPartToServiceOrderUseCase.Command toCommand(
            UUID serviceOrderId, AddServiceOrderPartRequest request) {
        return new AddPartToServiceOrderUseCase.Command(serviceOrderId, request.getPartId(), request.getQuantity());
    }

    public static UpdateServiceOrderStatusUseCase.Command toCommand(
            UUID serviceOrderId, UpdateServiceOrderStatusRequest request) {
        return new UpdateServiceOrderStatusUseCase.Command(
                serviceOrderId,
                ServiceOrderStatus.fromExternalCode(request.getStatus().getValue()));
    }

    public static DecideServiceOrderBudgetUseCase.Command toCommand(
            UUID serviceOrderId, ExternalBudgetDecisionRequest request) {
        return new DecideServiceOrderBudgetUseCase.Command(
                serviceOrderId,
                DecideServiceOrderBudgetUseCase.Decision.valueOf(
                        request.getDecision().getValue()),
                request.getSource(),
                request.getReason());
    }

    public static DecideServiceOrderBudgetUseCase.Command toCommand(
            UUID serviceOrderId,
            ExternalBudgetNotificationRequest request,
            DecideServiceOrderBudgetUseCase.Decision decision) {
        return new DecideServiceOrderBudgetUseCase.Command(
                serviceOrderId, decision, request.getSource(), request.getReason());
    }

    public static UpdateServiceOrderStatusUseCase.Command toCommand(
            UUID serviceOrderId, ExternalStatusUpdateRequest request) {
        return new UpdateServiceOrderStatusUseCase.Command(
                serviceOrderId,
                ServiceOrderStatus.fromExternalCode(request.getStatus().getValue()));
    }

    public static ListServiceOrdersUseCase.Query toQuery(
            br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderStatus status,
            UUID customerId,
            UUID vehicleId,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo,
            Integer page,
            Integer size) {
        return new ListServiceOrdersUseCase.Query(
                status == null ? null : ServiceOrderStatus.fromExternalCode(status.getValue()),
                customerId,
                vehicleId,
                createdFrom == null ? null : createdFrom.toLocalDateTime(),
                createdTo == null ? null : createdTo.toLocalDateTime(),
                page,
                size);
    }

    public static ListServiceOrdersUseCase.Query toQuery(
            br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderStatus status,
            UUID customerId,
            UUID vehicleId,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo) {
        return toQuery(status, customerId, vehicleId, createdFrom, createdTo, null, null);
    }

    public static ServiceOrderResponse toResponse(ServiceOrder serviceOrder) {
        return new ServiceOrderResponse(
                        serviceOrder.id(),
                        serviceOrder.customerId(),
                        serviceOrder.vehicleId(),
                        br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderStatus.fromValue(
                                serviceOrder.status().externalCode()),
                        serviceOrder.diagnosticNotes(),
                        serviceOrder.services().stream()
                                .map(ServiceOrderRestMapper::toServiceItem)
                                .toList(),
                        serviceOrder.parts().stream()
                                .map(ServiceOrderRestMapper::toPartItem)
                                .toList(),
                        serviceOrder.servicesTotal().value().doubleValue(),
                        serviceOrder.partsTotal().value().doubleValue(),
                        serviceOrder.totalAmount().value().doubleValue(),
                        RestMapperSupport.toOffsetDateTime(serviceOrder.createdAt()))
                .budgetGeneratedAt(RestMapperSupport.toOffsetDateTime(serviceOrder.budgetGeneratedAt()))
                .approvedAt(RestMapperSupport.toOffsetDateTime(serviceOrder.approvedAt()))
                .startedAt(RestMapperSupport.toOffsetDateTime(serviceOrder.startedAt()))
                .finishedAt(RestMapperSupport.toOffsetDateTime(serviceOrder.finishedAt()))
                .deliveredAt(RestMapperSupport.toOffsetDateTime(serviceOrder.deliveredAt()));
    }

    public static ServiceOrderListResponse toListResponse(
            List<ServiceOrder> serviceOrders, Integer page, Integer size) {
        return new ServiceOrderListResponse(RestMapperSupport.page(serviceOrders, page, size).stream()
                .map(ServiceOrderRestMapper::toResponse)
                .toList());
    }

    public static ServiceOrderTrackingListResponse toTrackingListResponse(
            List<TrackServiceOrderUseCase.Output> outputs) {
        return new ServiceOrderTrackingListResponse(
                outputs.stream().map(ServiceOrderRestMapper::toTrackingResponse).toList());
    }

    public static AverageExecutionTimeResponse toResponse(GetAverageServiceOrderExecutionTimeUseCase.Output output) {
        return new AverageExecutionTimeResponse(output.completedOrders(), output.averageExecutionTimeInMinutes());
    }

    private static ServiceOrderServiceItem toServiceItem(ServiceOrder.ServiceOrderService service) {
        return new ServiceOrderServiceItem(
                service.serviceId(),
                service.name(),
                service.quantity(),
                service.unitPrice().value().doubleValue(),
                service.totalPrice().value().doubleValue());
    }

    private static ServiceOrderPartItem toPartItem(ServiceOrder.ServiceOrderPart part) {
        return new ServiceOrderPartItem(
                part.partId(),
                part.name(),
                part.sku(),
                part.quantity(),
                part.unitPrice().value().doubleValue(),
                part.totalPrice().value().doubleValue());
    }

    private static ServiceOrderTrackingResponse toTrackingResponse(TrackServiceOrderUseCase.Output output) {
        ServiceOrder serviceOrder = output.serviceOrder();
        return new ServiceOrderTrackingResponse(
                serviceOrder.id(),
                serviceOrder.customerId(),
                VehicleRestMapper.toResponse(output.vehicle()),
                ServiceOrderTrackingStatus.fromValue(serviceOrder.status().name()),
                serviceOrder.diagnosticNotes(),
                serviceOrder.services().stream()
                        .map(ServiceOrderRestMapper::toServiceItem)
                        .toList(),
                serviceOrder.parts().stream()
                        .map(ServiceOrderRestMapper::toPartItem)
                        .toList(),
                toBudgetTrackingResponse(serviceOrder),
                statusHistory(serviceOrder),
                RestMapperSupport.toOffsetDateTime(serviceOrder.createdAt()));
    }

    private static ServiceOrderBudgetTrackingResponse toBudgetTrackingResponse(ServiceOrder serviceOrder) {
        boolean generated = serviceOrder.budgetGeneratedAt() != null;
        boolean approved = serviceOrder.approvedAt() != null;
        return new ServiceOrderBudgetTrackingResponse(
                        generated,
                        approved,
                        serviceOrder.servicesTotal().value().doubleValue(),
                        serviceOrder.partsTotal().value().doubleValue(),
                        serviceOrder.totalAmount().value().doubleValue())
                .generatedAt(RestMapperSupport.toOffsetDateTime(serviceOrder.budgetGeneratedAt()))
                .approvedAt(RestMapperSupport.toOffsetDateTime(serviceOrder.approvedAt()));
    }

    private static List<ServiceOrderStatusHistoryItem> statusHistory(ServiceOrder serviceOrder) {
        List<ServiceOrderStatusHistoryItem> history = new ArrayList<>();
        history.add(
                statusHistoryItem(ServiceOrderStatus.RECEBIDA, serviceOrder.createdAt(), "Ordem de serviço criada"));
        if (serviceOrder.status() == ServiceOrderStatus.EM_DIAGNOSTICO) {
            history.add(statusHistoryItem(
                    ServiceOrderStatus.EM_DIAGNOSTICO, serviceOrder.createdAt(), "Diagnóstico iniciado"));
        }
        LocalDateTime budgetGeneratedAt = serviceOrder.budgetGeneratedAt();
        if (budgetGeneratedAt != null) {
            history.add(statusHistoryItem(
                    ServiceOrderStatus.AGUARDANDO_APROVACAO,
                    budgetGeneratedAt,
                    "Orçamento gerado e disponibilizado para aprovação"));
        }
        LocalDateTime approvedAt = serviceOrder.approvedAt();
        if (approvedAt != null) {
            history.add(statusHistoryItem(
                    ServiceOrderStatus.AGUARDANDO_APROVACAO, approvedAt, "Orçamento aprovado pelo cliente"));
        }
        LocalDateTime startedAt = serviceOrder.startedAt();
        if (startedAt != null) {
            history.add(statusHistoryItem(ServiceOrderStatus.EM_EXECUCAO, startedAt, "Execução iniciada"));
        }
        LocalDateTime finishedAt = serviceOrder.finishedAt();
        if (finishedAt != null) {
            history.add(statusHistoryItem(ServiceOrderStatus.FINALIZADA, finishedAt, "Serviço finalizado"));
        }
        LocalDateTime deliveredAt = serviceOrder.deliveredAt();
        if (deliveredAt != null) {
            history.add(statusHistoryItem(ServiceOrderStatus.ENTREGUE, deliveredAt, "Veículo entregue"));
        }
        return history;
    }

    private static ServiceOrderStatusHistoryItem statusHistoryItem(
            ServiceOrderStatus status, @Nullable LocalDateTime occurredAt, String description) {
        return new ServiceOrderStatusHistoryItem(
                ServiceOrderTrackingStatus.fromValue(status.name()),
                RestMapperSupport.toOffsetDateTime(occurredAt),
                description);
    }

    private static CreateServiceOrderUseCase.CustomerInput toCustomerInput(CreateServiceOrderCustomerRequest customer) {
        if (customer == null) {
            return null;
        }

        return new CreateServiceOrderUseCase.CustomerInput(
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                CustomerRestMapper.toDomainAddress(customer.getAddress()));
    }

    private static CreateServiceOrderUseCase.VehicleInput toVehicleInput(CreateServiceOrderVehicleRequest vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new CreateServiceOrderUseCase.VehicleInput(
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getMileage() == null ? 0 : vehicle.getMileage());
    }

    private static CreateServiceOrderUseCase.ServiceInput toServiceInput(CreateServiceOrderServiceRequest service) {
        return new CreateServiceOrderUseCase.ServiceInput(service.getServiceId(), service.getQuantity());
    }

    private static CreateServiceOrderUseCase.PartInput toPartInput(CreateServiceOrderPartRequest part) {
        return new CreateServiceOrderUseCase.PartInput(part.getPartId(), part.getQuantity());
    }
}
