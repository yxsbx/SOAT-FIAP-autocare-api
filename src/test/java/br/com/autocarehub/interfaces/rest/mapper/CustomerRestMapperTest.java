package br.com.autocarehub.interfaces.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.autocarehub.domain.model.Customer;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import br.com.autocarehub.interfaces.rest.generated.model.CreateCustomerRequest;
import br.com.autocarehub.interfaces.rest.generated.model.UpdateCustomerRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CustomerRestMapperTest {

    private static Address address() {
        return new Address("Avenida Paulista", "1000", null, "Bela Vista", "São Paulo", "SP", "01310-100");
    }

    @Test
    void shouldMapCreateAndUpdateCommandsWithAddress() {
        var apiAddress = new br.com.autocarehub.interfaces.rest.generated.model.Address(
                        "Avenida Paulista", "1000", "Bela Vista", "São Paulo", "SP", "01310-100")
                .complement("10 andar");
        CreateCustomerRequest createRequest =
                new CreateCustomerRequest("Maria Silva", "52998224725", "11999999999", "maria@example.com", apiAddress);
        UUID customerId = UUID.randomUUID();
        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                        "Maria Souza", "52998224725", "11888888888", "maria.souza@example.com", apiAddress, null)
                .active(null);

        var createCommand = CustomerRestMapper.toCommand(createRequest);
        var updateCommand = CustomerRestMapper.toCommand(customerId, updateRequest);

        assertThat(createCommand.address().complement()).isEqualTo("10 andar");
        assertThat(updateCommand.customerId()).isEqualTo(customerId);
        assertThat(updateCommand.active()).isFalse();
    }

    @Test
    void shouldMapNullAddressesAndPageListResponses() {
        Customer cpfCustomer = new Customer(
                "Maria Silva", Document.from("52998224725"), "11999999999", "maria@example.com", address());
        Customer cnpjCustomer = new Customer(
                "Auto Peças LTDA",
                Document.from("11222333000181"),
                "1133333333",
                "contato@autopecas.example.com",
                null);

        var firstPage = CustomerRestMapper.toListResponse(List.of(cpfCustomer, cnpjCustomer), 0, 1);
        var allItems = CustomerRestMapper.toListResponse(List.of(cpfCustomer, cnpjCustomer), null, null);
        var emptySize = CustomerRestMapper.toListResponse(List.of(cpfCustomer), 0, 0);
        var fullResponse = CustomerRestMapper.toResponse(cnpjCustomer);

        assertThat(CustomerRestMapper.toDomainAddress(null)).isNull();
        assertThat(CustomerRestMapper.toApiAddress(null)).isNull();
        assertThat(firstPage.getItems()).hasSize(1);
        assertThat(firstPage.getItems().getFirst().getDocument()).isEqualTo("*********25");
        assertThat(allItems.getItems()).hasSize(2);
        assertThat(allItems.getItems().get(1).getDocument()).isEqualTo("**********0181");
        assertThat(emptySize.getItems()).isEmpty();
        assertThat(fullResponse.getAddress()).isNull();
        assertThat(fullResponse.getDocument()).isEqualTo("11222333000181");
    }
}
