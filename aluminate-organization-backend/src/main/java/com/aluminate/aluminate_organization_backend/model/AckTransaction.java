package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AckTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @OneToOne
    @JoinColumn(
            name = "incoming_transaction_id", // the actual column in this table
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_acktransaction_incoming_transaction",
                    foreignKeyDefinition = "FOREIGN KEY (incoming_transaction_id) REFERENCES incoming_transaction(id) ON UPDATE CASCADE ON DELETE CASCADE"
            )
    )
    private IncomingTransaction incomingTransaction;

    @Enumerated(EnumType.STRING)
    private PaymentCategory category;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


}
