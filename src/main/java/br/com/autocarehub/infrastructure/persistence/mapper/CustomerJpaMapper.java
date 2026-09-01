package br.com.autocarehub.infrastructure.persistence.mapper;

import br.com.autocarehub.domain.enums.DocumentType;
import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import br.com.autocarehub.infrastructure.persistence.entity.CustomerJpaEntity;
import java.util.Objects;

public final class CustomerJpaMapper {

    private CustomerJpaMapper() {}

    public static CustomerJpaEntity toEntity(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(customer.id());
        entity.setName(customer.name());
        entity.setDocumentType(customer.document().type().name());
        entity.setDocumentValue(customer.document().value());
        entity.setPhone(customer.phone());
        entity.setEmail(customer.email());
        entity.setActive(customer.active());
        entity.setCreatedAt(customer.createdAt());
        Address address = Objects.requireNonNull(customer.address(), "customer address is required");
        entity.setAddressStreet(address.street());
        entity.setAddressNumber(address.number());
        entity.setAddressComplement(address.complement());
        entity.setAddressNeighborhood(address.neighborhood());
        entity.setAddressCity(address.city());
        entity.setAddressState(address.state());
        entity.setAddressZipCode(address.zipCode());
        return entity;
    }

    public static Customer toDomain(CustomerJpaEntity entity) {
        Address address = getAddress(entity);
        return new Customer(
                entity.getId(),
                entity.getName(),
                new Document(DocumentType.valueOf(entity.getDocumentType()), entity.getDocumentValue()),
                entity.getPhone(),
                entity.getEmail(),
                address,
                entity.isActive(),
                entity.getCreatedAt());
    }

    private static Address getAddress(CustomerJpaEntity entity) {
        Address address = null;
        if (entity.getAddressStreet() != null) {
            address = new Address(
                    entity.getAddressStreet(),
                    entity.getAddressNumber(),
                    entity.getAddressComplement(),
                    entity.getAddressNeighborhood(),
                    entity.getAddressCity(),
                    entity.getAddressState(),
                    entity.getAddressZipCode());
        }
        return address;
    }
}
