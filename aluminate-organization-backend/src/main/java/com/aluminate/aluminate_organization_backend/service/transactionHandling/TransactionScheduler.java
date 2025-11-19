package com.aluminate.aluminate_organization_backend.service.transactionHandling;

import com.aluminate.aluminate_organization_backend.config.util.RSAEncryptionUtil;
import com.aluminate.aluminate_organization_backend.model.GlobalTransactionTicket;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PrivateKey;
import java.security.PublicKey;

@Service
public class TransactionScheduler {
    private final TransactionHandler transactionHandler;

    private final Logger log = LoggerFactory.getLogger(TransactionScheduler.class);

    public TransactionScheduler(TransactionHandler transactionHandler) {
        this.transactionHandler = transactionHandler;
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Colombo")
    @Transactional
    public void scheduleTransactionSyncing() {
        //stage transactions and create tickets
        GlobalTransactionTicket globalTransactionTicket = transactionHandler.stageAndTicketTransactions();
        if(globalTransactionTicket == null) {
            log.info("Transaction staging and ticketing process completed with no new transactions.");
        }
        else {

            //call the other server using feign client
            transactionHandler.handleGlobalServer(globalTransactionTicket);
            log.info("Transaction staging and ticketing process completed successfully with new transactions.");


        }
    }
}
