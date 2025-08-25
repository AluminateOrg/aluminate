//package com.aluminate.aluminate_organization_backend.controller;
//
//import com.aluminate.aluminate_organization_backend.dto.payment.PayHereNotifyRequest;
//import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentRequest;
//import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentResponse;
//import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
//import com.aluminate.aluminate_organization_backend.service.payment.PayHereService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.springframework.http.HttpStatus.*;
//
///**
// * Enhanced PayHere Payment Controller for donation processing
// * Handles payment initialization, notifications, and status checks
// */
//@Slf4j
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("${api.prefix}/user/payment/payhere")
//@CrossOrigin(origins = "*")
//public class PayHereController {
//
//    private final PayHereService payHereService;
//
//    /**
//     * Initialize donation payment with PayHere
//     */
//    @PostMapping("/initialize")
//    public ResponseEntity<ApiResponse> initializePayment(@RequestBody PayHerePaymentRequest request) {
//        try {
//            log.info("Initializing PayHere payment for campaign: {} by member: {}",
//                    request.getCampaignId(), request.getMemberId());
//
//            PayHerePaymentResponse response = payHereService.initializePayment(request);
//
//            log.info("Payment initialized successfully with order ID: {}", response.getOrderId());
//
//            return ResponseEntity.ok(new ApiResponse("Payment initialized successfully", response));
//
//        } catch (IllegalArgumentException e) {
//            log.warn("Payment initialization failed due to validation: {}", e.getMessage());
//            return ResponseEntity.status(BAD_REQUEST)
//                    .body(new ApiResponse(e.getMessage(), null));
//        } catch (Exception e) {
//            log.error("Unexpected error during payment initialization: ", e);
//            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Failed to initialize payment: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Handle PayHere notification callback
//     */
//    @PostMapping("/notify")
//    public ResponseEntity<String> handleNotification(@RequestParam Map<String, String> params) {
//        try {
//            log.info("PayHere notification received: {}", params);
//
//            // Convert request params to DTO
//            PayHereNotifyRequest notification = PayHereNotifyRequest.builder()
//                    .merchant_id(params.get("merchant_id"))
//                    .order_id(params.get("order_id"))
//                    .payhere_amount(params.get("payhere_amount"))
//                    .payhere_currency(params.get("payhere_currency"))
//                    .status_code(params.get("status_code"))
//                    .md5sig(params.get("md5sig"))
//                    .custom_1(params.get("custom_1"))
//                    .custom_2(params.get("custom_2"))
//                    .method(params.get("method"))
//                    .status_message(params.get("status_message"))
//                    .card_holder_name(params.get("card_holder_name"))
//                    .card_no(params.get("card_no"))
//                    .card_expiry(params.get("card_expiry"))
//                    .build();
//
//            boolean processed = payHereService.processPaymentNotification(notification);
//
//            if (processed) {
//                log.info("Payment notification processed successfully for order: {}",
//                        notification.getOrder_id());
//                return ResponseEntity.ok("OK");
//            } else {
//                log.error("Failed to process payment notification for order: {}",
//                        notification.getOrder_id());
//                return ResponseEntity.status(BAD_REQUEST).body("FAILED");
//            }
//
//        } catch (Exception e) {
//            log.error("Error processing PayHere notification: ", e);
//            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("ERROR");
//        }
//    }
//
//    /**
//     * Check payment status for success page
//     */
//    @GetMapping("/success/{orderId}")
//    public ResponseEntity<ApiResponse> checkPaymentSuccess(@PathVariable String orderId) {
//        try {
//            log.info("Checking payment success status for order: {}", orderId);
//
//            Map<String, Object> result = payHereService.getPaymentStatus(orderId);
//
//            return ResponseEntity.ok(new ApiResponse("Payment status retrieved successfully", result));
//
//        } catch (Exception e) {
//            log.error("Error checking payment success status for order: {}", orderId, e);
//            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Failed to check payment status", null));
//        }
//    }
//
//    /**
//     * Handle payment cancellation
//     */
//    @GetMapping("/cancel/{orderId}")
//    public ResponseEntity<ApiResponse> handlePaymentCancel(@PathVariable String orderId) {
//        try {
//            log.info("Handling payment cancellation for order: {}", orderId);
//
//            boolean cancelled = payHereService.cancelPayment(orderId);
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("orderId", orderId);
//            result.put("cancelled", cancelled);
//            result.put("message", "Payment was cancelled");
//
//            return ResponseEntity.ok(new ApiResponse("Payment cancellation processed", result));
//
//        } catch (Exception e) {
//            log.error("Error handling payment cancellation for order: {}", orderId, e);
//            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Failed to handle payment cancellation", null));
//        }
//    }
//
//    /**
//     * Get payment details by order ID
//     */
//    @GetMapping("/details/{orderId}")
//    public ResponseEntity<ApiResponse> getPaymentDetails(@PathVariable String orderId) {
//        try {
//            log.info("Getting payment details for order: {}", orderId);
//
//            Map<String, Object> details = payHereService.getPaymentDetails(orderId);
//
//            return ResponseEntity.ok(new ApiResponse("Payment details retrieved successfully", details));
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(NOT_FOUND)
//                    .body(new ApiResponse(e.getMessage(), null));
//        } catch (Exception e) {
//            log.error("Error getting payment details for order: {}", orderId, e);
//            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Failed to get payment details", null));
//        }
//    }
//
//    /**
//     * Health check endpoint for PayHere integration
//     */
//    @GetMapping("/health")
//    public ResponseEntity<ApiResponse> healthCheck() {
//        Map<String, Object> health = new HashMap<>();
//        health.put("service", "PayHere Integration");
//        health.put("status", "UP");
//        health.put("timestamp", System.currentTimeMillis());
//
//        return ResponseEntity.ok(new ApiResponse("PayHere service is healthy", health));
//    }
//}