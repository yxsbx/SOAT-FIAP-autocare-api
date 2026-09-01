package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.service.DomainValidation;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class Customer {

    private static final int NAME_MAX_LENGTH = 120;

    private final UUID id;
    private final Document document;
    private final LocalDateTime createdAt;
    private String name;
    private String phone;
    private String email;
    private @Nullable Address address;
    private boolean active;

    public Customer(String name, Document document, String phone, String email, @Nullable Address address) {
        this(UUID.randomUUID(), name, document, phone, email, address, true, LocalDateTime.now());
    }

    public Customer(
            UUID id,
            String name,
            Document document,
            String phone,
            String email,
            @Nullable Address address,
            boolean active,
            LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = DomainValidation.requireText(name, "Name is required", NAME_MAX_LENGTH);
        this.document = Objects.requireNonNull(document, "document is required");
        this.phone = DomainValidation.requirePhone(phone);
        this.email = DomainValidation.requireEmail(email);
        this.address = address;
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public void rename(String name) {
        this.name = DomainValidation.requireText(name, "Name is required", NAME_MAX_LENGTH);
    }

    public void updateContact(String phone, String email) {
        this.phone = DomainValidation.requirePhone(phone);
        this.email = DomainValidation.requireEmail(email);
    }

    public void updateAddress(@Nullable Address address) {
        this.address = address;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Document document() {
        return document;
    }

    public String phone() {
        return phone;
    }

    public String email() {
        return email;
    }

    public @Nullable Address address() {
        return address;
    }

    public boolean active() {
        return active;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }
}
