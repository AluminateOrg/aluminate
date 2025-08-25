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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_option_id")
    private PaymentOption paymentOption;

    @Column(nullable = false)
    private boolean isAnonymous;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String message;

    @Column
    private String paymentMethod;

    @Column(length = 100)
    private String paymentReference;

    @Column
    private String paymentToken;

    // Use String instead of enum to match database schema
    @Column
    private String status;

    @Column
    private String paymentStatus;

    @Column
    private String transactionId;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDate donationDate;

    @Column
    private String paymentHash;

    @Column
    private String paymentOrderId;

    // Helper methods for status validation
    public void setStatus(String status) {
        if (status != null && isValidDonationStatus(status)) {
            this.status = status.toUpperCase();
        } else {
            throw new IllegalArgumentException("Invalid donation status: " + status);
        }
    }

    public void setPaymentStatus(String paymentStatus) {
        if (paymentStatus != null && isValidPaymentStatus(paymentStatus)) {
            this.paymentStatus = paymentStatus.toUpperCase();
        } else {
            throw new IllegalArgumentException("Invalid payment status: " + paymentStatus);
        }
    }

    private boolean isValidDonationStatus(String status) {
        return status != null &&
                ("PENDING".equalsIgnoreCase(status) ||
                        "COMPLETED".equalsIgnoreCase(status) ||
                        "FAILED".equalsIgnoreCase(status) ||
                        "REFUNDED".equalsIgnoreCase(status) ||
                        "CANCELLED".equalsIgnoreCase(status));
    }

    private boolean isValidPaymentStatus(String status) {
        return status != null &&
                ("PENDING".equalsIgnoreCase(status) ||
                        "PROCESSING".equalsIgnoreCase(status) ||
                        "COMPLETED".equalsIgnoreCase(status) ||
                        "FAILED".equalsIgnoreCase(status) ||
                        "REFUNDED".equalsIgnoreCase(status) ||
                        "CANCELLED".equalsIgnoreCase(status));
    }

    // Constants for status values
    public static class DonationStatus {
        public static final String PENDING = "PENDING";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
        public static final String REFUNDED = "REFUNDED";
        public static final String CANCELLED = "CANCELLED";
    }

    public static class PaymentStatus {
        public static final String PENDING = "PENDING";
        public static final String PROCESSING = "PROCESSING";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
        public static final String REFUNDED = "REFUNDED";
        public static final String CANCELLED = "CANCELLED";
    }
}