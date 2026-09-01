package br.com.autocarehub.application.usecase.vehicle;

import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.domain.model.Vehicle;
import java.util.List;

public class ListVehiclesUseCase {

    private final VehicleRepository vehicleRepository;

    public ListVehiclesUseCase(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> execute() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> execute(Query query) {
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> query.active() == null || vehicle.active() == query.active())
                .toList();
    }

    public record Query(Boolean active) {}
}
