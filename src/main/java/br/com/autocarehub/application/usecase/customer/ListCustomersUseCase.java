package br.com.autocarehub.application.usecase.customer;

import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.domain.model.Customer;
import java.util.List;

public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;

    public ListCustomersUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> execute() {
        return customerRepository.findAll();
    }

    public List<Customer> execute(Query query) {
        return customerRepository.findAll().stream()
                .filter(customer -> query.active() == null || customer.active() == query.active())
                .toList();
    }

    public record Query(Boolean active) {}
}
