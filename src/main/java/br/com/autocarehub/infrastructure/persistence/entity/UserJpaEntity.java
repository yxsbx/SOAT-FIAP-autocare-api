package br.com.autocarehub.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "profile_type", nullable = false, length = 60)
    private String profileType;

    @Column(name = "company_name", nullable = false, length = 160)
    private String companyName;

    @Column(name = "company_type", nullable = false, length = 60)
    private String companyType;

    @Column(name = "employee_sub_role", nullable = false, length = 60)
    private String employeeSubRole;

    @Column(name = "permissions", nullable = false)
    private String permissions;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
