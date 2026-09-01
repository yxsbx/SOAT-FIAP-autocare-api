package br.com.autocarehub.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SensitiveDataValidationIntegrationTest {

    private static final String CUSTOMER_ID = "10000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRejectInvalidCpfOnCustomerCreation() throws Exception {
        String token = login();

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Cliente CPF Invalido", "11111111111")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/customers"));
    }

    @Test
    void shouldRejectInvalidCnpjOnCustomerCreation() throws Exception {
        String token = login();

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Empresa CNPJ Invalido", "11.222.333/0001-82")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/customers"));
    }

    @Test
    void shouldRejectInvalidVehiclePlate() throws Exception {
        String token = login();

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "customerId",
                                CUSTOMER_ID,
                                "plate",
                                "ABC12D3",
                                "brand",
                                "Honda",
                                "model",
                                "Civic",
                                "year",
                                2020,
                                "mileage",
                                30000))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"));
    }

    @Test
    void shouldRejectUnexpectedFieldsOnAdministrativeRequests() throws Exception {
        String token = login();

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "customerId",
                                CUSTOMER_ID,
                                "plate",
                                "TST9A99",
                                "brand",
                                "Honda",
                                "model",
                                "Civic",
                                "year",
                                2020,
                                "mileage",
                                30000,
                                "unexpectedField",
                                "must fail"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"));
    }

    @Test
    void shouldRejectServiceOrderWithoutRequestedServices() throws Exception {
        String token = login();

        mockMvc.perform(post("/api/v1/service-orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "customerDocument",
                                "12345678909",
                                "vehicleId",
                                "20000000-0000-0000-0000-000000000001",
                                "diagnosticNotes",
                                "Cliente solicita revisão sem informar serviços",
                                "services",
                                java.util.List.of(),
                                "generateBudget",
                                false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request body"))
                .andExpect(jsonPath("$.path").value("/api/v1/service-orders"));
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "admin@autocarehub.com", "password", "autocare123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String customerJson(String name, String document) throws Exception {
        return json(Map.of(
                "name",
                name,
                "document",
                document,
                "phone",
                "11999999999",
                "email",
                "cliente-" + UUID.randomUUID() + "@example.com",
                "address",
                Map.of(
                        "street",
                        "Avenida Paulista",
                        "number",
                        "1000",
                        "neighborhood",
                        "Bela Vista",
                        "city",
                        "São Paulo",
                        "state",
                        "SP",
                        "zipCode",
                        "01310-100")));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
