package com.aluminate.aluminate_organization_backend.service.transactionHandling;

import com.aluminate.aluminate_organization_backend.model.GlobalTransactionTicket;
import com.aluminate.aluminate_organization_backend.model.IncomingTransaction;
import com.aluminate.aluminate_organization_backend.model.Organization;
import com.aluminate.aluminate_organization_backend.model.StagedTransaction;
import com.aluminate.aluminate_organization_backend.repository.GlobalTransactionTicketRepository;
import com.aluminate.aluminate_organization_backend.repository.IncomingTransactionRepository;
import com.aluminate.aluminate_organization_backend.repository.StagedTransactionRepository;
import com.aluminate.aluminate_organization_backend.service.payment.PaymentNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionHandler {
    private final IncomingTransactionRepository incomingTransactionRepository;
    private final StagedTransactionRepository stagedTransactionRepository;
    private final GlobalTransactionTicketRepository globalTransactionTicketRepository;
    private BigDecimal amount = BigDecimal.ZERO;
    private final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

    public TransactionHandler(
            IncomingTransactionRepository incomingTransactionRepository,
            StagedTransactionRepository stagedTransactionRepository,
            GlobalTransactionTicketRepository globalTransactionTicketRepository

    ) {
        this.incomingTransactionRepository = incomingTransactionRepository;
        this.stagedTransactionRepository = stagedTransactionRepository;
        this.globalTransactionTicketRepository = globalTransactionTicketRepository;
    }

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Colombo")
    @Transactional
    public void syncGlobalTransactions(){

        try{
            log.info("Starting transaction staging process...");
            //get incomingTransactions which staged = false
            List<IncomingTransaction> unstagedTransactions = getUnstagedTransactions();
            log.info("retrieved unstaged transactions: " + unstagedTransactions.size());

            //save unstaged transactions to stagedTransactions table
            boolean staged = stageTransactions(unstagedTransactions);
            log.info("staging transactions status: " + staged);


            if(!staged) {
                log.info("No transactions were staged. Exiting process.");
                return;
            }
            log.info("Transaction staging process completed.");

            //retrieve staged transactions from yesterday 7 am to today 7 am where unticketed and key is null
            LocalDateTime yesterdayAt7Am = LocalDateTime.now().minusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayAt11Pm = LocalDateTime.now().withHour(23).withMinute(0).withSecond(0).withNano(0);

            List<StagedTransaction> stagedTransactions = stagedTransactionRepository.findStagedTransactionsBetweenDatesUnticketed(yesterdayAt7Am, todayAt11Pm);

            if(stagedTransactions.isEmpty()){
                log.info("No staged transactions found for ticketing process. Exiting.");
                return;
            }
            log.info("Retrieved staged transactions for ticketing: " + stagedTransactions.size());

            //create a unique key for the ticket
            String ticketKey = "ALUMINATE-TICKET-" + System.currentTimeMillis();

            //sum amount for each unticketed staged transaction & assign ticket key
            for(StagedTransaction stagedTransaction : stagedTransactions){
                //assign ticket key
                stagedTransaction.setKey(ticketKey);
                //sum amount
                amount = amount.add(stagedTransaction.getAmount());
            }
            log.info("Total amount for ticket " + ticketKey + " is: " + amount);

            //save StagedTransactions with ticket key
            stagedTransactionRepository.saveAll(stagedTransactions);
            log.info("Staged transactions updated with ticket key.");

            //create GlobalTransactionTicket

            GlobalTransactionTicket ticket = createGlobalTransactionTicket(ticketKey, amount, stagedTransactions.get(0).getOrganization());
            if(ticket == null){
                log.error("Failed to create Global Transaction Ticket. Exiting process.");
                return;
            }
            //link staged transactions to GlobalTransactionTicket
            for(StagedTransaction stagedTransaction : stagedTransactions){
                stagedTransaction.setGlobalTransactionTicket(ticket);
            }
            stagedTransactionRepository.saveAll(stagedTransactions);
            log.info("Linked staged transactions to Global Transaction Ticket.");

            //reset amount
            amount = BigDecimal.ZERO;

            //call the function to sync to global system using feign client

            //done
            log.info("Transaction syncing process completed successfully.");
        } catch(Exception e){
            log.error("Error starting transaction staging process: " + e.getMessage());
        }
    }

    public List<IncomingTransaction> getUnstagedTransactions(){
        return incomingTransactionRepository.findByStagedFalse();
    }

    public boolean stageTransactions(List<IncomingTransaction> unstagedTransactions){

        try{
            if(unstagedTransactions.isEmpty()){
                log.info("No unstaged transactions found.");
                return false;
            }
            else{
                for(IncomingTransaction unstagedTransaction : unstagedTransactions){
                    //create staged transaction
                    StagedTransaction stagedTransaction = new StagedTransaction();
                    stagedTransaction.setAmount(unstagedTransaction.getAmount());
                    stagedTransaction.setCurrency(unstagedTransaction.getCurrency());
                    stagedTransaction.setCategory(unstagedTransaction.getCategory());
                    stagedTransaction.setIncomingTransaction(unstagedTransaction);
                    stagedTransaction.setOrganization(unstagedTransaction.getOrganization());
                    stagedTransactionRepository.save(stagedTransaction);

                    //save staged transaction
                    unstagedTransaction.setStaged(true);
                    incomingTransactionRepository.save(unstagedTransaction);

                }
                log.info("Staged transactions saved.");
                return true;
            }
        } catch(Exception e){
            log.error("Error checking unstaged transactions: " + e.getMessage());
            return false;
        }


    }

    public GlobalTransactionTicket createGlobalTransactionTicket(String ticketKey, BigDecimal TotalAmount, Organization organization){
        try{
            //create GlobalTransactionTicket
            GlobalTransactionTicket globalTransactionTicket = new GlobalTransactionTicket();
            globalTransactionTicket.setAmount(TotalAmount);
            globalTransactionTicket.setKey(ticketKey);
            globalTransactionTicket.setOrganization(organization);

            //save GlobalTransactionTicket
            globalTransactionTicketRepository.save(globalTransactionTicket);
            log.info("Created Global Transaction Ticket with key: " + ticketKey);
            return globalTransactionTicket;



        } catch(Exception e){
            log.error("Error creating Global Transaction Ticket: " + e.getMessage());
            return null;
        }
    }


}
