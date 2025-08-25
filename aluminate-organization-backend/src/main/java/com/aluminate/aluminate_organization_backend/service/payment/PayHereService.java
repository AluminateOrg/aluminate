package com.aluminate.aluminate_organization_backend.service.payment;

import com.aluminate.aluminate_organization_backend.config.PayhereConfig;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHereNotifyRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PayHereService {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final MemberRepository memberRepository;
    private final PayhereConfig payhereConfig;

    // Additional configuration from application.properties
    @Value("${payhere.sandbox:true}")
    private boolean sandbox;

    @Value("${payhere.currency:LKR}")
    private String currency;

    @Value("${donation.min.amount:10.00}")
    private double minDonationAmount;

    @Value("${donation.max.amount:1000000.00}")
    private double maxDonationAmount;

    @Transactional
    public PayHerePaymentResponse initializePayment(PayHerePaymentRequest request) {
        log.info("Initializing PayHere payment for campaign: {} by member: {}",
                request.getCampaignId(), request.getMemberId());

        // Validate campaign exists and is active
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .filter(c -> !c.isDeleted() && c.isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found or inactive"));

        // Validate member exists
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        // Validate member is active
        if (!member.isActive()) {
            throw new IllegalStateException("Member account is not active");
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // Validate amount limits
        BigDecimal minAmount = BigDecimal.valueOf(minDonationAmount);
        BigDecimal maxAmount = BigDecimal.valueOf(maxDonationAmount);

        if (request.getAmount().compareTo(minAmount) < 0) {
            throw new IllegalArgumentException("Minimum donation amount is LKR " + minAmount);
        }

        if (request.getAmount().compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("Maximum donation amount is LKR " + maxAmount);
        }

        // Check if campaign is still accepting donations
        if (campaign.isExpired()) {
            throw new IllegalArgumentException("Campaign has expired and is no longer accepting donations");
        }

        // Create pending donation record
        Donation donation = Donation.builder()
                .campaign(campaign)
                .member(member)
                .amount(request.getAmount())
                .date(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .isAnonymous(request.isAnonymous())
                .message(request.getMessage())
                .status(Donation.DonationStatus.PENDING)
                .paymentStatus(Donation.PaymentStatus.PENDING)
                .paymentMethod("PAYHERE")
                .build();

        Donation savedDonation = donationRepository.save(donation);
        log.info("Created donation record with ID: {}", savedDonation.getId());

        // Generate order ID
        String orderId = "DON_" + savedDonation.getId() + "_" + System.currentTimeMillis();

        // Update donation with order ID
        savedDonation.setPaymentOrderId(orderId);
        donationRepository.save(savedDonation);

        // Generate hash
        String hash = generatePaymentHash(orderId, request.getAmount());

        // Format amount
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(request.getAmount());

        log.info("PayHere payment initialized successfully. OrderID: {}, Amount: {}",
                orderId, formattedAmount);

        return PayHerePaymentResponse.builder()
                .orderId(orderId)
                .hash(hash)
                .merchantId(payhereConfig.getMerchantId())
                .amount(formattedAmount)
                .currency(currency)
                .itemDescription("Donation to " + campaign.getTitle())
                .sandbox(sandbox)
                .build();
    }

    @Transactional
    public boolean processPaymentNotification(PayHereNotifyRequest notification) {
        try {
            log.info("Processing PayHere notification for order: {}", notification.getOrder_id());

            // Validate the notification signature
            if (!validateNotificationSignature(notification)) {
                log.error("Invalid notification signature for order: {}", notification.getOrder_id());
                return false;
            }

            // Find the donation by order ID
            Donation donation = donationRepository.findByPaymentOrderId(notification.getOrder_id())
                    .orElseThrow(() -> new ResourceNotFoundException("Donation not found for order: " + notification.getOrder_id()));

            // Check if donation is already processed
            if (Donation.DonationStatus.COMPLETED.equals(donation.getStatus()) ||
                    Donation.DonationStatus.FAILED.equals(donation.getStatus())) {
                log.info("Donation already processed for order: {}", notification.getOrder_id());
                return true;
            }

            // Update donation status based on PayHere response
            if ("2".equals(notification.getStatus_code())) { // Success
                donation.setStatus(Donation.DonationStatus.COMPLETED);
                donation.setPaymentStatus(Donation.PaymentStatus.COMPLETED);
                donation.setTransactionId(notification.getOrder_id());
                donation.setUpdatedAt(LocalDateTime.now());

                // Update campaign statistics
                Campaign campaign = donation.getCampaign();
                campaign.setRaised(campaign.getRaised().add(donation.getAmount()));
                campaign.setDonorCount(campaign.getDonorCount() + 1);
                campaignRepository.save(campaign);

                log.info("Payment completed successfully for order: {} - Amount: {}",
                        notification.getOrder_id(), donation.getAmount());
            } else {
                donation.setStatus(Donation.DonationStatus.FAILED);
                donation.setPaymentStatus(Donation.PaymentStatus.FAILED);
                donation.setUpdatedAt(LocalDateTime.now());
                log.warn("Payment failed for order: {} with status: {} - {}",
                        notification.getOrder_id(), notification.getStatus_code(), notification.getStatus_message());
            }

            donationRepository.save(donation);
            return true;

        } catch (Exception e) {
            log.error("Error processing PayHere notification for order: {}", notification.getOrder_id(), e);
            return false;
        }
    }

    private String generatePaymentHash(String orderId, BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(amount);

        String hashString = payhereConfig.getMerchantId() + orderId + formattedAmount + currency + getMd5Hash(payhereConfig.getSecret());
        String hash = getMd5Hash(hashString);

        log.debug("Generated payment hash for order: {} with amount: {}", orderId, formattedAmount);
        return hash;
    }

    private boolean validateNotificationSignature(PayHereNotifyRequest notification) {
        try {
            String merchantSecretHash = getMd5Hash(payhereConfig.getSecret());
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
                log.error("Signature validation failed for order: {}. Expected: {}, Received: {}",
                        notification.getOrder_id(), expectedSignature, notification.getMd5sig());
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating PayHere signature for order: {}", notification.getOrder_id(), e);
            return false;
        }
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