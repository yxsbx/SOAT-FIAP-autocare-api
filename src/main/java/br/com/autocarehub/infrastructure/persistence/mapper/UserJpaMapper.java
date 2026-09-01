package br.com.autocarehub.infrastructure.persistence.mapper;

import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.User;
import br.com.autocarehub.infrastructure.persistence.entity.UserJpaEntity;
import java.util.Arrays;
import java.util.List;

public final class UserJpaMapper {

    private UserJpaMapper() {}

    public static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getPasswordHash(),
                UserRole.valueOf(entity.getRole()),
                entity.getCustomerId(),
                entity.getCompanyId(),
                entity.getFullName(),
                entity.getProfileType(),
                entity.getCompanyName(),
                entity.getCompanyType(),
                entity.getEmployeeSubRole(),
                toPermissions(entity.getPermissions()),
                entity.isActive(),
                entity.getCreatedAt());
    }

    public static UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.id());
        entity.setUsername(user.username());
        entity.setPasswordHash(user.passwordHash());
        entity.setRole(user.role().name());
        entity.setCustomerId(user.customerId());
        entity.setCompanyId(user.companyId());
        entity.setFullName(user.fullName());
        entity.setProfileType(user.profileType());
        entity.setCompanyName(user.companyName());
        entity.setCompanyType(user.companyType());
        entity.setEmployeeSubRole(user.employeeSubRole());
        entity.setPermissions(String.join(",", user.permissions()));
        entity.setActive(user.active());
        entity.setCreatedAt(user.createdAt());
        return entity;
    }

    private static List<String> toPermissions(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
