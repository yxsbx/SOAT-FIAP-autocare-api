package br.com.autocarehub.application.usecase.vehicle;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.domain.model.Vehicle;
import java.util.UUID;

public class FindVehicleUseCase {

    private final VehicleRepository vehicleRepository;

    public FindVehicleUseCase(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle execute(UUID vehicleId) {
        return vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }
}
