package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.application.port.out.StockMovementRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StockMovementRepositoryAdapter implements StockMovementRepository {

    private final JdbcTemplate jdbcTemplate;

    public StockMovementRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void register(
            UUID partId, String movementType, int quantity, BigDecimal unitCost, BigDecimal unitPrice, String reason) {
        jdbcTemplate.update(
                """
                        INSERT INTO stock_movements
                            (id, part_id, movement_type, quantity, unit_cost, unit_price, reason, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID(),
                partId,
                movementType,
                quantity,
                unitCost,
                unitPrice,
                reason,
                Timestamp.valueOf(LocalDateTime.now()));
    }
}
