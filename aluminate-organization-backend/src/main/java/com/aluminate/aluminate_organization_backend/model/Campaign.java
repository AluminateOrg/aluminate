package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})
@Table(name = "campaign")
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Campaign title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Campaign type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "type")
    private CampaignType type;

    @NotNull(message = "Goal amount is required")
    @DecimalMin(value = "100.0", message = "Goal must be at least LKR 100")
    @DecimalMax(value = "10000000.0", message = "Goal cannot exceed LKR 10,000,000")
    @Column(name = "target", precision = 15, scale = 2)
    private BigDecimal goal;

    @Builder.Default
    @Column(name = "current_amount", precision = 15, scale = 2)
    private BigDecimal raised = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "start_date")
    private LocalDate startDate = LocalDate.now();

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Min(value = 0, message = "Donor count cannot be negative")
    @Column(name = "total_donors")
    private int donorCount = 0;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Donation> donations = new HashSet<>();

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Utility methods
    public void addDonation(Donation donation) {
        donations.add(donation);
        donation.setCampaign(this);
    }

    public double getProgressPercentage() {
        if (goal == null || goal.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return raised.divide(goal, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public boolean canAcceptDonations() {
        return isActive && !isDeleted && !isExpired();
    }

    public BigDecimal getRemainingAmount() {
        if (goal == null || raised == null) return BigDecimal.ZERO;
        BigDecimal remaining = goal.subtract(raised);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }

    public boolean isGoalAchieved() {
        if (goal == null || raised == null) return false;
        return raised.compareTo(goal) >= 0;
    }

    public long getDaysRemaining() {
        if (endDate == null) return 0;
        LocalDate now = LocalDate.now();
        if (endDate.isBefore(now)) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(now, endDate);
    }
}