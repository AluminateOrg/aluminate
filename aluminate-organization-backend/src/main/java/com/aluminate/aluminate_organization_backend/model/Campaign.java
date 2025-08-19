package com.aluminate.aluminate_organization_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
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
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "type")
    private CampaignType type;

    @Column(name = "target")
    private BigDecimal goal;

    @Column(name = "current_amount")
    @Builder.Default
    private BigDecimal raised = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_donors")
    @Builder.Default
    private int donorCount = 0;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Donation> donations = new HashSet<>();

    // Utility method to add donation
    public void addDonation(Donation donation) {
        donations.add(donation);
        donation.setCampaign(this);
    }

    // Calculate progress percentage
    public double getProgressPercentage() {
        if (goal == null || goal.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return raised.divide(goal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).doubleValue();
    }

    // Check if campaign is expired
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    // Check if campaign can accept donations
    public boolean canAcceptDonations() {
        return isActive && !isDeleted && !isExpired();
    }
}