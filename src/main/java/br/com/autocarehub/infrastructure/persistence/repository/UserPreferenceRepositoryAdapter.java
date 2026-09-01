package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.application.port.out.UserPreferenceRepository;
import br.com.autocarehub.infrastructure.persistence.entity.UserPreferenceId;
import br.com.autocarehub.infrastructure.persistence.entity.UserPreferenceJpaEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserPreferenceRepositoryAdapter implements UserPreferenceRepository {

    private final UserPreferenceJpaRepository userPreferenceJpaRepository;

    public UserPreferenceRepositoryAdapter(UserPreferenceJpaRepository userPreferenceJpaRepository) {
        this.userPreferenceJpaRepository = userPreferenceJpaRepository;
    }

    @Override
    public Optional<String> findValue(UUID userId, String key) {
        return userPreferenceJpaRepository
                .findById(new UserPreferenceId(userId, key))
                .map(UserPreferenceJpaEntity::getValueJson);
    }

    @Override
    public String saveValue(UUID userId, String key, String valueJson) {
        UserPreferenceJpaEntity entity = userPreferenceJpaRepository
                .findById(new UserPreferenceId(userId, key))
                .orElseGet(UserPreferenceJpaEntity::new);
        entity.setUserId(userId);
        entity.setPrefKey(key);
        entity.setValueJson(valueJson);
        entity.setUpdatedAt(LocalDateTime.now());
        return userPreferenceJpaRepository.save(entity).getValueJson();
    }
}
