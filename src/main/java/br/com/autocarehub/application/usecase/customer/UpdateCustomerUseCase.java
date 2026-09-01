package br.com.autocarehub.application.usecase.customer;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.valueobject.Address;
import java.util.UUID;

public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public UpdateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer execute(Command command) {
        Customer customer = customerRepository
                .findById(command.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.rename(command.name());
        customer.updateContact(command.phone(), command.email());
        customer.updateAddress(command.address());
        if (command.active()) {
            customer.activate();
        } else {
            customer.deactivate();
        }
        return customerRepository.save(customer);
    }

    public record Command(UUID customerId, String name, String phone, String email, Address address, boolean active) {}
}
