package com.aluminate.aluminate_organization_backend.service.payment;

import com.aluminate.aluminate_organization_backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


}
