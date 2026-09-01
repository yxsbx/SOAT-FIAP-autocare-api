package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.infrastructure.persistence.entity.ServiceOrderJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ServiceOrderJpaRepository
        extends JpaRepository<ServiceOrderJpaEntity, UUID>, JpaSpecificationExecutor<ServiceOrderJpaEntity> {

    @Override
    @EntityGraph(attributePaths = {"services", "parts"})
    @NonNull
    List<ServiceOrderJpaEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"services", "parts"})
    @NonNull
    Optional<ServiceOrderJpaEntity> findById(@NonNull UUID id);

    @EntityGraph(attributePaths = {"services", "parts"})
    List<ServiceOrderJpaEntity> findByCustomerId(UUID customerId);

    @EntityGraph(attributePaths = {"services", "parts"})
    @Query(
            """
                    select serviceOrder
                    from ServiceOrderJpaEntity serviceOrder
                    where serviceOrder.startedAt is not null
                      and serviceOrder.finishedAt is not null
                    """)
    List<ServiceOrderJpaEntity> findCompletedWithExecutionTime();
}
