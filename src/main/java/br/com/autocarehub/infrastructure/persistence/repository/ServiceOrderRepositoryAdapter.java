package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.model.ServiceOrder;
import br.com.autocarehub.infrastructure.persistence.entity.ServiceOrderJpaEntity;
import br.com.autocarehub.infrastructure.persistence.mapper.ServiceOrderJpaMapper;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ServiceOrderRepositoryAdapter implements ServiceOrderRepository {

    private final ServiceOrderJpaRepository serviceOrderJpaRepository;

    public ServiceOrderRepositoryAdapter(ServiceOrderJpaRepository serviceOrderJpaRepository) {
        this.serviceOrderJpaRepository = serviceOrderJpaRepository;
    }

    @Override
    public ServiceOrder save(ServiceOrder serviceOrder) {
        return ServiceOrderJpaMapper.toDomain(
                serviceOrderJpaRepository.save(ServiceOrderJpaMapper.toEntity(serviceOrder)));
    }

    @Override
    public Optional<ServiceOrder> findById(UUID id) {
        return serviceOrderJpaRepository.findById(id).map(ServiceOrderJpaMapper::toDomain);
    }

    @Override
    public List<ServiceOrder> findAll() {
        return serviceOrderJpaRepository.findAll().stream()
                .map(ServiceOrderJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<ServiceOrder> findByCustomerId(UUID customerId) {
        return serviceOrderJpaRepository.findByCustomerId(customerId).stream()
                .map(ServiceOrderJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<ServiceOrder> findCompletedWithExecutionTime() {
        return serviceOrderJpaRepository.findCompletedWithExecutionTime().stream()
                .map(ServiceOrderJpaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOrder> findOperationalQueue(
            ServiceOrderStatus status,
            UUID customerId,
            UUID vehicleId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Integer page,
            Integer size) {
        Pageable pageable = size == null ? Pageable.unpaged() : PageRequest.of(page == null ? 0 : page, size);
        String statusCode = status == null ? null : status.externalCode();
        return serviceOrderJpaRepository
                .findAll(operationalQueueSpec(statusCode, customerId, vehicleId, createdFrom, createdTo), pageable)
                .getContent()
                .stream()
                .map(ServiceOrderJpaMapper::toDomain)
                .toList();
    }

    private static Specification<ServiceOrderJpaEntity> operationalQueueSpec(
            String statusCode, UUID customerId, UUID vehicleId, LocalDateTime createdFrom, LocalDateTime createdTo) {
        return (root, query, criteriaBuilder) -> {
            boolean countQuery = Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType());
            if (!countQuery) {
                var statusPriority = criteriaBuilder
                        .selectCase(root.<String>get("status"))
                        .when("IN_PROGRESS", 0)
                        .when("WAITING_APPROVAL", 1)
                        .when("IN_DIAGNOSIS", 2)
                        .when("RECEIVED", 3)
                        .otherwise(4);
                query.orderBy(criteriaBuilder.asc(statusPriority), criteriaBuilder.asc(root.get("createdAt")));
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.not(root.get("status").in("FINISHED", "DELIVERED")));
            if (statusCode != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), statusCode));
            }
            if (customerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("customerId"), customerId));
            }
            if (vehicleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleId"), vehicleId));
            }
            if (createdFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
