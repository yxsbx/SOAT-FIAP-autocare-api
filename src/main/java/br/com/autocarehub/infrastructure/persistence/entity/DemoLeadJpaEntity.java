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
@Table(name = "demo_leads")
public class DemoLeadJpaEntity {

    @Id
    private UUID id;

    @Column(name = "contact_name", nullable = false, length = 120)
    private String contactName;

    @Column(name = "company_name", nullable = false, length = 120)
    private String companyName;

    @Column(name = "demo_profile", nullable = false, length = 40)
    private String demoProfile;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 40)
    private String cnpj;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
