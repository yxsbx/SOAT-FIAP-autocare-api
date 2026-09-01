package br.com.autocarehub.interfaces.rest.controller;

import br.com.autocarehub.application.usecase.vehicle.CreateVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.DeleteVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.FindVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.ListVehiclesByCustomerUseCase;
import br.com.autocarehub.application.usecase.vehicle.ListVehiclesUseCase;
import br.com.autocarehub.application.usecase.vehicle.UpdateVehicleUseCase;
import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.interfaces.rest.generated.api.VehiclesApi;
import br.com.autocarehub.interfaces.rest.generated.model.CreateVehicleRequest;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateVehicleRequest;
import br.com.autocarehub.interfaces.rest.generated.model.VehicleListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.VehicleResponse;
import br.com.autocarehub.interfaces.rest.mapper.VehicleRestMapper;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VehiclesController implements VehiclesApi {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final FindVehicleUseCase findVehicleUseCase;
    private final ListVehiclesUseCase listVehiclesUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final ListVehiclesByCustomerUseCase listVehiclesByCustomerUseCase;

    public VehiclesController(
            CreateVehicleUseCase createVehicleUseCase,
            UpdateVehicleUseCase updateVehicleUseCase,
            FindVehicleUseCase findVehicleUseCase,
            ListVehiclesUseCase listVehiclesUseCase,
            DeleteVehicleUseCase deleteVehicleUseCase,
            ListVehiclesByCustomerUseCase listVehiclesByCustomerUseCase) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.updateVehicleUseCase = updateVehicleUseCase;
        this.findVehicleUseCase = findVehicleUseCase;
        this.listVehiclesUseCase = listVehiclesUseCase;
        this.deleteVehicleUseCase = deleteVehicleUseCase;
        this.listVehiclesByCustomerUseCase = listVehiclesByCustomerUseCase;
    }

    @Override
    public ResponseEntity<VehicleResponse> createVehicle(CreateVehicleRequest createVehicleRequest) {
        Vehicle vehicle = createVehicleUseCase.execute(VehicleRestMapper.toCommand(createVehicleRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleRestMapper.toResponse(vehicle));
    }

    @Override
    public ResponseEntity<Void> deleteVehicle(UUID vehicleId) {
        deleteVehicleUseCase.execute(vehicleId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<VehicleResponse> getVehicleById(UUID vehicleId) {
        return ResponseEntity.ok(VehicleRestMapper.toResponse(findVehicleUseCase.execute(vehicleId)));
    }

    @Override
    public ResponseEntity<VehicleListResponse> listVehicles(Integer page, Integer size, @Nullable Boolean active) {
        return ResponseEntity.ok(VehicleRestMapper.toListResponse(
                listVehiclesUseCase.execute(VehicleRestMapper.toQuery(active)), page, size));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE') or @authorizationService.canAccessCustomer(#customerId)")
    public ResponseEntity<VehicleListResponse> listVehiclesByCustomer(UUID customerId) {
        return ResponseEntity.ok(
                VehicleRestMapper.toListResponse(listVehiclesByCustomerUseCase.execute(customerId), null, null));
    }

    @Override
    public ResponseEntity<VehicleResponse> updateVehicle(UUID vehicleId, UpdateVehicleRequest updateVehicleRequest) {
        Vehicle vehicle = updateVehicleUseCase.execute(VehicleRestMapper.toCommand(vehicleId, updateVehicleRequest));
        return ResponseEntity.ok(VehicleRestMapper.toResponse(vehicle));
    }
}
