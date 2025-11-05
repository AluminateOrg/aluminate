package com.aluminate.aluminate_organization_backend.service.payment;

    import com.aluminate.aluminate_organization_backend.dto.payment.PaymentNotifyRequest;
    import com.aluminate.aluminate_organization_backend.model.*;
    import com.aluminate.aluminate_organization_backend.repository.*;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.math.BigDecimal;
    import java.security.MessageDigest;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.Objects;
    import java.util.Optional;

    import static com.aluminate.aluminate_organization_backend.model.PaymentCategory.DONATION;
    import static org.springframework.messaging.simp.stomp.StompHeaders.SUBSCRIPTION;


/**
     * Service for handling payment notifications from PayHere.
     */
    @Service
    public class PaymentNotifyService {
        @Value("${payhere.merchant_secret}")
        private String merchantSecret;
        @Value("${payhere.merchant_id}")
        private String merchantId;

        private final TransactionRepository transactionRepository;
        private final OrganizationRepository organizationRepository;
        private final Logger log = LoggerFactory.getLogger(PaymentNotifyService.class);
        private final MemberRepository memberRepository;
        private final CampaignRepository campaignRepository;
        private final DonationRepository donationRepository;
        private final IncomingTransactionRepository incomingTransactionRepository;

        /**
         * Constructs a PaymentNotifyService with required repositories.
         *
         * @param transactionRepository the transaction repository
         * @param organizationRepository the organization repository
         * @param memberRepository the member repository
         * @param campaignRepository the campaign repository
         * @param donationRepository the donation repository
         */
        public PaymentNotifyService(TransactionRepository transactionRepository,
                                    OrganizationRepository organizationRepository,
                                    MemberRepository memberRepository,
                                    CampaignRepository campaignRepository,
                                    DonationRepository donationRepository,
                                    IncomingTransactionRepository incomingTransactionRepository
        ) {
            this.transactionRepository = transactionRepository;
            this.organizationRepository = organizationRepository;
            this.memberRepository = memberRepository;
            this.campaignRepository = campaignRepository;
            this.donationRepository = donationRepository;
            this.incomingTransactionRepository = incomingTransactionRepository;
        }

        /**
         * Handles the payment notification from PayHere.
         * Verifies the signature and updates the transaction if successful.
         *
         * @param request the payment notification request
         */
        @Transactional
        public void handlePaymentNotification(PaymentNotifyRequest request) {
            try {
                String localSig = generateMd5Sig(
                        request.getMerchant_id(),
                        request.getOrder_id(),
                        request.getPayhere_amount(),
                        request.getPayhere_currency(),
                        request.getStatus_code()
                );

                if (localSig.equals(request.getMd5sig()) && "2".equals(request.getStatus_code())) {
                    // Payment verified and successful
                    Optional<Transaction> optionalTransaction = transactionRepository.findById(Long.valueOf(request.getOrder_id()));

                    if (optionalTransaction.isPresent()) {
                        Transaction transaction = getTransaction(request, optionalTransaction);
                        if(transaction == null) {
                            log.warn("Transaction is null for order ID: {}", request.getOrder_id());
                            return;
                        }

                        transactionRepository.save(transaction);
                        log.info("Transaction saved! Order ID: {}", request.getOrder_id());
                        // Handle domain-specific logic based on transaction category
                        handleDomainSpecificLogic(transaction, request);

                        //add notification by kafka and send email

                        // Additional processing can be done here



                    } else {
                        log.warn("Transaction not found for ID: {}", request.getOrder_id());
                    }
                } else {
                    log.warn("Payment verification FAILED for order {}", request.getOrder_id());
                }
            } catch (Exception e) {
                log.error("Error while verifying PayHere payment notification", e);
            }
        }

        private void handleDomainSpecificLogic(Transaction transaction, PaymentNotifyRequest request) {
            // Implement any domain-specific logic here if needed
            switch (transaction.getCategory()) {
                case DONATION:
                    try {
                        Optional<Member> optionalMember = memberRepository.findByEmail(request.getCustom_2());
                        if (optionalMember.isEmpty()) {
                            throw new IllegalArgumentException("Member not found for email: " + request.getCustom_2());
                        }
                        Member member = optionalMember.get();

                        //custom_1 has campaign id
                        Long campaignId;
                        try {
                            campaignId = Long.valueOf(request.getCustom_1());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid campaign ID in custom_1: " + request.getCustom_1());
                        }

                        //find campaign
                        Optional<Campaign> optionalCampaign = campaignRepository.findById(campaignId);
                        if (optionalCampaign.isEmpty()) {
                            throw new IllegalArgumentException("Campaign not found for ID: " + campaignId);

                        }
                        Campaign campaign = optionalCampaign.get();

                        Donation donation = Donation.builder()
                                .campaign(campaign)
                                .member(member)
                                .amount(new BigDecimal(request.getPayhere_amount()))
                                .date(LocalDate.now())
                                .createdAt(LocalDateTime.now())
                                .status(Donation.DonationStatus.PENDING)
                                .paymentStatus(Donation.PaymentStatus.PENDING)
                                .paymentMethod("PAYHERE")
                                .build();

                        Donation savedDonation = donationRepository.save(donation);
                        log.info("Donation saved with ID: {}", savedDonation.getId());

                        //save incoming transaction
                        IncomingTransaction incomingTransaction = IncomingTransaction.builder()
                                .amount(new BigDecimal(request.getPayhere_amount()))
                                .currency(request.getPayhere_currency())
                                .transactionStatus(TransactionStatus.SUCCESS)
                                .category(DONATION)
                                .organization(transaction.getOrganization())
                                .transaction(transaction)
                                .staged(false)
                                .ack(false)
                                .build();
                        incomingTransactionRepository.save(incomingTransaction);



                    }
                    catch (Exception e) {
                        log.error("Error while processing donation for transaction ID: {}", transaction.getId(), e);
                    }

                    break;
                case MENTORSHIP:
                    // Handle Mentorship-specific logic
                    break;
                default:
                    log.warn("Unknown transaction category: {}", transaction.getCategory());

            }

        }

        /**
         * Updates the transaction entity with payment notification details.
         *
         * @param request the payment notification request
         * @param optionalTransaction the transaction to update
         * @return the updated transaction, or null if an error occurs
         */
        private Transaction getTransaction(PaymentNotifyRequest request, Optional<Transaction> optionalTransaction) {
            try{
                Transaction transaction = optionalTransaction.get();

                transaction.setAmount(new BigDecimal(request.getPayhere_amount()));
                transaction.setPaymentId(request.getPayment_id());
                transaction.setMethod(request.getMethod());
                transaction.setStatusCode(request.getStatus_code());
                transaction.setStatusMessage(request.getStatus_message());
                transaction.setCardHolderName(Objects.requireNonNullElse(request.getCard_holder_name(), ""));
                transaction.setCardNo(Objects.requireNonNullElse(request.getCard_no(), ""));
                transaction.setTransactionStatus(TransactionStatus.SUCCESS);

                // Get admin email from custom_2
                String memberEmail = request.getCustom_2();
                if (memberEmail == null || memberEmail.isBlank()) {
                    log.warn("Missing admin email in custom_2 for order {}", request.getOrder_id());
                    return transaction;
                }

                // Fetch member and set in transaction
                Optional<Member> optionalMember = memberRepository.findByEmail(memberEmail);
                if (optionalMember.isEmpty()) {
                    log.warn("Admin not found for email: {}", memberEmail);
                    return transaction;
                }

                Member member = (Member) optionalMember.get();
                transaction.setMember(member);

                // Fetch organization from admin
                Organization organization = member.getOrganization();
                if (organization == null) {
                    log.warn("Organization is null for admin with email: {}", memberEmail);
                } else {
                    transaction.setOrganization(organization);
                }

                return transaction;
            }catch (Exception e){
                log.error("Error while verifying PayHere payment notification", e);
                return null;
            }
        }

        /**
         * Generates the MD5 signature for payment verification.
         *
         * @param merchantId the merchant ID
         * @param orderId the order ID
         * @param amount the payment amount
         * @param currency the payment currency
         * @param statusCode the payment status code
         * @return the generated MD5 signature
         * @throws Exception if an error occurs during hashing
         */
        private String generateMd5Sig(String merchantId, String orderId, String amount, String currency, String statusCode) throws Exception {
            String localSecretHash = md5(merchantSecret).toUpperCase();
            String raw = merchantId + orderId + amount + currency + statusCode + localSecretHash;
            return md5(raw).toUpperCase();
        }

        /**
         * Generates the MD5 hash of the input string.
         *
         * @param input the input string
         * @return the MD5 hash
         * @throws Exception if an error occurs during hashing
         */
        private String md5(String input) throws Exception {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        }
    }