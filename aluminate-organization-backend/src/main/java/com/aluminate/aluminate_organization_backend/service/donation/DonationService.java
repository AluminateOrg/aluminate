package com.aluminate.aluminate_organization_backend.service.donation;

import com.aluminate.aluminate_organization_backend.dto.donation.CreateDonationRequest;
import com.aluminate.aluminate_organization_backend.dto.donation.DonationResponseDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.CampaignRepository;
import com.aluminate.aluminate_organization_backend.repository.DonationRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentHashResponse;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentHashRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aluminate.aluminate_organization_backend.service.notification.NotificationService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService implements IDonationService {

    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;
    private final CampaignRepository campaignRepository;
    private final NotificationService notificationService;

    @Value("${payhere.merchant.id:1224501}")
    private String merchantId;

    @Value("${payhere.merchant.secret:YOUR_MERCHANT_SECRET}")
    private String merchantSecret;

    @Override
    @Transactional
    public DonationResponseDTO createDonation(Long memberId, CreateDonationRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + request.getCampaignId()));

        // Validate campaign is active
        if (!campaign.isActive()) {
            throw new IllegalStateException("Cannot donate to inactive campaign");
        }

        // Validate donation amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Donation amount must be greater than zero");
        }

        if (request.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("Donation amount cannot exceed LKR 1,000,000");
        }

        Donation donation = Donation.builder()
                .member(member)
                .campaign(campaign)
                .amount(request.getAmount())
                .isAnonymous(request.isAnonymous())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "PAYHERE")
                .status(DonationStatus.PENDING)
                .build();

        Donation savedDonation = donationRepository.save(donation);
        return mapToDonationResponseDTO(savedDonation);
    }

    @Override
    public PaymentHashResponse generatePaymentHash(PaymentHashRequest request) {
        String orderId = "ORD_" + System.currentTimeMillis();
        String currency = "LKR";
        DecimalFormat df = new DecimalFormat("0.00");
        String amountFormatted = df.format(request.getAmount());

        // Create hash string according to PayHere documentation
        String hashString = merchantId + orderId + amountFormatted + currency + getMd5(merchantSecret);
        String hash = getMd5(hashString);

        return PaymentHashResponse.builder()
                .orderId(orderId)
                .hash(hash)
                .amount(amountFormatted)
                .merchantId(merchantId)
                .currency(currency)
                .build();
    }

    @Override
    @Transactional
    public void handlePaymentNotification(PaymentNotificationRequest notification) {
        // Verify the payment notification hash
        String localMd5sig = getMd5(
                merchantId +
                        notification.getOrder_id() +
                        notification.getPayhere_amount() +
                        notification.getPayhere_currency() +
                        notification.getStatus_code() +
                        getMd5(merchantSecret)
        );

        if (!localMd5sig.equalsIgnoreCase(notification.getMd5sig())) {
            throw new IllegalArgumentException("Invalid payment notification - hash mismatch");
        }

        Donation donation = donationRepository.findByPaymentOrderId(notification.getOrder_id())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found for order ID: " + notification.getOrder_id()));

        // Update donation based on payment status
        if ("2".equals(notification.getStatus_code())) { // Success
            donation.setStatus(DonationStatus.COMPLETED);
            donation.setTransactionId(notification.getOrder_id());
            donation.setPaymentHash(notification.getMd5sig());
            donationRepository.save(donation);

            // Update campaign statistics
            updateCampaignStatistics(donation.getCampaign().getId());

            // Create success notification
            notificationService.createDonationNotification(
                    donation.getMember(),
                    donation.getCampaign(),
                    donation
            );

        } else if ("0".equals(notification.getStatus_code())) { // Pending
            donation.setStatus(DonationStatus.PENDING);
            donationRepository.save(donation);

        } else { // Failed or Cancelled
            donation.setStatus(DonationStatus.FAILED);
            donationRepository.save(donation);

            // Create failure notification
            notificationService.createDonationFailedNotification(
                    donation.getMember(),
                    donation.getCampaign(),
                    donation
            );
        }
    }

    @Override
    @Transactional
    public void updateCampaignStatistics(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        BigDecimal totalRaised = donationRepository.getTotalRaisedByCampaign(campaign);
        int donorCount = donationRepository.getUniqueDonorCountByCampaign(campaign);

        campaign.setRaised(totalRaised != null ? totalRaised : BigDecimal.ZERO);
        campaign.setDonorCount(donorCount);

        campaignRepository.save(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        return donationRepository.findByMemberOrderByCreatedAtDesc(member)
                .stream()
                .map(this::mapToDonationResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsByCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        return donationRepository.findByCampaignOrderByCreatedAtDesc(campaign)
                .stream()
                .map(this::mapToDonationResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DonationResponseDTO getDonationById(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + donationId));

        return mapToDonationResponseDTO(donation);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDonatedByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        BigDecimal total = donationRepository.getTotalDonatedByMember(member);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Add this method to your existing DonationService class

    @Override
    @Transactional
    public DonationResponseDTO updatePaymentOrderId(Long donationId, String paymentOrderId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + donationId));

        // Validate that donation is in PENDING status
        if (donation.getStatus() != DonationStatus.PENDING) {
            throw new IllegalStateException("Can only update payment order ID for pending donations");
        }

        // Validate payment order ID is not null or empty
        if (paymentOrderId == null || paymentOrderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment order ID cannot be null or empty");
        }

        donation.setPaymentOrderId(paymentOrderId);
        Donation savedDonation = donationRepository.save(donation);

        return mapToDonationResponseDTO(savedDonation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getAllDonations() {
        return donationRepository.findAll()
                .stream()
                .map(this::mapToDonationResponseDTO)
                .collect(Collectors.toList());
    }

    private DonationResponseDTO mapToDonationResponseDTO(Donation donation) {
        return DonationResponseDTO.builder()
                .id(donation.getId())
                .memberId(donation.getMember().getId())
                .memberName(donation.isAnonymous() ? "Anonymous" : donation.getMember().getName())
                .campaignId(donation.getCampaign().getId())
                .campaignTitle(donation.getCampaign().getTitle())
                .amount(donation.getAmount())
                .date(donation.getDate())
                .createdAt(donation.getCreatedAt())
                .paymentOrderId(donation.getPaymentOrderId())
                .status(donation.getStatus())
                .isAnonymous(donation.isAnonymous())
                .paymentMethod(donation.getPaymentMethod())
                .transactionId(donation.getTransactionId())
                .build();
    }

    private static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}