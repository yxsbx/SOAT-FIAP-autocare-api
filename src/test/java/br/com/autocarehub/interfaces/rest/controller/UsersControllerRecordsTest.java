package br.com.autocarehub.interfaces.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UsersControllerRecordsTest {

    @Test
    void shouldExerciseUserControllerRecordContracts() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UsersController.UserResponse user = new UsersController.UserResponse(
                userId,
                "admin@autocarehub.com",
                "ADMIN",
                customerId,
                companyId,
                "Admin",
                "WORKSHOP_ADMIN",
                "AutoCare",
                "WORKSHOP",
                "",
                List.of("USERS_READ"),
                true);
        UsersController.UserResponse sameUser = new UsersController.UserResponse(
                userId,
                "admin@autocarehub.com",
                "ADMIN",
                customerId,
                companyId,
                "Admin",
                "WORKSHOP_ADMIN",
                "AutoCare",
                "WORKSHOP",
                "",
                List.of("USERS_READ"),
                true);
        UsersController.UserResponse differentUser = new UsersController.UserResponse(
                UUID.randomUUID(),
                "employee@autocarehub.com",
                "EMPLOYEE",
                null,
                companyId,
                "Employee",
                "WORKSHOP_EMPLOYEE",
                "AutoCare",
                "WORKSHOP",
                "CONSULTANT",
                List.of("ORDERS_READ"),
                false);

        assertThat(user)
                .isEqualTo(user)
                .isEqualTo(sameUser)
                .isNotEqualTo(differentUser)
                .isNotEqualTo(null);
        assertThat(user.hashCode()).isEqualTo(sameUser.hashCode());
        assertThat(user.toString()).contains("admin@autocarehub.com");

        UsersController.UserListResponse userList = new UsersController.UserListResponse(List.of(user));
        assertThat(userList.items()).containsExactly(user);
        assertThat(userList).isEqualTo(new UsersController.UserListResponse(List.of(sameUser)));

        UsersController.CompanyResponse company =
                new UsersController.CompanyResponse(companyId, "AutoCare", "WORKSHOP", true);
        UsersController.CompanyResponse sameCompany =
                new UsersController.CompanyResponse(companyId, "AutoCare", "WORKSHOP", true);
        assertThat(company).isEqualTo(sameCompany).isNotEqualTo(new Object());
        assertThat(new UsersController.CompanyListResponse(List.of(company)))
                .isEqualTo(new UsersController.CompanyListResponse(List.of(sameCompany)));

        UsersController.CreateUserRequest create = new UsersController.CreateUserRequest(
                "new@autocarehub.com",
                "secret123",
                "ADMIN",
                null,
                companyId,
                "New User",
                "WORKSHOP_ADMIN",
                "AutoCare",
                "WORKSHOP",
                false,
                "",
                List.of("USERS_WRITE"),
                true);
        UsersController.UpdateUserRequest update = new UsersController.UpdateUserRequest(
                "new@autocarehub.com",
                "ADMIN",
                null,
                companyId,
                "New User",
                "WORKSHOP_ADMIN",
                "AutoCare",
                "WORKSHOP",
                false,
                "",
                List.of("USERS_WRITE"),
                true);

        assertThat(create.username()).isEqualTo(update.username());
        assertThat(create).isNotEqualTo(update);
        assertThat(new UsersController.UpdateCurrentUserRequest("New User").fullName())
                .isEqualTo("New User");
        assertThat(new UsersController.ChangePasswordRequest("oldSecret", "newSecret123").newPassword())
                .isEqualTo("newSecret123");
        assertThat(new UsersController.ResetPasswordRequest("newSecret123").newPassword())
                .isEqualTo("newSecret123");
        assertThat(new UsersController.HomePreferenceRequest(List.of("orders-progress"), true).showAlertsOnHome())
                .isTrue();
        assertThat(new UsersController.HomePreferenceResponse(List.of("orders-progress"), false).widgets())
                .containsExactly("orders-progress");
    }
}
