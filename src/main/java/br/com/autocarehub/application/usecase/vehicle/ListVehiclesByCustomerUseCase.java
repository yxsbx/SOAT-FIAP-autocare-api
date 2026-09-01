package br.com.autocarehub.application.usecase.vehicle;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.domain.model.Vehicle;
import java.util.List;
import java.util.UUID;

public class ListVehiclesByCustomerUseCase {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public ListVehiclesByCustomerUseCase(VehicleRepository vehicleRepository, CustomerRepository customerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
    }

    public List<Vehicle> execute(UUID customerId) {
        customerRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return vehicleRepository.findByCustomerId(customerId);
    }
}
