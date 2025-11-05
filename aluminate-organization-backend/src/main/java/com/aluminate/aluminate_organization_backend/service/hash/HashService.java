package com.aluminate.aluminate_organization_backend.service.hash;

import com.aluminate.aluminate_organization_backend.dto.hash.HashResponse;
import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.IncomingTransactionRepository;
import com.aluminate.aluminate_organization_backend.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

import static com.aluminate.aluminate_organization_backend.model.PaymentCategory.DONATION;

@Service
public class HashService {

    @Value("${payhere.merchant_id}")
    private String merchantId;

    @Value("${payhere.merchant_secret}")
    private String merchantSecret;

    private final TransactionRepository transactionRepository;
    private final IncomingTransactionRepository incomingTransactionRepository;
    private final Logger logger = LoggerFactory.getLogger(HashService.class);

    public HashService(TransactionRepository transactionRepository, IncomingTransactionRepository incomingTransactionRepository) {
        this.transactionRepository = transactionRepository;
        this.incomingTransactionRepository = incomingTransactionRepository;
    }

    public HashResponse generateHash(PaymentCategory paymentCategory, double amount, String currency, Organization organization, Member member) {


        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(amount); // must be like "1000.00"

        String localHash = md5(merchantSecret).toUpperCase(); // CRITICAL


        // Save transaction first
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(amount))
                .currency(currency)
                .transactionStatus(TransactionStatus.PENDING)
                .category(paymentCategory)
                .organization(organization)
                .member(member)
                .build();
        Transaction saved = transactionRepository.save(transaction);

        //mock saving incoming transaction for testing
        IncomingTransaction incomingTransaction = IncomingTransaction.builder()
                .amount(BigDecimal.valueOf(amount))
                .currency(currency)
                .transactionStatus(TransactionStatus.SUCCESS)
                .category(DONATION)
                .organization(organization)
                .transaction(transaction)
                .staged(false)
                .ack(false)
                .build();

        IncomingTransaction savedIncoming = incomingTransactionRepository.save(incomingTransaction);

        String orderId = saved.getId().toString(); // Must use this exact ID in frontend
        logger.info("Order ID: " + orderId);

        String raw = (merchantId + orderId + formattedAmount + currency + localHash).toUpperCase();
        String hash = md5(raw).toUpperCase();



        return new HashResponse(hash, saved.getId()); // include order ID
    }

    private String md5(String input) {
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
