package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.payment.PayHereNotifyRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentResponse;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.service.payment.PayHereService;
import com.aluminate.aluminate_organization_backend.service.donation.IDonationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/user/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PayHereService payHereService;
    private final IDonationService donationService;
    private final MemberRepository memberRepository;

    /**
     * Initialize PayHere payment for donation
     */
    @PostMapping("/payhere/initialize")
    public ResponseEntity<ApiResponse> initializePayHerePayment(@RequestBody PayHerePaymentRequest request) {
        try {
            log.info("Initializing PayHere payment for campaign: {} by member: {}",
                    request.getCampaignId(), request.getMemberId());

            // Validate member exists and is active
            Member member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + request.getMemberId()));

            if (!member.isActive()) {
                throw new IllegalStateException("Member account is not active");
            }

            // Log member details for debugging
            log.debug("Processing payment for member: {} ({})", member.getName(), member.getEmail());

            PayHerePaymentResponse response = payHereService.initializePayment(request);

            log.info("PayHere payment initialized successfully. OrderID: {}", response.getOrderId());
            return ResponseEntity.ok(new ApiResponse("Payment initialized successfully", response));

        } catch (ResourceNotFoundException e) {
            log.warn("Member or campaign not found: {}", e.getMessage());
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Payment initialization failed due to validation: {}", e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during payment initialization: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to initialize payment: " + e.getMessage(), null));
        }
    }

    /**
     * Handle PayHere payment notification (webhook)
     */
    @PostMapping("/payhere/notify")
    public ResponseEntity<String> handlePayHereNotification(
            @RequestParam("merchant_id") String merchantId,
            @RequestParam("order_id") String orderId,
            @RequestParam("payhere_amount") String amount,
            @RequestParam("payhere_currency") String currency,
            @RequestParam("status_code") String statusCode,
            @RequestParam("md5sig") String md5sig,
            @RequestParam(value = "custom_1", required = false) String custom1,
            @RequestParam(value = "custom_2", required = false) String custom2,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "status_message", required = false) String statusMessage,
            @RequestParam(value = "card_holder_name", required = false) String cardHolderName,
            @RequestParam(value = "card_no", required = false) String cardNo,
            @RequestParam(value = "card_expiry", required = false) String cardExpiry) {

        try {
            log.info("PayHere notification received - OrderID: {}, Status: {}", orderId, statusCode);

            PayHereNotifyRequest notifyRequest = new PayHereNotifyRequest();
            notifyRequest.setMerchant_id(merchantId);
            notifyRequest.setOrder_id(orderId);
            notifyRequest.setPayhere_amount(amount);
            notifyRequest.setPayhere_currency(currency);
            notifyRequest.setStatus_code(statusCode);
            notifyRequest.setMd5sig(md5sig);
            notifyRequest.setCustom_1(custom1);
            notifyRequest.setCustom_2(custom2);
            notifyRequest.setMethod(method);
            notifyRequest.setStatus_message(statusMessage);
            notifyRequest.setCard_holder_name(cardHolderName);
            notifyRequest.setCard_no(cardNo);
            notifyRequest.setCard_expiry(cardExpiry);

            boolean processed = payHereService.processPaymentNotification(notifyRequest);

            if (processed) {
                log.info("PayHere notification processed successfully for order: {}", orderId);
                return ResponseEntity.ok("OK");
            } else {
                log.error("Failed to process PayHere notification for order: {}", orderId);
                return ResponseEntity.status(BAD_REQUEST).body("FAIL");
            }

        } catch (Exception e) {
            log.error("Error processing PayHere notification for order: {}", orderId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }

    /**
     * Handle successful payment return
     */
    @GetMapping("/payhere/success")
    public ResponseEntity<ApiResponse> paymentSuccess(@RequestParam("order_id") String orderId) {
        try {
            log.info("Payment success callback for order: {}", orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", orderId);
            response.put("message", "Payment completed successfully");

            return ResponseEntity.ok(new ApiResponse("Payment success", response));

        } catch (Exception e) {
            log.error("Error handling payment success for order: {}", orderId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error processing success callback", null));
        }
    }

    /**
     * Handle cancelled payment return
     */
    @GetMapping("/payhere/cancel")
    public ResponseEntity<ApiResponse> paymentCancel(@RequestParam("order_id") String orderId) {
        try {
            log.info("Payment cancel callback for order: {}", orderId);

            // Update donation status to cancelled if needed
            donationService.processDonationFailure(orderId, "Payment cancelled by user");

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("orderId", orderId);
            response.put("message", "Payment was cancelled");

            return ResponseEntity.ok(new ApiResponse("Payment cancelled", response));

        } catch (Exception e) {
            log.error("Error handling payment cancellation for order: {}", orderId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error processing cancel callback", null));
        }
    }

    /**
     * Get payment status by order ID
     */
    @GetMapping("/payhere/status/{orderId}")
    public ResponseEntity<ApiResponse> getPaymentStatus(@PathVariable String orderId) {
        try {
            log.info("Checking payment status for order: {}", orderId);

            // Get donation by order ID to check status
            var donation = donationService.getDonationByOrderId(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("status", donation.getStatus());
            response.put("paymentStatus", donation.getPaymentStatus());
            response.put("amount", donation.getAmount());

            return ResponseEntity.ok(new ApiResponse("Payment status retrieved", response));

        } catch (Exception e) {
            log.error("Error checking payment status for order: {}", orderId, e);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse("Payment not found", null));
        }
    }
}