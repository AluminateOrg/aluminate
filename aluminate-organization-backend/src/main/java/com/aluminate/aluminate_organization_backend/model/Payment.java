package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private LocalDateTime payementDate;
    private String paymentMethod;
    private String status;

    @OneToOne
    @JoinColumn(name = "payer_id", referencedColumnName = "id")
    private Member payer;
}
