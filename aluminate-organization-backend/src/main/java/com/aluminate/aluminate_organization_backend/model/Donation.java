package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "donation_date")
    @Builder.Default
    private LocalDate date = LocalDate.now();

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "payment_order_id")
    private String paymentOrderId; // PayHere order ID

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DonationStatus status = DonationStatus.PENDING;

    @Builder.Default
    private boolean isAnonymous = false;

    private String paymentMethod; // "PAYHERE", "BANK_TRANSFER", etc.

    private String transactionId; // Payment gateway transaction ID

    private String paymentHash; // PayHere payment hash for verification
}