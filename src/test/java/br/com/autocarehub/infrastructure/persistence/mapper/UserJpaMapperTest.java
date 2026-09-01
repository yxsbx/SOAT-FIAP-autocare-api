package br.com.autocarehub.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.User;
import br.com.autocarehub.infrastructure.persistence.entity.UserJpaEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserJpaMapperTest {

    private static UserJpaEntity entity(UUID id, UUID customerId, LocalDateTime createdAt) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(id);
        entity.setUsername("admin@autocarehub.com");
        entity.setPasswordHash("hash");
        entity.setRole(UserRole.ADMIN.name());
        entity.setCustomerId(customerId);
        entity.setFullName("Admin");
        entity.setProfileType("admin");
        entity.setCompanyName("AutoCare");
        entity.setCompanyType("Oficina");
        entity.setEmployeeSubRole("Gestor");
        entity.setPermissions(String.join(",", List.of("users:read")));
        entity.setActive(true);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    @Test
    void shouldMapEntityPermissionsAndDomainBackToEntity() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        UserJpaEntity entity = entity(id, customerId, createdAt);
        entity.setPermissions("users:read, , orders:write ");

        User user = UserJpaMapper.toDomain(entity);
        UserJpaEntity mapped = UserJpaMapper.toEntity(user);

        assertThat(user.id()).isEqualTo(id);
        assertThat(user.customerId()).isEqualTo(customerId);
        assertThat(user.permissions()).containsExactly("users:read", "orders:write");
        assertThat(mapped.getPermissions()).isEqualTo("users:read,orders:write");
    }

    @Test
    void shouldMapMissingPermissionsToEmptyList() {
        UserJpaEntity nullPermissions = entity(UUID.randomUUID(), null, LocalDateTime.now());
        nullPermissions.setPermissions(null);
        UserJpaEntity blankPermissions = entity(UUID.randomUUID(), null, LocalDateTime.now());
        blankPermissions.setPermissions("   ");

        assertThat(UserJpaMapper.toDomain(nullPermissions).permissions()).isEmpty();
        assertThat(UserJpaMapper.toDomain(blankPermissions).permissions()).isEmpty();
    }
}
