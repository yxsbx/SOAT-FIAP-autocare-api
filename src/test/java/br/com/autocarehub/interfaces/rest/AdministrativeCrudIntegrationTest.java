package br.com.autocarehub.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
class AdministrativeCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldManageAdministrativeResourcesThroughRestApi() throws Exception {
        String token = login();

        UUID customerId = manageCustomer(token);
        manageVehicle(token, customerId);
        manageWorkshopService(token);
        managePart(token);
        manageDemoLead(token);
        manageUsersAndPreferences(token);
    }

    private UUID manageCustomer(String token) throws Exception {
        Map<String, Object> address = Map.of(
                "street", "Rua das Flores",
                "number", "120",
                "neighborhood", "Centro",
                "city", "Curitiba",
                "state", "PR",
                "zipCode", "80010-000");
        Map<String, Object> createRequest = Map.of(
                "name", "Cliente CRUD",
                "document", "11144477735",
                "phone", "41999999999",
                "email", "cliente.crud@example.com",
                "address", address);

        UUID customerId = id(mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cliente CRUD"))
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(get("/api/v1/customers/{customerId}", customerId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document").value("11144477735"));

        mockMvc.perform(get("/api/v1/customers").param("active", "true").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        Map<String, Object> updateRequest = Map.of(
                "name", "Cliente CRUD Atualizado",
                "document", "11144477735",
                "phone", "41988888888",
                "email", "cliente.crud@example.com",
                "address", address,
                "active", true);
        mockMvc.perform(put("/api/v1/customers/{customerId}", customerId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cliente CRUD Atualizado"));

        mockMvc.perform(delete("/api/v1/customers/{customerId}", customerId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        return customerId;
    }

    private void manageVehicle(String token, UUID customerId) throws Exception {
        Map<String, Object> createRequest = Map.of(
                "customerId",
                customerId,
                "plate",
                "CRD1A23",
                "brand",
                "Fiat",
                "model",
                "Argo",
                "year",
                2022,
                "mileage",
                18000);
        UUID vehicleId = id(mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}", vehicleId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("CRD1A23"));

        mockMvc.perform(get("/api/v1/customers/{customerId}/vehicles", customerId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        Map<String, Object> updateRequest = Map.of(
                "customerId",
                customerId,
                "plate",
                "CRD1A23",
                "brand",
                "Fiat",
                "model",
                "Argo",
                "year",
                2022,
                "mileage",
                20000,
                "active",
                true);
        mockMvc.perform(put("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mileage").value(20000));

        Map<String, Object> invalidIdentityUpdateRequest = Map.of(
                "customerId",
                customerId,
                "plate",
                "CRD1A23",
                "brand",
                "Fiat",
                "model",
                "Argo Trekking",
                "year",
                2022,
                "mileage",
                21000,
                "active",
                true);
        mockMvc.perform(put("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalidIdentityUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Vehicle identity data cannot be changed; deactivate it and create a new vehicle"));

        mockMvc.perform(delete("/api/v1/vehicles/{vehicleId}", vehicleId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    private void manageWorkshopService(String token) throws Exception {
        UUID serviceId = id(mockMvc.perform(post("/api/v1/workshop-services")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name",
                                "Alinhamento CRUD",
                                "description",
                                "Alinhamento completo das rodas",
                                "basePrice",
                                120.00,
                                "estimatedTimeInMinutes",
                                45))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(get("/api/v1/workshop-services/{serviceId}", serviceId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alinhamento CRUD"));

        mockMvc.perform(get("/api/v1/workshop-services").param("active", "true").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        mockMvc.perform(put("/api/v1/workshop-services/{serviceId}", serviceId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name",
                                "Alinhamento Premium",
                                "description",
                                "Alinhamento computadorizado das rodas",
                                "basePrice",
                                160.00,
                                "estimatedTimeInMinutes",
                                60,
                                "active",
                                true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alinhamento Premium"));

        mockMvc.perform(delete("/api/v1/workshop-services/{serviceId}", serviceId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    private void managePart(String token) throws Exception {
        Map<String, Object> createRequest = Map.ofEntries(
                Map.entry("name", "Pastilha CRUD"),
                Map.entry("description", "Pastilha dianteira para teste CRUD"),
                Map.entry("sku", "PAD-CRUD-001"),
                Map.entry("category", "Freios"),
                Map.entry("subcategory", "Pastilhas"),
                Map.entry("brand", "Bosch"),
                Map.entry("unitPrice", 180.00),
                Map.entry("costPrice", 100.00),
                Map.entry("stockQuantity", 8),
                Map.entry("minimumStock", 2));
        UUID partId = id(mockMvc.perform(post("/api/v1/parts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(get("/api/v1/parts/{partId}", partId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("PAD-CRUD-001"));

        mockMvc.perform(get("/api/v1/parts").param("active", "true").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        Map<String, Object> updateRequest = Map.ofEntries(
                Map.entry("name", "Pastilha CRUD Premium"),
                Map.entry("description", "Pastilha dianteira premium para teste CRUD"),
                Map.entry("sku", "PAD-CRUD-001"),
                Map.entry("category", "Freios"),
                Map.entry("subcategory", "Pastilhas"),
                Map.entry("brand", "Bosch"),
                Map.entry("unitPrice", 210.00),
                Map.entry("costPrice", 120.00),
                Map.entry("stockQuantity", 8),
                Map.entry("minimumStock", 2),
                Map.entry("active", true));
        mockMvc.perform(put("/api/v1/parts/{partId}", partId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pastilha CRUD Premium"));

        mockMvc.perform(delete("/api/v1/parts/{partId}", partId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    private void manageDemoLead(String token) throws Exception {
        mockMvc.perform(post("/api/v1/demo-leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "contactName", "Contato Demo",
                                "companyName", "Oficina Demo",
                                "demoProfile", "workshop",
                                "email", "demo.crud@example.com",
                                "phone", "41977777777",
                                "cnpj", "11222333000181",
                                "city", "Curitiba",
                                "message", "Solicitação criada pelo teste de integração"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("demo.crud@example.com"));

        mockMvc.perform(get("/api/v1/demo-leads").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private void manageUsersAndPreferences(String token) throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin@autocarehub.com"));

        mockMvc.perform(put("/api/v1/users/me/preferences/home")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "widgets", List.of("orders-progress", "pending-budgets"), "showAlertsOnHome", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showAlertsOnHome").value(true));

        mockMvc.perform(get("/api/v1/users/me/preferences/home").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.widgets[0]").value("orders-progress"));

        Map<String, Object> createRequest = Map.ofEntries(
                Map.entry("username", "crud.employee@example.com"),
                Map.entry("password", "autocare123"),
                Map.entry("role", "EMPLOYEE"),
                Map.entry("fullName", "Funcionário CRUD"),
                Map.entry("profileType", "WORKSHOP_EMPLOYEE"),
                Map.entry("companyName", "Oficina CRUD"),
                Map.entry("companyType", "WORKSHOP"),
                Map.entry("createCompany", true),
                Map.entry("employeeSubRole", "MECHANIC"),
                Map.entry("permissions", List.of("CREATE_ORDER", "EDIT_ORDER")),
                Map.entry("active", true));
        String response = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID userId = id(response);
        UUID companyId =
                UUID.fromString(objectMapper.readTree(response).get("companyId").asText());

        mockMvc.perform(get("/api/v1/users").param("search", "crud.employee").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(userId.toString()));

        mockMvc.perform(get("/api/v1/users/companies").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '" + companyId + "')]").exists());

        mockMvc.perform(get("/api/v1/users/partners").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        Map<String, Object> updateRequest = Map.ofEntries(
                Map.entry("username", "crud.employee@example.com"),
                Map.entry("role", "EMPLOYEE"),
                Map.entry("fullName", "Funcionário CRUD Atualizado"),
                Map.entry("profileType", "WORKSHOP_EMPLOYEE"),
                Map.entry("companyId", companyId.toString()),
                Map.entry("companyName", "Oficina CRUD"),
                Map.entry("companyType", "WORKSHOP"),
                Map.entry("createCompany", false),
                Map.entry("employeeSubRole", "ADVISOR"),
                Map.entry("permissions", List.of("CREATE_ORDER")),
                Map.entry("active", true));
        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Funcionário CRUD Atualizado"));

        mockMvc.perform(patch("/api/v1/users/{userId}/password", userId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("newPassword", "novaSenha123"))))
                .andExpect(status().isNoContent());
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "admin@autocarehub.com",
                                "password", "autocare123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private UUID id(String response) throws Exception {
        JsonNode node = objectMapper.readTree(response);
        return UUID.fromString(node.get("id").asText());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
