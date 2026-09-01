package br.com.autocarehub.interfaces.rest.mapper;

import br.com.autocarehub.application.usecase.customer.CreateCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.ListCustomersUseCase;
import br.com.autocarehub.application.usecase.customer.UpdateCustomerUseCase;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.interfaces.rest.generated.model.CreateCustomerRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CustomerListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.CustomerResponse;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateCustomerRequest;
import java.util.List;
import java.util.UUID;

public final class CustomerRestMapper {

    private static final int CNPJ_LENGTH = 14;
    private static final int CNPJ_VISIBLE_END_DIGITS = 4;
    private static final int DEFAULT_VISIBLE_END_DIGITS = 2;
    private static final int MIN_MASKABLE_DOCUMENT_LENGTH = 5;

    private CustomerRestMapper() {}

    public static CreateCustomerUseCase.Command toCommand(CreateCustomerRequest request) {
        return new CreateCustomerUseCase.Command(
                request.getName(),
                request.getDocument(),
                request.getPhone(),
                request.getEmail(),
                toDomainAddress(request.getAddress()));
    }

    public static UpdateCustomerUseCase.Command toCommand(UUID customerId, UpdateCustomerRequest request) {
        return new UpdateCustomerUseCase.Command(
                customerId,
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                toDomainAddress(request.getAddress()),
                Boolean.TRUE.equals(request.getActive()));
    }

    public static ListCustomersUseCase.Query toQuery(Boolean active) {
        return new ListCustomersUseCase.Query(active);
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.id(),
                customer.name(),
                customer.document().value(),
                customer.phone(),
                customer.email(),
                toApiAddress(customer.address()),
                customer.active(),
                RestMapperSupport.toOffsetDateTime(customer.createdAt()));
    }

    public static CustomerListResponse toListResponse(List<Customer> customers, Integer page, Integer size) {
        List<CustomerResponse> items = RestMapperSupport.page(customers, page, size).stream()
                .map(CustomerRestMapper::toListItemResponse)
                .toList();

        return new CustomerListResponse(
                items,
                page == null ? 0 : page,
                size == null ? customers.size() : size,
                (long) customers.size(),
                RestMapperSupport.totalPages(customers.size(), size));
    }

    private static CustomerResponse toListItemResponse(Customer customer) {
        return new CustomerResponse(
                customer.id(),
                customer.name(),
                maskDocument(customer.document().value()),
                customer.phone(),
                customer.email(),
                toApiAddress(customer.address()),
                customer.active(),
                RestMapperSupport.toOffsetDateTime(customer.createdAt()));
    }

    private static String maskDocument(String document) {
        if (document == null || document.length() < MIN_MASKABLE_DOCUMENT_LENGTH) {
            return "****";
        }
        int visibleEndDigits = document.length() == CNPJ_LENGTH ? CNPJ_VISIBLE_END_DIGITS : DEFAULT_VISIBLE_END_DIGITS;
        int maskedLength = document.length() - visibleEndDigits;
        return "*".repeat(maskedLength) + document.substring(maskedLength);
    }

    public static br.com.autocarehub.domain.valueobject.Address toDomainAddress(
            br.com.autocarehub.interfaces.rest.generated.model.Address address) {
        if (address == null) {
            return null;
        }
        return new br.com.autocarehub.domain.valueobject.Address(
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZipCode());
    }

    public static br.com.autocarehub.interfaces.rest.generated.model.Address toApiAddress(
            br.com.autocarehub.domain.valueobject.Address address) {
        if (address == null) {
            return null;
        }
        return new br.com.autocarehub.interfaces.rest.generated.model.Address(
                        address.street(),
                        address.number(),
                        address.neighborhood(),
                        address.city(),
                        address.state(),
                        address.zipCode())
                .complement(address.complement());
    }
}
