package com.aluminate.aluminate_organization_backend.service.payment;

import com.aluminate.aluminate_organization_backend.dto.payment.PayHereNotifyRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentResponse;
import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayHereService {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final MemberRepository memberRepository;

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @Value("${payhere.merchant.secret}")
    private String merchantSecret;

    @Value("${payhere.sandbox:true}")
    private boolean sandbox;

    @Value("${payhere.return.url}")
    private String returnUrl;

    @Value("${payhere.cancel.url}")
    private String cancelUrl;

    @Value("${payhere.notify.url}")
    private String notifyUrl;

    @Value("${payhere.currency:LKR}")
    private String currency;

    @Value("${donation.min.amount:10.00}")
    private BigDecimal minDonationAmount;

    @Value("${donation.max.amount:1000000.00}")
    private BigDecimal maxDonationAmount;

    /**
     * Initialize payment with PayHere
     */
    @Transactional
    public PayHerePaymentResponse initializePayment(PayHerePaymentRequest request) {
        log.info("Initializing payment for campaign: {} member: {} amount: {}",
                request.getCampaignId(), request.getMemberId(), request.getAmount());

        // Validate request
        validatePaymentRequest(request);

        // Validate campaign exists and is active
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .filter(c -> !c.isDeleted() && c.isActive() && !c.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found, inactive, or expired"));

        // Validate member exists
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Create pending donation record
        Donation donation = createPendingDonation(request, campaign, member);
        Donation savedDonation = donationRepository.save(donation);

        // Generate unique order ID
        String orderId = generateOrderId(savedDonation.getId());

        // Update donation with order ID
        savedDonation.setPaymentOrderId(orderId);
        donationRepository.save(savedDonation);

        // Generate payment hash
        String hash = generatePaymentHash(orderId, request.getAmount());

        // Format amount
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(request.getAmount());

        log.info("Payment initialized successfully with order ID: {}", orderId);

        return PayHerePaymentResponse.builder()
                .orderId(orderId)
                .hash(hash)
                .merchantId(merchantId)
                .amount(formattedAmount)
                .currency(currency)
                .itemDescription("Donation to " + campaign.getTitle())
                .sandbox(sandbox)
                .build();
    }

    /**
     * Process PayHere payment notification
     */
    @Transactional
    public boolean processPaymentNotification(PayHereNotifyRequest notification) {
        try {
            log.info("Processing PayHere notification for order: {} status: {}",
                    notification.getOrder_id(), notification.getStatus_code());

            // Validate the notification signature
            if (!validateNotificationSignature(notification)) {
                log.error("Invalid notification signature for order: {}", notification.getOrder_id());
                return false;
            }

            // Find the donation by order ID
            Optional<Donation> donationOpt = donationRepository.findByPaymentOrderId(notification.getOrder_id());
            if (donationOpt.isEmpty()) {
                log.error("Donation not found for order ID: {}", notification.getOrder_id());
                return false;
            }

            Donation donation = donationOpt.get();

            // Process based on status code
            return processPaymentStatus(donation, notification);

        } catch (Exception e) {
            log.error("Error processing PayHere notification for order: {}",
                    notification.getOrder_id(), e);
            return false;
        }
    }

    /**
     * Get payment status
     */
    public Map<String, Object> getPaymentStatus(String orderId) {
        Optional<Donation> donationOpt = donationRepository.findByPaymentOrderId(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);

        if (donationOpt.isPresent()) {
            Donation donation = donationOpt.get();
            result.put("success", "COMPLETED".equals(donation.getStatus()));
            result.put("status", donation.getStatus());
            result.put("paymentStatus", donation.getPaymentStatus());
            result.put("amount", donation.getAmount());
            result.put("campaignTitle", donation.getCampaign().getTitle());
            result.put("memberName", donation.isAnonymous() ? "Anonymous" : donation.getMember().getName());
            result.put("transactionId", donation.getTransactionId());
            result.put("createdAt", donation.getCreatedAt());
        } else {
            result.put("success", false);
            result.put("message", "Payment not found");
        }

        return result;
    }

    /**
     * Cancel payment
     */
    @Transactional
    public boolean cancelPayment(String orderId) {
        Optional<Donation> donationOpt = donationRepository.findByPaymentOrderId(orderId);

        if (donationOpt.isPresent()) {
            Donation donation = donationOpt.get();

            // Only cancel if it's still pending
            if ("PENDING".equals(donation.getStatus())) {
                donation.setStatus("CANCELLED");
                donation.setPaymentStatus("CANCELLED");
                donation.setUpdatedAt(LocalDateTime.now());
                donationRepository.save(donation);

                log.info("Payment cancelled successfully for order: {}", orderId);
                return true;
            }
        }

        return false;
    }

    /**
     * Get payment details
     */
    public Map<String, Object> getPaymentDetails(String orderId) {
        Donation donation = donationRepository.findByPaymentOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));

        Map<String, Object> details = new HashMap<>();
        details.put("orderId", orderId);
        details.put("donationId", donation.getId());
        details.put("amount", donation.getAmount());
        details.put("status", donation.getStatus());
        details.put("paymentStatus", donation.getPaymentStatus());
        details.put("campaignId", donation.getCampaign().getId());
        details.put("campaignTitle", donation.getCampaign().getTitle());
        details.put("memberId", donation.getMember().getId());
        details.put("memberName", donation.isAnonymous() ? "Anonymous" : donation.getMember().getName());
        details.put("isAnonymous", donation.isAnonymous());
        details.put("message", donation.getMessage());
        details.put("paymentMethod", donation.getPaymentMethod());
        details.put("transactionId", donation.getTransactionId());
        details.put("createdAt", donation.getCreatedAt());
        details.put("updatedAt", donation.getUpdatedAt());

        return details;
    }

    // Private helper methods

    private void validatePaymentRequest(PayHerePaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (request.getAmount().compareTo(minDonationAmount) < 0) {
            throw new IllegalArgumentException("Amount must be at least LKR " + minDonationAmount);
        }

        if (request.getAmount().compareTo(maxDonationAmount) > 0) {
            throw new IllegalArgumentException("Amount cannot exceed LKR " + maxDonationAmount);
        }

        if (request.getCampaignId() == null || request.getCampaignId() <= 0) {
            throw new IllegalArgumentException("Valid campaign ID is required");
        }

        if (request.getMemberId() == null || request.getMemberId() <= 0) {
            throw new IllegalArgumentException("Valid member ID is required");
        }

        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
    }

    private Donation createPendingDonation(PayHerePaymentRequest request, Campaign campaign, Member member) {
        return Donation.builder()
                .campaign(campaign)
                .member(member)
                .amount(request.getAmount())
                .date(LocalDate.now())
                .donationDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .isAnonymous(request.isAnonymous())
                .message(request.getMessage())
                .status("PENDING")
                .paymentStatus("PENDING")
                .paymentMethod("PAYHERE")
                .build();
    }

    private String generateOrderId(Long donationId) {
        return String.format("DON_%d_%d", donationId, System.currentTimeMillis());
    }

    private String generatePaymentHash(String orderId, BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(amount);

        String hashString = merchantId + orderId + formattedAmount + currency + getMd5Hash(merchantSecret);
        return getMd5Hash(hashString);
    }

    private boolean validateNotificationSignature(PayHereNotifyRequest notification) {
        try {
            String merchantSecretHash = getMd5Hash(merchantSecret);
            String expectedSignature = getMd5Hash(
                    notification.getMerchant_id() +
                            notification.getOrder_id() +
                            notification.getPayhere_amount() +
                            notification.getPayhere_currency() +
                            notification.getStatus_code() +
                            merchantSecretHash
            );

            boolean isValid = expectedSignature.equalsIgnoreCase(notification.getMd5sig());

            if (!isValid) {
                log.warn("Signature validation failed. Expected: {}, Received: {}",
                        expectedSignature, notification.getMd5sig());
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error validating signature for order: {}", notification.getOrder_id(), e);
            return false;
        }
    }

    private boolean processPaymentStatus(Donation donation, PayHereNotifyRequest notification) {
        String statusCode = notification.getStatus_code();

        switch (statusCode) {
            case "2": // Success
                return processSuccessfulPayment(donation, notification);
            case "0": // Pending
                return processPendingPayment(donation, notification);
            case "-1": // Cancelled
                return processCancelledPayment(donation, notification);
            case "-2": // Failed
                return processFailedPayment(donation, notification);
            case "-3": // Charged back
                return processChargedBackPayment(donation, notification);
            default:
                log.warn("Unknown status code: {} for order: {}", statusCode, notification.getOrder_id());
                return false;
        }
    }

    private boolean processSuccessfulPayment(Donation donation, PayHereNotifyRequest notification) {
        try {
            donation.setStatus("COMPLETED");
            donation.setPaymentStatus("COMPLETED");
            donation.setTransactionId(notification.getOrder_id());
            donation.setUpdatedAt(LocalDateTime.now());

            // Update campaign statistics
            Campaign campaign = donation.getCampaign();
            campaign.setRaised(campaign.getRaised().add(donation.getAmount()));
            campaign.setDonorCount(campaign.getDonorCount() + 1);
            campaignRepository.save(campaign);

            donationRepository.save(donation);

            log.info("Payment completed successfully for order: {} amount: LKR {}",
                    notification.getOrder_id(), donation.getAmount());

            return true;

        } catch (Exception e) {
            log.error("Error processing successful payment for order: {}",
                    notification.getOrder_id(), e);
            return false;
        }
    }

    private boolean processPendingPayment(Donation donation, PayHereNotifyRequest notification) {
        donation.setPaymentStatus("PROCESSING");
        donation.setUpdatedAt(LocalDateTime.now());
        donationRepository.save(donation);

        log.info("Payment is processing for order: {}", notification.getOrder_id());
        return true;
    }

    private boolean processCancelledPayment(Donation donation, PayHereNotifyRequest notification) {
        donation.setStatus("CANCELLED");
        donation.setPaymentStatus("CANCELLED");
        donation.setUpdatedAt(LocalDateTime.now());
        donationRepository.save(donation);

        log.info("Payment cancelled for order: {}", notification.getOrder_id());
        return true;
    }

    private boolean processFailedPayment(Donation donation, PayHereNotifyRequest notification) {
        donation.setStatus("FAILED");
        donation.setPaymentStatus("FAILED");
        donation.setUpdatedAt(LocalDateTime.now());
        donationRepository.save(donation);

        log.warn("Payment failed for order: {} message: {}",
                notification.getOrder_id(), notification.getStatus_message());
        return true;
    }

    private boolean processChargedBackPayment(Donation donation, PayHereNotifyRequest notification) {
        donation.setStatus("REFUNDED");
        donation.setPaymentStatus("REFUNDED");
        donation.setUpdatedAt(LocalDateTime.now());

        // Reverse campaign statistics if it was previously completed
        if ("COMPLETED".equals(donation.getStatus())) {
            Campaign campaign = donation.getCampaign();
            campaign.setRaised(campaign.getRaised().subtract(donation.getAmount()));
            campaign.setDonorCount(Math.max(0, campaign.getDonorCount() - 1));
            campaignRepository.save(campaign);
        }

        donationRepository.save(donation);

        log.warn("Payment charged back for order: {}", notification.getOrder_id());
        return true;
    }

    private String getMd5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}