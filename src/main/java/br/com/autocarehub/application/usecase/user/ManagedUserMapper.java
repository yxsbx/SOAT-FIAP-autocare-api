package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.domain.model.User;

final class ManagedUserMapper {

    private ManagedUserMapper() {}

    static CreateUserUseCase.Command toCreateCommand(UserManagementPolicy.ManagedUserCommand command, String password) {
        return new CreateUserUseCase.Command(
                command.username(),
                password,
                command.role(),
                command.customerId(),
                command.companyId(),
                command.fullName(),
                command.profileType(),
                command.companyName(),
                command.companyType(),
                command.employeeSubRole(),
                command.permissions(),
                command.active());
    }

    static UpdateUserUseCase.Command toUpdateCommand(User user, UserManagementPolicy.ManagedUserCommand command) {
        return new UpdateUserUseCase.Command(
                user.id(),
                command.username(),
                command.role(),
                command.customerId(),
                command.companyId(),
                command.fullName(),
                command.profileType(),
                command.companyName(),
                command.companyType(),
                command.employeeSubRole(),
                command.permissions(),
                command.active());
    }
}
