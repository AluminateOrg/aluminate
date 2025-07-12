package com.aluminate.aluminate_organization_backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})

public class PaymentOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** Only store last 4 digits of card number */
    @Column(length = 4)
    private String lastFourDigits;

    private String cardHolderName;

    @Column(name = "expiry_month")
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    private Integer expiryYear;

    /** Card type ek (VISA, MASTERCARD, etc.) */
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    /** Token from payment processor */
    @Column(nullable = false)
    private String paymentToken;

    /** Is this the default payment method? */
    private boolean isDefault;

    /** When was this payment method added */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Soft delete */
    private boolean isActive;
}

