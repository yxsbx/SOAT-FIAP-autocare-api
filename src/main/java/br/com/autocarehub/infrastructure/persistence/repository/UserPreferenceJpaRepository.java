package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.infrastructure.persistence.entity.UserPreferenceId;
import br.com.autocarehub.infrastructure.persistence.entity.UserPreferenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceJpaRepository extends JpaRepository<UserPreferenceJpaEntity, UserPreferenceId> {}
