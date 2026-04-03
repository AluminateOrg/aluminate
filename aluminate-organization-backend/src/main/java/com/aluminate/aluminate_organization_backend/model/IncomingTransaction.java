package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
    private PaymentCategory category;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(
            name = "organization_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transaction_organization",
                    foreignKeyDefinition = "FOREIGN KEY (organization_id) REFERENCES organization(id) ON UPDATE CASCADE ON DELETE CASCADE"
            )
    )
    private Organization organization;

    @OneToOne
    @JoinColumn(
            name = "transaction_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_incomingTransaction_transaction",
                    foreignKeyDefinition = "FOREIGN KEY (transaction_id) REFERENCES transaction(id) ON UPDATE CASCADE ON DELETE CASCADE"
            )
    )
    private Transaction transaction;

    @Builder.Default
    private Boolean staged = false;

    @Builder.Default
    private Boolean ack = false;
}
