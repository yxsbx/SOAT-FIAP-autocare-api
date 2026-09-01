package br.com.autocarehub.interfaces.rest.mapper;

import br.com.autocarehub.application.usecase.vehicle.CreateVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.ListVehiclesUseCase;
import br.com.autocarehub.application.usecase.vehicle.UpdateVehicleUseCase;
import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.interfaces.rest.generated.model.CreateVehicleRequest;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateVehicleRequest;
import br.com.autocarehub.interfaces.rest.generated.model.VehicleListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.VehicleResponse;
import java.util.List;
import java.util.UUID;

public final class VehicleRestMapper {

    private VehicleRestMapper() {}

    public static CreateVehicleUseCase.Command toCommand(CreateVehicleRequest request) {
        return new CreateVehicleUseCase.Command(
                request.getCustomerId(),
                request.getPlate(),
                request.getBrand(),
                request.getModel(),
                request.getYear(),
                request.getMileage());
    }

    public static UpdateVehicleUseCase.Command toCommand(UUID vehicleId, UpdateVehicleRequest request) {
        return new UpdateVehicleUseCase.Command(
                vehicleId,
                request.getPlate(),
                request.getBrand(),
                request.getModel(),
                request.getYear(),
                request.getMileage(),
                Boolean.TRUE.equals(request.getActive()));
    }

    public static ListVehiclesUseCase.Query toQuery(Boolean active) {
        return new ListVehiclesUseCase.Query(active);
    }

    public static VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.id(),
                vehicle.customerId(),
                vehicle.plate().value(),
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.mileage(),
                vehicle.active());
    }

    public static VehicleListResponse toListResponse(List<Vehicle> vehicles, Integer page, Integer size) {
        return new VehicleListResponse(RestMapperSupport.page(vehicles, page, size).stream()
                .map(VehicleRestMapper::toResponse)
                .toList());
    }
}
