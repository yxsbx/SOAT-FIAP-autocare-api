package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.domain.model.User;
import java.util.List;

public class ListPartnerUsersUseCase {

    private final ListUsersUseCase listUsersUseCase;

    public ListPartnerUsersUseCase(ListUsersUseCase listUsersUseCase) {
        this.listUsersUseCase = listUsersUseCase;
    }

    public List<User> execute() {
        return listUsersUseCase.execute(new ListUsersUseCase.Query(true, "ADMIN", null, null)).stream()
                .filter(user ->
                        "WORKSHOP_ADMIN".equals(user.profileType()) || "PARTS_STORE_ADMIN".equals(user.profileType()))
                .toList();
    }
}
