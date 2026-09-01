package br.com.autocarehub.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
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
class UserCompanyManagementIntegrationTest {

    private static final String PLATFORM_COMPANY_ID = "90000000-0000-0000-0000-000000000001";
    private static final String WORKSHOP_COMPANY_ID = "90000000-0000-0000-0000-000000000011";
    private static final String PARTS_STORE_COMPANY_ID = "90000000-0000-0000-0000-000000000012";
    private static final String WORKSHOP_EMPLOYEE_ID = "00000000-0000-0000-0000-000000000013";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateUsersUsingGeneratedCompanyIdsAndExistingCompanyNames() throws Exception {
        String token = login("admin@autocarehub.com");
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(get("/api/v1/users/companies").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.name == 'AutoCare Hub')]").exists())
                .andExpect(jsonPath("$.items[?(@.name == 'Oficina Central AutoCare')]")
                        .exists())
                .andExpect(jsonPath("$.items[?(@.name == 'Loja peças Prime')]").exists());

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "master.coverage." + suffix + "@example.com",
                                "ADMIN",
                                "MASTER_ADMIN",
                                PLATFORM_COMPANY_ID,
                                "",
                                "PLATFORM",
                                false,
                                ""))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("AutoCare Hub"))
                .andExpect(jsonPath("$.companyType").value("PLATFORM"));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "cliente.coverage." + suffix + "@example.com",
                                "CUSTOMER",
                                "CUSTOMER_OWNER",
                                null,
                                "",
                                "",
                                false,
                                ""))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").doesNotExist())
                .andExpect(jsonPath("$.companyName").value(""));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "oficina.admin.coverage." + suffix + "@example.com",
                                "ADMIN",
                                "WORKSHOP_ADMIN",
                                WORKSHOP_COMPANY_ID,
                                "Nome ignorado pelo vínculo",
                                "WORKSHOP",
                                false,
                                ""))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(WORKSHOP_COMPANY_ID))
                .andExpect(jsonPath("$.companyName").value("Oficina Central AutoCare"));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "loja.func.coverage." + suffix + "@example.com",
                                "EMPLOYEE",
                                "PARTS_STORE_EMPLOYEE",
                                null,
                                "Loja peças Prime",
                                "PARTS_STORE",
                                false,
                                ""))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(PARTS_STORE_COMPANY_ID))
                .andExpect(jsonPath("$.employeeSubRole").value("UNSPECIFIED"));
    }

    @Test
    void shouldAcceptMinimalCustomerOwnerPayloadWithoutOptionalCompanyFields() throws Exception {
        String token = login("admin@autocarehub.com");
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> createPayload = new LinkedHashMap<>();
        createPayload.put("username", "cliente.minimo." + suffix + "@example.com");
        createPayload.put("password", "autocare123");
        createPayload.put("role", "CUSTOMER");
        createPayload.put("fullName", "Cliente Mínimo");
        createPayload.put("profileType", "CUSTOMER_OWNER");
        createPayload.put("active", true);

        String response = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.companyName").value(""))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();
        Map<String, Object> updatePayload = new LinkedHashMap<>();
        updatePayload.put("username", "cliente.minimo.atualizado." + suffix + "@example.com");
        updatePayload.put("role", "CUSTOMER");
        updatePayload.put("fullName", "Cliente Mínimo Atualizado");
        updatePayload.put("profileType", "CUSTOMER_OWNER");
        updatePayload.put("active", false);

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Cliente Mínimo Atualizado"))
                .andExpect(jsonPath("$.companyName").value(""))
                .andExpect(jsonPath("$.permissions").isArray());
    }

    @Test
    void shouldRejectInvalidCompanyBindingsAndDuplicateCompanyCreation() throws Exception {
        String token = login("admin@autocarehub.com");
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "tipo.invalido." + suffix + "@example.com",
                                "ADMIN",
                                "WORKSHOP_ADMIN",
                                PARTS_STORE_COMPANY_ID,
                                "Loja peças Prime",
                                "WORKSHOP",
                                false,
                                ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Company type dões not match user profile"));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "empresa.inexistente." + suffix + "@example.com",
                                "EMPLOYEE",
                                "WORKSHOP_EMPLOYEE",
                                null,
                                "Empresa Inexistente Coverage",
                                "WORKSHOP",
                                false,
                                "ATTENDANT"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Company not found"));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "empresa.duplicada." + suffix + "@example.com",
                                "ADMIN",
                                "WORKSHOP_ADMIN",
                                null,
                                "Oficina Central AutoCare",
                                "WORKSHOP",
                                true,
                                ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Company already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "empresa.sem.nome." + suffix + "@example.com",
                                "EMPLOYEE",
                                "WORKSHOP_EMPLOYEE",
                                null,
                                "",
                                "WORKSHOP",
                                true,
                                "MECHANIC"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Company name is required"));
    }

    @Test
    void shouldScopeCompanyAdminsToTheirOwnCompany() throws Exception {
        String storeToken = login("loja.admin@autocarehub.com");
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(get("/api/v1/users/companies").header("Authorization", bearer(storeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(PARTS_STORE_COMPANY_ID));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(storeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(userPayload(
                                "loja.escopo." + suffix + "@example.com",
                                "ADMIN",
                                "WORKSHOP_ADMIN",
                                WORKSHOP_COMPANY_ID,
                                "Oficina Central AutoCare",
                                "WORKSHOP",
                                true,
                                "MECHANIC"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.profileType").value("PARTS_STORE_EMPLOYEE"))
                .andExpect(jsonPath("$.companyId").value(PARTS_STORE_COMPANY_ID))
                .andExpect(jsonPath("$.employeeSubRole").value("UNSPECIFIED"));

        Map<String, Object> updateOutsideScope = userPayload(
                "oficina.funcionario@autocarehub.com",
                "EMPLOYEE",
                "PARTS_STORE_EMPLOYEE",
                PARTS_STORE_COMPANY_ID,
                "Loja peças Prime",
                "PARTS_STORE",
                false,
                "ATTENDANT");
        updateOutsideScope.remove("password");

        mockMvc.perform(put("/api/v1/users/{userId}", WORKSHOP_EMPLOYEE_ID)
                        .header("Authorization", bearer(storeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateOutsideScope)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is outside the current company scope"));
    }

    private Map<String, Object> userPayload(
            String username,
            String role,
            String profileType,
            String companyId,
            String companyName,
            String companyType,
            boolean createCompany,
            String employeeSubRole) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", username);
        payload.put("password", "autocare123");
        payload.put("role", role);
        payload.put("fullName", "Usuário Coverage");
        payload.put("profileType", profileType);
        payload.put("companyName", companyName);
        payload.put("companyType", companyType);
        payload.put("createCompany", createCompany);
        payload.put("employeeSubRole", employeeSubRole);
        payload.put("permissions", List.of("CREATE_ORDER", "EDIT_ORDER", "MANAGE_STOCK"));
        payload.put("active", true);
        if (companyId != null) {
            payload.put("companyId", companyId);
        }
        return payload;
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
