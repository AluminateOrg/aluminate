package com.aluminate.aluminate_organization_backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // This will act as order_id for PayHere

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    private String paymentId;         // From PayHere
    private String method;            // e.g. VISA, EZCash
    private String statusCode;        // e.g. 2 for success
    private String statusMessage;     // Human-readable message from PayHere
    private String cardHolderName;
    private String cardNo;
    private String cardExpiry;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
    private PaymentCategory category;



    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Who made the transaction
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

    @ManyToOne
    @JoinColumn(
            name = "member_id",
            foreignKey = @ForeignKey(
                    name = "fk_transaction_member",
                    foreignKeyDefinition = "FOREIGN KEY (member_id) REFERENCES member(id) ON UPDATE CASCADE ON DELETE SET NULL"
            )
    )
    private Member member;

    @OneToOne
    @JoinColumn(
            name="donation_id",
            foreignKey = @ForeignKey(
                    name = "fk_transaction_donation",
                    foreignKeyDefinition = "FOREIGN KEY (donation_id) REFERENCES donation(id) ON UPDATE CASCADE ON DELETE SET NULL"
            )
    )
    private Donation donation;

    public boolean isPresent() {
        return this.id != null;
    }
}

