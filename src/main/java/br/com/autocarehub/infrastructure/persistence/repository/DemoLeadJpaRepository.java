package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.infrastructure.persistence.entity.DemoLeadJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoLeadJpaRepository extends JpaRepository<DemoLeadJpaEntity, UUID> {}
