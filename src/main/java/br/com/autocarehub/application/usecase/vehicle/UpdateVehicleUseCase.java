package br.com.autocarehub.application.usecase.vehicle;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.domain.valueobject.Plate;
import java.util.UUID;

public class UpdateVehicleUseCase {

    private final VehicleRepository vehicleRepository;

    public UpdateVehicleUseCase(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle execute(Command command) {
        Vehicle vehicle = vehicleRepository
                .findById(command.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        Plate plate = new Plate(command.plate());
        if (!vehicle.plate().equals(plate)
                || !vehicle.brand().equals(command.brand().trim())
                || !vehicle.model().equals(command.model().trim())
                || vehicle.year() != command.year()) {
            throw new DomainException(
                    "Vehicle identity data cannot be changed; deactivate it and create a new vehicle");
        }
        vehicle.updateMileage(command.mileage());
        if (command.active()) {
            vehicle.activate();
        } else {
            vehicle.deactivate();
        }
        return vehicleRepository.save(vehicle);
    }

    public record Command(
            UUID vehicleId, String plate, String brand, String model, int year, int mileage, boolean active) {}
}
