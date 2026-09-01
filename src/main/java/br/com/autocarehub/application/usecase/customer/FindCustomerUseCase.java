package br.com.autocarehub.application.usecase.customer;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.domain.model.Customer;
import java.util.UUID;

public class FindCustomerUseCase {

    private final CustomerRepository customerRepository;

    public FindCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer execute(UUID customerId) {
        return customerRepository
                .findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
}
