package br.com.autocarehub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.domain.enums.DocumentType;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import org.junit.jupiter.api.Test;

class CustomerTest {

    private static Customer customer() {
        return new Customer("Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", null);
    }

    @Test
    void shouldValidateValidCpf() {
        Customer customer = new Customer(
                "Maria Silva", Document.from("529.982.247-25"), "(11) 99999-9999", "Maria@Example.com", null);

        assertThat(customer.document().type()).isEqualTo(DocumentType.CPF);
        assertThat(customer.document().value()).isEqualTo("52998224725");
        assertThat(customer.phone()).isEqualTo("11999999999");
        assertThat(customer.email()).isEqualTo("maria@example.com");
    }

    @Test
    void shouldRejectInvalidCpf() {
        assertThatThrownBy(() -> new Customer(
                        "Maria Silva", Document.from("11111111111"), "11999999999", "maria@example.com", null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid document");
    }

    @Test
    void shouldValidateValidCnpj() {
        Document document = Document.from("11.222.333/0001-81");

        assertThat(document.type()).isEqualTo(DocumentType.CNPJ);
        assertThat(document.value()).isEqualTo("11222333000181");
    }

    @Test
    void shouldRejectInvalidCnpj() {
        assertThatThrownBy(() -> Document.from("11.222.333/0001-82"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid document");
    }

    @Test
    void shouldUpdateCustomerData() {
        Customer customer = customer();
        Address address = new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", "01001000");

        customer.rename("Maria Souza");
        customer.updateContact("11888888888", "souza@example.com");
        customer.updateAddress(address);

        assertThat(customer.name()).isEqualTo("Maria Souza");
        assertThat(customer.phone()).isEqualTo("11888888888");
        assertThat(customer.email()).isEqualTo("souza@example.com");
        assertThat(customer.address()).isEqualTo(address);
    }

    @Test
    void shouldActivateAndDeactivate() {
        Customer customer = customer();

        customer.deactivate();
        assertThat(customer.active()).isFalse();

        customer.activate();
        assertThat(customer.active()).isTrue();
    }

    @Test
    void shouldRejectInvalidCustomerData() {
        Customer customer = customer();

        assertThatThrownBy(() -> customer.rename(" "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Name is required");
        assertThatThrownBy(() -> customer.updateContact("11999999999", "invalid-email"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid email");
        assertThatThrownBy(() -> customer.updateContact("1234", "maria@example.com"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid phone");
        assertThatThrownBy(() -> Document.from("123"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Document must be CPF or CNPJ");
        assertThatThrownBy(() -> Document.from(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Document must be CPF or CNPJ");
    }
}
