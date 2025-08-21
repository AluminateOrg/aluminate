package com.aluminate.aluminate_organization_backend.service.payment;

import com.aluminate.aluminate_organization_backend.dto.payment.PayHereNotifyRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PayHerePaymentResponse;
import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
@RequiredArgsConstructor
public class PayHereService {

    private static final Logger logger = LoggerFactory.getLogger(PayHereService.class);

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final MemberRepository memberRepository;

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @Value("${payhere.merchant.secret}")
    private String merchantSecret;

    @Value("${payhere.sandbox:true}")
    private boolean sandbox;

    @Transactional
    public PayHerePaymentResponse initializePayment(PayHerePaymentRequest request) {
        // Validate campaign exists and is active
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .filter(c -> !c.isDeleted() && c.isActive())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found or inactive"));

        // Validate member exists
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
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
                .status("PENDING")
                .paymentStatus("PENDING")
                .paymentMethod("PAYHERE")
                .build();

        Donation savedDonation = donationRepository.save(donation);

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

        return PayHerePaymentResponse.builder()
                .orderId(orderId)
                .hash(hash)
                .merchantId(merchantId)
                .amount(formattedAmount)
                .currency("LKR")
                .itemDescription("Donation to " + campaign.getTitle())
                .sandbox(sandbox)
                .build();
    }

    @Transactional
    public boolean processPaymentNotification(PayHereNotifyRequest notification) {
        try {
            logger.info("Processing PayHere notification for order: {}", notification.getOrder_id());

            // Validate the notification signature
            if (!validateNotificationSignature(notification)) {
                logger.error("Invalid notification signature for order: {}", notification.getOrder_id());
                return false;
            }

            // Find the donation by order ID
            Donation donation = donationRepository.findByPaymentOrderId(notification.getOrder_id())
                    .orElseThrow(() -> new IllegalArgumentException("Donation not found for order: " + notification.getOrder_id()));

            // Update donation status based on PayHere response
            if ("2".equals(notification.getStatus_code())) { // Success
                donation.setStatus("COMPLETED");
                donation.setPaymentStatus("COMPLETED");
                donation.setTransactionId(notification.getOrder_id());
                donation.setUpdatedAt(LocalDateTime.now());

                // Update campaign statistics
                Campaign campaign = donation.getCampaign();
                campaign.setRaised(campaign.getRaised().add(donation.getAmount()));
                campaign.setDonorCount(campaign.getDonorCount() + 1);
                campaignRepository.save(campaign);

                logger.info("Payment completed successfully for order: {}", notification.getOrder_id());
            } else {
                donation.setStatus("FAILED");
                donation.setPaymentStatus("FAILED");
                donation.setUpdatedAt(LocalDateTime.now());
                logger.warn("Payment failed for order: {} with status: {}", notification.getOrder_id(), notification.getStatus_message());
            }

            donationRepository.save(donation);
            return true;

        } catch (Exception e) {
            logger.error("Error processing PayHere notification", e);
            return false;
        }
    }

    private String generatePaymentHash(String orderId, BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(amount);
        String currency = "LKR";

        String hashString = merchantId + orderId + formattedAmount + currency + getMd5Hash(merchantSecret);
        return getMd5Hash(hashString);
    }

    private boolean validateNotificationSignature(PayHereNotifyRequest notification) {
        String merchantSecretHash = getMd5Hash(merchantSecret);
        String expectedSignature = getMd5Hash(
                notification.getMerchant_id() +
                        notification.getOrder_id() +
                        notification.getPayhere_amount() +
                        notification.getPayhere_currency() +
                        notification.getStatus_code() +
                        merchantSecretHash
        );

        return expectedSignature.equalsIgnoreCase(notification.getMd5sig());
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