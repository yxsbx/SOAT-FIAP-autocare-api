package br.com.autocarehub.interfaces.rest.controller;

import br.com.autocarehub.application.usecase.customer.CreateCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.DeleteCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.FindCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.ListCustomersUseCase;
import br.com.autocarehub.application.usecase.customer.UpdateCustomerUseCase;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.interfaces.rest.generated.api.CustomersApi;
import br.com.autocarehub.interfaces.rest.generated.model.CreateCustomerRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CustomerListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.CustomerResponse;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateCustomerRequest;
import br.com.autocarehub.interfaces.rest.mapper.CustomerRestMapper;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomersController implements CustomersApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final FindCustomerUseCase findCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;

    public CustomersController(
            CreateCustomerUseCase createCustomerUseCase,
            UpdateCustomerUseCase updateCustomerUseCase,
            FindCustomerUseCase findCustomerUseCase,
            ListCustomersUseCase listCustomersUseCase,
            DeleteCustomerUseCase deleteCustomerUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.findCustomerUseCase = findCustomerUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.deleteCustomerUseCase = deleteCustomerUseCase;
    }

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(CreateCustomerRequest createCustomerRequest) {
        Customer customer = createCustomerUseCase.execute(CustomerRestMapper.toCommand(createCustomerRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerRestMapper.toResponse(customer));
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(UUID customerId) {
        deleteCustomerUseCase.execute(customerId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomerById(UUID customerId) {
        return ResponseEntity.ok(CustomerRestMapper.toResponse(findCustomerUseCase.execute(customerId)));
    }

    @Override
    public ResponseEntity<CustomerListResponse> listCustomers(Integer page, Integer size, @Nullable Boolean active) {
        return ResponseEntity.ok(CustomerRestMapper.toListResponse(
                listCustomersUseCase.execute(CustomerRestMapper.toQuery(active)), page, size));
    }

    @Override
    public ResponseEntity<CustomerResponse> updateCustomer(
            UUID customerId, UpdateCustomerRequest updateCustomerRequest) {
        Customer customer =
                updateCustomerUseCase.execute(CustomerRestMapper.toCommand(customerId, updateCustomerRequest));
        return ResponseEntity.ok(CustomerRestMapper.toResponse(customer));
    }
}
