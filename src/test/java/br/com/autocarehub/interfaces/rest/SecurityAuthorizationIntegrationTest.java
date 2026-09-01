package br.com.autocarehub.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationIntegrationTest {

    private static final String CUSTOMER_ID = "10000000-0000-0000-0000-000000000001";
    private static final String OTHER_CUSTOMER_ID = "10000000-0000-0000-0000-000000000002";
    private static final String CUSTOMER_ORDER_ID = "50000000-0000-0000-0000-000000000002";
    private static final String OTHER_CUSTOMER_ORDER_ID = "50000000-0000-0000-0000-000000000003";
    private static final String CUSTOMER_DOCUMENT = "12345678909";
    private static final String EXTERNAL_SERVICE_TOKEN = "test-external-service-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRequireAuthenticationForAdministrativeApis() throws Exception {
        int status = mockMvc.perform(get("/api/v1/customers"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isIn(401, 403);
    }

    @Test
    void shouldExposeOpenApiAndSwaggerUiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cross-Origin-Resource-Policy", "same-origin"));

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));

        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());

        mockMvc.perform(get("/webjars/swagger-ui/5.32.6/swagger-ui.css")).andExpect(status().isOk());
    }

    @Test
    void shouldExposeHealthcheckWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdministrativeApiAccessWithValidAdminJwt() throws Exception {
        String token = login("admin@autocarehub.com");

        mockMvc.perform(get("/api/v1/customers").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void shouldRestrictCompanyAdminUserManagementToOwnCompanyEmployees() throws Exception {
        String token = login("oficina.admin@autocarehub.com");

        mockMvc.perform(get("/api/v1/users").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items[?(@.profileType == 'MASTER_ADMIN')]").isEmpty())
                .andExpect(jsonPath("$.items[?(@.companyName == 'Loja peças Prime')]")
                        .isEmpty());

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.ofEntries(
                                Map.entry("username", "master.falso@example.com"),
                                Map.entry("password", "autocare123"),
                                Map.entry("role", "ADMIN"),
                                Map.entry("fullName", "Master Falso"),
                                Map.entry("profileType", "MASTER_ADMIN"),
                                Map.entry("companyName", "AutoCare Hub"),
                                Map.entry("companyType", "PLATFORM"),
                                Map.entry("employeeSubRole", ""),
                                Map.entry("permissions", java.util.List.of("VIEW_STATS")),
                                Map.entry("active", true)))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBlockCustomerFromAdministrativeApis() throws Exception {
        String token = login("cliente@autocarehub.com");

        mockMvc.perform(get("/api/v1/customers").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowCustomerToTrackOnlyOwnServiceOrders() throws Exception {
        String token = login("cliente@autocarehub.com");

        mockMvc.perform(get("/api/v1/customers/{customerId}/service-orders", CUSTOMER_ID)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        mockMvc.perform(get("/api/v1/customers/{customerId}/service-orders", OTHER_CUSTOMER_ID)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowCustomerToUseTrackingApiOnlyForOwnDocument() throws Exception {
        String token = login("cliente@autocarehub.com");

        mockMvc.perform(get("/api/v1/service-orders/tracking")
                        .param("serviceOrderId", CUSTOMER_ORDER_ID)
                        .param("customerDocument", CUSTOMER_DOCUMENT)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(CUSTOMER_ORDER_ID));

        mockMvc.perform(get("/api/v1/service-orders/tracking")
                        .param("serviceOrderId", OTHER_CUSTOMER_ORDER_ID)
                        .param("customerDocument", CUSTOMER_DOCUMENT)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowCustomerToApproveOnlyOwnBudget() throws Exception {
        String token = login("cliente@autocarehub.com");

        mockMvc.perform(post("/api/v1/service-orders/{serviceOrderId}/budget/approve", CUSTOMER_ORDER_ID)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedAt").isNotEmpty());

        mockMvc.perform(post("/api/v1/service-orders/{serviceOrderId}/budget/approve", OTHER_CUSTOMER_ORDER_ID)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowExternalBudgetDecisionWithoutUserJwtWhenExternalTokenIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/service-orders/{serviceOrderId}/budget/decision", CUSTOMER_ORDER_ID)
                        .header("X-External-Service-Token", EXTERNAL_SERVICE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("decision", "APPROVED", "source", "email"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedAt").isNotEmpty());
    }

    private String login(String username) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "autocare123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
