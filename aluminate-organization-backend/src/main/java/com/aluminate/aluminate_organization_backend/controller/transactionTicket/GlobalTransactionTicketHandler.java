package com.aluminate.aluminate_organization_backend.controller.transactionTicket;


import com.aluminate.aluminate_organization_backend.config.ResponseWrapper;
import com.aluminate.aluminate_organization_backend.config.util.RSAEncryptionUtil;
import com.aluminate.aluminate_organization_backend.dto.login.LoginResponse;
import com.aluminate.aluminate_organization_backend.dto.transactionSync.DecryptedTicketKey;
import com.aluminate.aluminate_organization_backend.dto.transactionSync.GlobalMainTransactionTicket;
import com.aluminate.aluminate_organization_backend.dto.transactionSync.OrgMainTransactionTicket;
import com.aluminate.aluminate_organization_backend.dto.transactionSync.OrgTicketAck;
import com.aluminate.aluminate_organization_backend.service.transactionHandling.TransactionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PrivateKey;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/public/transactionTicketHandler")
public class GlobalTransactionTicketHandler {
    private final TransactionHandler transactionHandler;
    private final Logger logger = LoggerFactory.getLogger(GlobalMainTransactionTicket.class);
    private final ObjectMapper objectMapper;


    @Value("${encryption.organization.private-key}")
    private String organizationPrivateKeyENV;


    private PrivateKey organizationPrivateKey;

    @PostConstruct
    public void initKeys() throws Exception {
        this.organizationPrivateKey = RSAEncryptionUtil.privateKeyFromPem(organizationPrivateKeyENV);
    }

    @PostMapping("/handleTicket")
    @Transactional
    public ResponseEntity<ResponseWrapper<Boolean>> handleTransactionTicket(@RequestBody OrgMainTransactionTicket orgMainTransactionTicket){
        try{
            logger.info("Handling transaction ticket: {}", orgMainTransactionTicket);

            logger.info("encryptedTicketKey: {}", orgMainTransactionTicket.getEncryptedTicketKey());
            //decrypt the encryptedTicketKey
            String decryptedTicketKey = RSAEncryptionUtil.decrypt(
                    orgMainTransactionTicket.getEncryptedTicketKey(),
                    organizationPrivateKey
            );
            logger.info("Decrypted Ticket Key String: {}", decryptedTicketKey);

            //build OrgTicketAck
            OrgTicketAck orgTicketAck = OrgTicketAck.builder()
                    .key(decryptedTicketKey)
                    .amount(orgMainTransactionTicket.getAmount())
                    .organizationId(orgMainTransactionTicket.getOrganizationId())
                    .status(orgMainTransactionTicket.getTransactionStatus())
                    .build();

            //validate ticket
            boolean isTicketValid = transactionHandler.validateTransactionTicketKey(orgTicketAck.getKey());

            if(!isTicketValid){
                logger.error("Invalid transaction ticket key: {}", orgTicketAck.getKey());
                ResponseWrapper<Boolean> body = new ResponseWrapper<>(false, "Invalid transaction ticket key", false);
                return ResponseEntity.badRequest().body(body);
            }
            //process ticket
            transactionHandler.processTransactionTicketAck(orgTicketAck);

            //response
            ResponseWrapper<Boolean> body = new ResponseWrapper<>(true, "Transaction ticket handled successfully", true);
            logger.info("Transaction ticket handled successfully");
            return ResponseEntity.ok(body);


        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());

        }
    }

}
