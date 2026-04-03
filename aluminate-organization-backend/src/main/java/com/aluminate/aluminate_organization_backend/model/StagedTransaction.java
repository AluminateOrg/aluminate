package com.aluminate.aluminate_organization_backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class StagedTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentCategory category;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(
            name = "incoming_transaction_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_paymentdetails_incomingtransaction",
                    foreignKeyDefinition = "FOREIGN KEY (incoming_transaction_id) REFERENCES incoming_transaction(id) ON UPDATE CASCADE ON DELETE CASCADE"
            )
    )
    private IncomingTransaction incomingTransaction;

    //points to the GlobalTransactionTicket
    @ManyToOne
    @JoinColumn(
            name = "global_transaction_ticket_id",
            nullable = true,
            foreignKey = @ForeignKey(
                    name = "fk_stagedtransaction_globaltransactionticket",
                    foreignKeyDefinition = "FOREIGN KEY (global_transaction_ticket_id) REFERENCES global_transaction_ticket(id) ON UPDATE CASCADE ON DELETE SET NULL"
            )
    )
    private GlobalTransactionTicket globalTransactionTicket;



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

    // key to identify the staged transaction
    @Column(nullable = true)
    private String key = null;
}
