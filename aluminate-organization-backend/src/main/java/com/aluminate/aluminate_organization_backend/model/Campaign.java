package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal target;
    private String Category;
    private BigDecimal currentAmount;
    private int totalDonors;
    private boolean isActive;

    @OneToMany(mappedBy = "campaign")
    private Set<Donation> donations = new HashSet<>();

    /** Utility method to update currentAmount and totalDonors */
    public void addDonation(Donation donation) {
        donations.add(donation);
        this.currentAmount = this.currentAmount.add(donation.getAmount());
        this.totalDonors = donations.size();
    }


}
