package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
