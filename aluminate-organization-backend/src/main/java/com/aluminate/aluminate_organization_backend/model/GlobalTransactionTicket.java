package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GlobalTransactionTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    //organization
    @OneToOne
    @JoinColumn(name = "organization_id", foreignKey = @ForeignKey(name = "fk_global_transaction_ticket_organization",
            foreignKeyDefinition = "FOREIGN KEY (organization_id) REFERENCES organization(id) ON UPDATE CASCADE ON DELETE CASCADE"))
    private Organization organization;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private Boolean staged = true;

    @Builder.Default
    private Boolean ack = false;

    @Column(unique = true, nullable = false)
    private String key;

}
