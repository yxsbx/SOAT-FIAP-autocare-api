package br.com.autocarehub.interfaces.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.autocarehub.application.usecase.serviceorder.TrackServiceOrderUseCase;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.domain.model.WorkshopService;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.domain.valueobject.Plate;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderCustomerRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderPartRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderServiceRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreateServiceOrderVehicleRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderStatus;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderStatusHistoryItem;
import br.com.autocarehub.interfaces.rest.generated.model.ServiceOrderTrackingResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ServiceOrderRestMapperTest {

    private static ServiceOrderTrackingResponse trackingResponse(
            ServiceOrder serviceOrder, Customer customer, Vehicle vehicle) {
        return ServiceOrderRestMapper.toTrackingListResponse(
                        List.of(new TrackServiceOrderUseCase.Output(serviceOrder, customer, vehicle)))
                .getItems()
                .getFirst();
    }

    private static Customer customer() {
        return new Customer("Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address());
    }

    private static Vehicle vehicle(Customer customer) {
        return new Vehicle(customer.id(), new Plate("ABC1D23"), "Honda", "Civic", 2020, 35000);
    }

    private static Address address() {
        return new Address("Avenida Paulista", "1000", null, "Bela Vista", "São Paulo", "SP", "01310-100");
    }

    @Test
    void shouldMapCreateCommandWithDefaultsAndNullOptionalCollections() {
        UUID serviceId = UUID.randomUUID();
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                "52998224725",
                "Cliente relata barulho ao frear",
                List.of(new CreateServiceOrderServiceRequest(serviceId, 2)));
        request.setParts(null);
        request.setGenerateBudget(null);

        var command = ServiceOrderRestMapper.toCommand(request);

        assertThat(command.customerDocument()).isEqualTo("52998224725");
        assertThat(command.customer()).isNull();
        assertThat(command.vehicleId()).isNull();
        assertThat(command.vehicle()).isNull();
        assertThat(command.services()).hasSize(1);
        assertThat(command.services().getFirst().serviceId()).isEqualTo(serviceId);
        assertThat(command.parts()).isEmpty();
        assertThat(command.generateBudget()).isTrue();
    }

    @Test
    void shouldMapCreateCommandWithCustomerVehiclePartsAndExplicitBudgetFlag() {
        UUID serviceId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        var apiAddress = new br.com.autocarehub.interfaces.rest.generated.model.Address(
                        "Avenida Paulista", "1000", "Bela Vista", "São Paulo", "SP", "01310-100")
                .complement("10 andar");
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                        "52998224725",
                        "Cliente relata vibração ao acelerar",
                        List.of(new CreateServiceOrderServiceRequest(serviceId, 1)))
                .customer(new CreateServiceOrderCustomerRequest(
                        "Maria Silva", "11999999999", "maria@example.com", apiAddress))
                .vehicleId(vehicleId)
                .vehicle(new CreateServiceOrderVehicleRequest("ABC1D23", "Honda", "Civic", 2020))
                .parts(List.of(new CreateServiceOrderPartRequest(partId, 3)))
                .generateBudget(false);

        var command = ServiceOrderRestMapper.toCommand(request);

        assertThat(command.customer()).isNotNull();
        assertThat(command.customer().address().complement()).isEqualTo("10 andar");
        assertThat(command.vehicleId()).isEqualTo(vehicleId);
        assertThat(command.vehicle().mileage()).isZero();
        assertThat(command.parts()).hasSize(1);
        assertThat(command.parts().getFirst().partId()).isEqualTo(partId);
        assertThat(command.generateBudget()).isFalse();
    }

    @Test
    void shouldMapQueryDatesStatusAndNulls() {
        OffsetDateTime createdFrom = OffsetDateTime.now(ZoneOffset.UTC).minusDays(2);
        OffsetDateTime createdTo = OffsetDateTime.now(ZoneOffset.UTC);

        var query = ServiceOrderRestMapper.toQuery(
                ServiceOrderStatus.WAITING_APPROVAL, UUID.randomUUID(), UUID.randomUUID(), createdFrom, createdTo);
        var nullQuery = ServiceOrderRestMapper.toQuery(null, null, null, null, null);

        assertThat(query.status()).isEqualTo(br.com.autocarehub.domain.enums.ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(query.createdFrom()).isEqualTo(createdFrom.toLocalDateTime());
        assertThat(query.createdTo()).isEqualTo(createdTo.toLocalDateTime());
        assertThat(nullQuery.status()).isNull();
        assertThat(nullQuery.createdFrom()).isNull();
        assertThat(nullQuery.createdTo()).isNull();
    }

    @Test
    void shouldPageServiceOrderListResponses() {
        Customer customer = customer();
        Vehicle vehicle = vehicle(customer);
        ServiceOrder first = new ServiceOrder(customer.id(), vehicle.id(), "Primeira OS");
        ServiceOrder second = new ServiceOrder(customer.id(), vehicle.id(), "Segunda OS");

        ServiceOrderListResponse response = ServiceOrderRestMapper.toListResponse(List.of(first, second), 1, 1);
        ServiceOrderListResponse emptyPage = ServiceOrderRestMapper.toListResponse(List.of(first), 2, 1);
        ServiceOrderListResponse zeroSize = ServiceOrderRestMapper.toListResponse(List.of(first), 0, 0);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getId()).isEqualTo(second.id());
        assertThat(emptyPage.getItems()).isEmpty();
        assertThat(zeroSize.getItems()).isEmpty();
    }

    @Test
    void shouldExposePendingBudgetAndOnlyExistingStatusEventsOnTrackingResponse() {
        Customer customer = customer();
        Vehicle vehicle = vehicle(customer);
        ServiceOrder serviceOrder = new ServiceOrder(customer.id(), vehicle.id(), "Cliente relata barulho no motor");

        ServiceOrderTrackingResponse response = trackingResponse(serviceOrder, customer, vehicle);

        assertThat(response.getBudget().getGenerated()).isFalse();
        assertThat(response.getBudget().getApproved()).isFalse();
        assertThat(response.getBudget().getGeneratedAt()).isNull();
        assertThat(response.getBudget().getApprovedAt()).isNull();
        assertThat(response.getStatusHistory()).hasSize(1);
        assertThat(response.getStatusHistory().getFirst().getOccurredAt()).isNotNull();
        assertThat(response.getStatusHistory().getFirst().getDescription()).isEqualTo("Ordem de serviço criada");
    }

    @Test
    void shouldExposeBudgetApprovalAndExecutionEventsWhenTheyHappened() {
        Customer customer = customer();
        Vehicle vehicle = vehicle(customer);
        ServiceOrder serviceOrder = new ServiceOrder(customer.id(), vehicle.id(), "Cliente relata vazamento de óleo");
        serviceOrder.addService(
                new WorkshopService("Troca de óleo", "Substituição de óleo e filtro", Money.of("120.00"), 60), 1);
        serviceOrder.addPart(
                Part.create(
                        new Part.CatalogData(
                                "Filtro de óleo", "Filtro de óleo do motor", "OIL-MAP-001", "Filtros", "Óleo", "Bosch"),
                        new Part.Pricing(Money.of("30.00"), Money.of("60.00")),
                        10,
                        2),
                1);
        serviceOrder.generateBudget();
        serviceOrder.approveBudget();
        serviceOrder.startExecution();
        serviceOrder.finish();
        serviceOrder.deliver();

        ServiceOrderTrackingResponse response = trackingResponse(serviceOrder, customer, vehicle);

        assertThat(response.getBudget().getGenerated()).isTrue();
        assertThat(response.getBudget().getApproved()).isTrue();
        assertThat(response.getBudget().getGeneratedAt()).isNotNull();
        assertThat(response.getBudget().getApprovedAt()).isNotNull();
        assertThat(response.getStatusHistory())
                .extracting(ServiceOrderStatusHistoryItem::getDescription)
                .containsExactly(
                        "Ordem de serviço criada",
                        "Orçamento gerado e disponibilizado para aprovação",
                        "Orçamento aprovado pelo cliente",
                        "Execução iniciada",
                        "Serviço finalizado",
                        "Veículo entregue");
        assertThat(response.getStatusHistory()).allSatisfy(historyItem -> assertThat(historyItem.getOccurredAt())
                .isNotNull());
    }

    @Test
    void shouldExposeDiagnosisEventWhenDiagnosisIsCurrentStatus() {
        Customer customer = customer();
        Vehicle vehicle = vehicle(customer);
        ServiceOrder serviceOrder = new ServiceOrder(customer.id(), vehicle.id(), "Cliente relata falha na partida");
        serviceOrder.startDiagnosis();

        ServiceOrderTrackingResponse response = trackingResponse(serviceOrder, customer, vehicle);

        assertThat(response.getStatusHistory())
                .extracting(ServiceOrderStatusHistoryItem::getDescription)
                .containsExactly("Ordem de serviço criada", "Diagnóstico iniciado");
    }
}
