package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.infrastructure.persistence.entity.WorkshopServiceJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkshopServiceJpaRepository extends JpaRepository<WorkshopServiceJpaEntity, UUID> {}
