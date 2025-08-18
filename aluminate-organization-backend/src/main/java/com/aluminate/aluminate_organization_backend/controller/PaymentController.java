package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.payment.PaymentDTO;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/user/payment")
public class PaymentController {

    private final MemberRepository memberRepository;

    @Value("${payhere.merchantId}")
    private String merchantID;

    @Value("${payhere.secret}")
    private String merchantSecret;

    public PaymentController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @PostMapping("/create")
    public Map<String, Object> createPayment(@RequestBody PaymentDTO paymentDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Member member = memberRepository.findById(paymentDTO.getPayerId()).orElseThrow();
            response.put("merchant_id", merchantID);
            response.put("merchant_secret", merchantSecret);
            response.put("payment_date", paymentDTO.getPaymentDate());
            response.put("amount", paymentDTO.getAmount());
            response.put("currency", "LKR");
            response.put("order_id", paymentDTO.getId());
            response.put("status", paymentDTO.getStatus());
            response.put("payer_id", paymentDTO.getPayerId());
            response.put("payment_method", paymentDTO.getPaymentMethod());
            response.put("payer_name", member.getName());
            response.put("payer_email", member.getEmail());
            response.put("payer_phone", member.getPhone());
            response.put("message", "Payment created successfully");
            return response;
        } catch (Exception e) {
            response.put("message", "Error creating payment: " + e.getMessage());
            return response;
        }
    }



}
