package com.aluminate.aluminate_organization_backend.controller.payment;

import com.aluminate.aluminate_organization_backend.dto.payment.PaymentNotifyRequest;
import com.aluminate.aluminate_organization_backend.service.payment.PaymentNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/public/payment/notify")
public class PaymentNotificationHandler {

    private final PaymentNotifyService paymentNotifyService;
    private final Logger logger = LoggerFactory.getLogger(PaymentNotificationHandler.class);

    public PaymentNotificationHandler(PaymentNotifyService paymentNotifyService) {
        this.paymentNotifyService = paymentNotifyService;
    }
    @PostMapping(value = "/DONATION", consumes = "application/x-www-form-urlencoded")
    public void notifyPayment(@RequestBody PaymentNotifyRequest request) {
        logger.info("Reached PaymentNotificationHandler DONATION method");
        try {
            paymentNotifyService.handlePaymentNotification(request);
            logger.info("Payment notification handled successfully");

        } catch (Exception e) {
            // Log the error and return a 500 response
            logger.error("Error processing payment notification", e);
            throw new RuntimeException("Error processing payment notification", e);

        }
    }
//    Manually verifies the payment and triggers the creation of the Donation record.
//    This endpoint is called by the frontend immediately after a successful PayHere payment
    @GetMapping(value = "/verify/{orderId}")
    public ResponseEntity<String> verifyPayment(@PathVariable String orderId) {
        boolean success = paymentNotifyService.verifyOrder(orderId);
        if (success) return ResponseEntity.ok("Saved");
        return ResponseEntity.badRequest().body("Failed");
    }
}
