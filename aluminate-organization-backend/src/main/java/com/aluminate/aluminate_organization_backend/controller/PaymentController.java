//package com.aluminate.aluminate_organization_backend.controller;
//
//import com.aluminate.aluminate_organization_backend.dto.payment.PaymentDTO;
//import com.aluminate.aluminate_organization_backend.model.Campaign;
//import com.aluminate.aluminate_organization_backend.model.Donation;
//import com.aluminate.aluminate_organization_backend.model.Member;
//import com.aluminate.aluminate_organization_backend.repository.CampaignRepository;
//import com.aluminate.aluminate_organization_backend.repository.DonationRepository;
//import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.text.DecimalFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("${api.prefix}/user/payment")
//public class PaymentController {
//
//    // Existing repositories
//    private final MemberRepository memberRepository;
//
//    // New repositories for donation functionality
//    @Autowired
//    private CampaignRepository campaignRepository;
//
//    @Autowired
//    private DonationRepository donationRepository;
//
//    @Value("${payhere.merchantId}")
//    private String merchantID;
//
//    @Value("${payhere.secret}")
//    private String merchantSecret;
//
//    public PaymentController(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }
//
//    // ===============================
//    // EXISTING PAYMENT METHODS
//    // ===============================
//
//    @PostMapping("/create")
//    public Map<String, Object> createPayment(@RequestBody PaymentDTO paymentDTO) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            Member member = memberRepository.findById(paymentDTO.getPayerId()).orElseThrow();
//            response.put("merchant_id", merchantID);
//            response.put("merchant_secret", merchantSecret);
//            response.put("payment_date", paymentDTO.getPaymentDate());
//            response.put("amount", paymentDTO.getAmount());
//            response.put("currency", "LKR");
//            response.put("order_id", paymentDTO.getId());
//            response.put("status", paymentDTO.getStatus());
//            response.put("payer_id", paymentDTO.getPayerId());
//            response.put("payment_method", paymentDTO.getPaymentMethod());
//            response.put("payer_name", member.getName());
//            response.put("payer_email", member.getEmail());
//            response.put("payer_phone", member.getPhone());
//            response.put("message", "Payment created successfully");
//            return response;
//        } catch (Exception e) {
//            response.put("message", "Error creating payment: " + e.getMessage());
//            return response;
//        }
//    }
//
//    // ===============================
//    // NEW DONATION PAYMENT METHODS
//    // ===============================
//
//    @PostMapping("/donation/initialize")
//    public ResponseEntity<Map<String, Object>> initializeDonationPayment(@RequestBody DonationPaymentRequest request) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            // Validate campaign and member
//            Campaign campaign = campaignRepository.findById(request.getCampaignId())
//                    .filter(c -> !c.isDeleted() && c.isActive())
//                    .orElseThrow(() -> new IllegalArgumentException("Campaign not found or inactive"));
//
//            Member member = memberRepository.findById(request.getMemberId())
//                    .orElseThrow(() -> new IllegalArgumentException("Member not found"));
//
//            // Validate amount
//            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
//                throw new IllegalArgumentException("Amount must be greater than zero");
//            }
//
//            // Create donation record
//            Donation donation = Donation.builder()
//                    .campaign(campaign)
//                    .member(member)
//                    .amount(request.getAmount())
//                    .date(LocalDate.now())
//                    .createdAt(LocalDateTime.now())
//                    .isAnonymous(request.isAnonymous())
//                    .message(request.getMessage())
//                    .status(Donation.DonationStatus.PENDING)
//                    .paymentStatus(Donation.PaymentStatus.PENDING)
//                    .paymentMethod("PAYHERE")
//                    .build();
//
//            Donation savedDonation = donationRepository.save(donation);
//
//            // Generate order ID and hash
//            String orderId = "DON_" + savedDonation.getId() + "_" + System.currentTimeMillis();
//            savedDonation.setPaymentOrderId(orderId);
//            donationRepository.save(savedDonation);
//
//            // Generate payment hash
//            DecimalFormat df = new DecimalFormat("0.00");
//            String formattedAmount = df.format(request.getAmount());
//            String hash = generatePaymentHash(orderId, formattedAmount);
//
//            response.put("success", true);
//            response.put("orderId", orderId);
//            response.put("hash", hash);
//            response.put("merchantId", merchantID);
//            response.put("amount", formattedAmount);
//            response.put("currency", "LKR");
//            response.put("itemDescription", "Donation to " + campaign.getTitle());
//            response.put("sandbox", true); // Set based on environment
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("message", "Error initializing payment: " + e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    @PostMapping("/payhere/notify")
//    public ResponseEntity<String> handlePayHereNotification(@RequestParam Map<String, String> params) {
//        try {
//            String orderId = params.get("order_id");
//            String statusCode = params.get("status_code");
//            String md5sig = params.get("md5sig");
//
//            System.out.println("PayHere notification received for order: " + orderId);
//            System.out.println("Status code: " + statusCode);
//
//            // Validate signature
//            if (!validatePayHereSignature(params, md5sig)) {
//                System.err.println("Invalid PayHere signature for order: " + orderId);
//                return ResponseEntity.badRequest().body("Invalid signature");
//            }
//
//            // Find donation
//            Donation donation = donationRepository.findByPaymentOrderId(orderId)
//                    .orElseThrow(() -> new IllegalArgumentException("Donation not found for order: " + orderId));
//
//            // Update donation status
//            if ("2".equals(statusCode)) { // Success
//                donation.setStatus(Donation.DonationStatus.COMPLETED);
//                donation.setPaymentStatus(Donation.PaymentStatus.COMPLETED);
//                donation.setTransactionId(orderId);
//                donation.setUpdatedAt(LocalDateTime.now());
//
//                // Update campaign statistics
//                Campaign campaign = donation.getCampaign();
//                campaign.setRaised(campaign.getRaised().add(donation.getAmount()));
//                campaign.setDonorCount(campaign.getDonorCount() + 1);
//                campaignRepository.save(campaign);
//
//                System.out.println("Payment completed successfully for order: " + orderId);
//            } else {
//                donation.setStatus(Donation.DonationStatus.FAILED);
//                donation.setPaymentStatus(Donation.PaymentStatus.FAILED);
//                donation.setUpdatedAt(LocalDateTime.now());
//                System.err.println("Payment failed for order: " + orderId + " with status: " + statusCode);
//            }
//
//            donationRepository.save(donation);
//            return ResponseEntity.ok("OK");
//
//        } catch (Exception e) {
//            System.err.println("Error processing PayHere notification: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("ERROR");
//        }
//    }
//
//    @GetMapping("/payhere/success/{orderId}")
//    public ResponseEntity<Map<String, Object>> paymentSuccess(@PathVariable String orderId) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            // Check if donation exists and is completed
//            Donation donation = donationRepository.findByPaymentOrderId(orderId)
//                    .orElse(null);
//
//            if (donation != null && Donation.DonationStatus.COMPLETED.equals(donation.getStatus())) {
//                response.put("success", true);
//                response.put("message", "Payment completed successfully");
//                response.put("orderId", orderId);
//                response.put("amount", donation.getAmount());
//                response.put("campaignTitle", donation.getCampaign().getTitle());
//            } else {
//                response.put("success", false);
//                response.put("message", "Payment not found or not completed");
//                response.put("orderId", orderId);
//            }
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("message", "Error checking payment status: " + e.getMessage());
//            return ResponseEntity.ok(response);
//        }
//    }
//
//    @GetMapping("/payhere/cancel/{orderId}")
//    public ResponseEntity<Map<String, Object>> paymentCancel(@PathVariable String orderId) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            // Update donation status to cancelled if it exists
//            donationRepository.findByPaymentOrderId(orderId)
//                    .ifPresent(donation -> {
//                        if (Donation.DonationStatus.PENDING.equals(donation.getStatus())) {
//                            donation.setStatus(Donation.DonationStatus.CANCELLED);
//                            donation.setPaymentStatus(Donation.PaymentStatus.CANCELLED);
//                            donation.setUpdatedAt(LocalDateTime.now());
//                            donationRepository.save(donation);
//                        }
//                    });
//
//            response.put("success", true);
//            response.put("message", "Payment was cancelled");
//            response.put("orderId", orderId);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("message", "Error handling payment cancellation: " + e.getMessage());
//            return ResponseEntity.ok(response);
//        }
//    }
//
//    // ===============================
//    // HELPER METHODS
//    // ===============================
//
//    private String generatePaymentHash(String orderId, String amount) {
//        String currency = "LKR";
//        String hashString = merchantID + orderId + amount + currency + getMd5(merchantSecret);
//        return getMd5(hashString);
//    }
//
//    private boolean validatePayHereSignature(Map<String, String> params, String receivedSignature) {
//        try {
//            String merchantSecretHash = getMd5(merchantSecret);
//            String expectedSignature = getMd5(
//                    params.get("merchant_id") +
//                            params.get("order_id") +
//                            params.get("payhere_amount") +
//                            params.get("payhere_currency") +
//                            params.get("status_code") +
//                            merchantSecretHash
//            );
//            return expectedSignature.equalsIgnoreCase(receivedSignature);
//        } catch (Exception e) {
//            System.err.println("Error validating PayHere signature: " + e.getMessage());
//            return false;
//        }
//    }
//
//    // Existing getMd5 method (keep as is)
//    public static String getMd5(String input) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            byte[] messageDigest = md.digest(input.getBytes());
//            BigInteger no = new BigInteger(1, messageDigest);
//            String hashtext = no.toString(16);
//            while (hashtext.length() < 32) {
//                hashtext = "0" + hashtext;
//            }
//            return hashtext.toUpperCase();
//        }
//        catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    // ===============================
//    // DTO CLASSES
//    // ===============================
//
//    /**
//     * DTO class for donation payment requests
//     */
//    public static class DonationPaymentRequest {
//        private Long campaignId;
//        private Long memberId;
//        private BigDecimal amount;
//        private String firstName;
//        private String lastName;
//        private String email;
//        private String phone;
//        private String address;
//        private String city;
//        private String country;
//        private boolean isAnonymous;
//        private String message;
//
//        // Default constructor
//        public DonationPaymentRequest() {}
//
//        // Getters and setters
//        public Long getCampaignId() {
//            return campaignId;
//        }
//
//        public void setCampaignId(Long campaignId) {
//            this.campaignId = campaignId;
//        }
//
//        public Long getMemberId() {
//            return memberId;
//        }
//
//        public void setMemberId(Long memberId) {
//            this.memberId = memberId;
//        }
//
//        public BigDecimal getAmount() {
//            return amount;
//        }
//
//        public void setAmount(BigDecimal amount) {
//            this.amount = amount;
//        }
//
//        public String getFirstName() {
//            return firstName;
//        }
//
//        public void setFirstName(String firstName) {
//            this.firstName = firstName;
//        }
//
//        public String getLastName() {
//            return lastName;
//        }
//
//        public void setLastName(String lastName) {
//            this.lastName = lastName;
//        }
//
//        public String getEmail() {
//            return email;
//        }
//
//        public void setEmail(String email) {
//            this.email = email;
//        }
//
//        public String getPhone() {
//            return phone;
//        }
//
//        public void setPhone(String phone) {
//            this.phone = phone;
//        }
//
//        public String getAddress() {
//            return address;
//        }
//
//        public void setAddress(String address) {
//            this.address = address;
//        }
//
//        public String getCity() {
//            return city;
//        }
//
//        public void setCity(String city) {
//            this.city = city;
//        }
//
//        public String getCountry() {
//            return country;
//        }
//
//        public void setCountry(String country) {
//            this.country = country;
//        }
//
//        public boolean isAnonymous() {
//            return isAnonymous;
//        }
//
//        public void setAnonymous(boolean anonymous) {
//            isAnonymous = anonymous;
//        }
//
//        public String getMessage() {
//            return message;
//        }
//
//        public void setMessage(String message) {
//            this.message = message;
//        }
//
//        @Override
//        public String toString() {
//            return "DonationPaymentRequest{" +
//                    "campaignId=" + campaignId +
//                    ", memberId=" + memberId +
//                    ", amount=" + amount +
//                    ", firstName='" + firstName + '\'' +
//                    ", lastName='" + lastName + '\'' +
//                    ", email='" + email + '\'' +
//                    ", phone='" + phone + '\'' +
//                    ", isAnonymous=" + isAnonymous +
//                    ", message='" + message + '\'' +
//                    '}';
//        }
//    }
//}