package com.aluminate.aluminate_organization_backend.service.transactionHandling;

import com.aluminate.aluminate_organization_backend.config.GlobalAuth.GlobalBackendAuthClient;
import com.aluminate.aluminate_organization_backend.config.ResponseWrapper;
import com.aluminate.aluminate_organization_backend.config.util.RSAEncryptionUtil;
import com.aluminate.aluminate_organization_backend.dto.transactionSync.GlobalMainTransactionTicket;
import com.aluminate.aluminate_organization_backend.dto.transactionSync.EncryptedTicketKey;
import com.aluminate.aluminate_organization_backend.dto.transactionSync.OrgTicketAck;
import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionHandler {
    private final IncomingTransactionRepository incomingTransactionRepository;
    private final StagedTransactionRepository stagedTransactionRepository;
    private final GlobalTransactionTicketRepository globalTransactionTicketRepository;
    private final OrganizationRepository organizationRepository;
    private final AckTransactionRepository ackTransactionRepository;
    private final TransactionRepository transactionRepository;

    @Value("${encryption.organization.private-key}")
    private String orgPrivateKeyENV;

    private final GlobalBackendAuthClient globalBackendAuthClient;

    @Value("${encryption.global.public-key}")
    private String globalPublicKeyENV;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PrivateKey orgPrivateKey;
    private PublicKey globalPublicKey;

    @PostConstruct
    public void initKeys() throws Exception {
        this.orgPrivateKey = RSAEncryptionUtil.privateKeyFromPem(orgPrivateKeyENV);
        this.globalPublicKey = RSAEncryptionUtil.publicKeyFromPem(globalPublicKeyENV);
    }
    private final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

    public TransactionHandler(
            IncomingTransactionRepository incomingTransactionRepository,
            StagedTransactionRepository stagedTransactionRepository,
            GlobalTransactionTicketRepository globalTransactionTicketRepository,
            GlobalBackendAuthClient globalBackendAuthClient,
            OrganizationRepository organizationRepository,
            AckTransactionRepository ackTransactionRepository,
            TransactionRepository transactionRepository

    ) {
        this.incomingTransactionRepository = incomingTransactionRepository;
        this.stagedTransactionRepository = stagedTransactionRepository;
        this.globalTransactionTicketRepository = globalTransactionTicketRepository;
        this.globalBackendAuthClient = globalBackendAuthClient;
        this.organizationRepository = organizationRepository;
        this.ackTransactionRepository = ackTransactionRepository;
        this.transactionRepository = transactionRepository;
    }


    public List<IncomingTransaction> getUnstagedTransactions(){
        return incomingTransactionRepository.findByStagedFalse();
    }
    //run the staging process & ticketing process

    public GlobalTransactionTicket stageAndTicketTransactions(){
        BigDecimal amount = BigDecimal.ZERO;

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
                return null;
            }
            log.info("Transaction staging process completed.");

            //retrieve staged transactions from yesterday 7 am to today 7 am where unticketed and key is null
            LocalDateTime yesterdayAt7Am = LocalDateTime.now().minusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayAt11Pm = LocalDateTime.now().withHour(23).withMinute(0).withSecond(0).withNano(0);

            List<StagedTransaction> stagedTransactions = stagedTransactionRepository.findStagedTransactionsBetweenDatesUnticketed(yesterdayAt7Am, todayAt11Pm);

            if(stagedTransactions.isEmpty()){
                log.info("No staged transactions found for ticketing process. Exiting.");
                return null;
            }
            log.info("Retrieved staged transactions for ticketing: " + stagedTransactions.size());
            //get the admin from the organization of the first staged transaction
            Admin admin = stagedTransactions.get(0).getOrganization().getAdmin();
            String adminEmail = admin.getEmail();

            //get first 3 characters of admin email
            String emailPrefix = adminEmail.substring(0, Math.min(adminEmail.length(), 3)).toUpperCase();
            //create a unique key for the ticket
            String ticketKey = stagedTransactions.get(0).getOrganization().getId() + "-" + emailPrefix + "-" + System.currentTimeMillis();

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
                throw new RuntimeException("Failed to create Global Transaction Ticket. Exiting process.");
            }
            //link staged transactions to GlobalTransactionTicket
            for(StagedTransaction stagedTransaction : stagedTransactions){
                stagedTransaction.setGlobalTransactionTicket(ticket);
            }
            stagedTransactionRepository.saveAll(stagedTransactions);
            log.info("Linked staged transactions to Global Transaction Ticket.");


            return ticket;

        }catch(Exception e){
            log.error("Error in stageAndTicketTransactions: " + e.getMessage());
            throw new RuntimeException("Error in stageAndTicketTransactions-> "+ e.getMessage());

        }
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

    //send to global server
    public void handleGlobalServer(GlobalTransactionTicket globalTransactionTicket){
        try{
            //encrypt object -> amount & key
            String encryptedKey = RSAEncryptionUtil.encrypt(
                    objectMapper.writeValueAsString(new EncryptedTicketKey(globalTransactionTicket.getKey())),
                    globalPublicKey
            );
            //create GlobalMainTransactionTicket object
            GlobalMainTransactionTicket globalMainTransactionTicket = new GlobalMainTransactionTicket();
            globalMainTransactionTicket.setEncryptedTicketKey(encryptedKey);
            globalMainTransactionTicket.setAmount(globalTransactionTicket.getAmount());
            globalMainTransactionTicket.setOrganizationId(globalTransactionTicket.getOrganization().getId());



            //call the other server using feign client
            ResponseEntity<ResponseWrapper<Boolean>> response = globalBackendAuthClient.syncOrgTransactionTickets(globalMainTransactionTicket);
            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null && Boolean.TRUE.equals(response.getBody().getData())){
                //update the ticket as sent
                globalTransactionTicket.setSend(true);
                globalTransactionTicketRepository.save(globalTransactionTicket);
                log.info("Successfully sent Global Transaction Ticket to Global Server for key: " + globalTransactionTicket.getKey());
            } else {
                log.error("Failed to send Global Transaction Ticket to Global Server for key: " + globalTransactionTicket.getKey());
                throw new RuntimeException("Failed to send Global Transaction Ticket to Global Server for key: " + globalTransactionTicket.getKey());
            }
        }catch(Exception e){
            log.error("Error handling Global Transaction Ticket: " + e.getMessage());
            throw new RuntimeException("Error sending Global Transaction Ticket-> " + e.getMessage());
        }

    }

    //validate ticket ack from global server
    public boolean validateTransactionTicketKey(String ticketKey){

        try{
            log.info("Validating Ticket Key: {}", ticketKey);
            //get ticket chars from beginning to the first '-'
            String orgId = ticketKey.split("-")[0];
            Optional<Organization> optionalOrganization = organizationRepository.findById(Long.valueOf(orgId));

            if(optionalOrganization.isEmpty()){
                log.error("Organization not found for ID: {}", orgId);
                return false;
            }
            Organization organization = optionalOrganization.get();

            //get admin
            Admin admin = organization.getAdmin();

            //get first 3 chars of admin email
            String emailPrefix = admin.getEmail().substring(0, 3).toUpperCase();

            //from ticket key get chars between first '-' and second '-'
            String ticketEmailPrefix = ticketKey.split("-")[1];
            if(!emailPrefix.equals(ticketEmailPrefix)){
                log.error("Ticket key email prefix does not match for ticket key: {}", ticketKey);
                return false;
            }
            //check if ticket exists in globalTransactionTicketRepository
            Optional<GlobalTransactionTicket> optionalTicket = globalTransactionTicketRepository.findByKey(ticketKey);
            if(optionalTicket.isEmpty()){
                log.error("Ticket key not found in GlobalTransactionTicketRepository: {}", ticketKey);
                return false;
            }

            log.info("Ticket key validated successfully for ticket key: {}", ticketKey);
            return true;


        } catch (Exception e) {
            log.error("Error while validating PayHere payment notification", e);

            throw new RuntimeException(e);
        }

    }

    //process ticket from global server
    public void processTransactionTicketAck(OrgTicketAck globalTicketAck){
        try{
            log.info("Processing Transaction Ticket Acknowledgement");
            BigDecimal totalAmount = BigDecimal.ZERO;
            //get ticket from local repository
            Optional<GlobalTransactionTicket> localTicket = globalTransactionTicketRepository.findByKey(globalTicketAck.getKey());
            if(localTicket.isEmpty()){
                log.error("Ticket key not found in GlobalTransactionTicketRepository: {}", globalTicketAck.getKey());
                return ;
            } else if (!Objects.equals(localTicket.get().getKey(), globalTicketAck.getKey())) {
                log.error("Ticket keys don't match: {}", globalTicketAck.getKey());
                return ;
            }
            //get all staged transaction which reference the localTicket id
            List<StagedTransaction> stagedTransactions = stagedTransactionRepository.findByGlobalTransactionTicketId(localTicket.get().getId());
            if(stagedTransactions.isEmpty()){
                log.error("No staged transactions retrieved for ticket key: {}", globalTicketAck.getKey());
                return ;
            }

            //get the incoming transactions from each staged transaction
            for(StagedTransaction stagedTransaction : stagedTransactions){
                IncomingTransaction incomingTransaction = stagedTransaction.getIncomingTransaction();
                //mark incoming transaction as acked
                incomingTransaction.setAck(true);
                incomingTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
                incomingTransactionRepository.save(incomingTransaction);

                //get transaction records incoming transactions reference & mark them as completed
                Transaction transaction = incomingTransaction.getTransaction();
                transaction.setTransactionStatus(TransactionStatus.COMPLETED);
                //save transaction
                transactionRepository.save(transaction);

                //sum total amount
                totalAmount = totalAmount.add(stagedTransaction.getAmount());
            }

            //create a ackTransaction per staged transaction
            for(StagedTransaction stagedTransaction : stagedTransactions) {
                AckTransaction ackTransaction = new AckTransaction();
                ackTransaction.setAmount(stagedTransaction.getAmount());
                ackTransaction.setCurrency(stagedTransaction.getCurrency());
                ackTransaction.setCategory(stagedTransaction.getCategory());
                ackTransaction.setIncomingTransaction(stagedTransaction.getIncomingTransaction());

                //save ackTransaction
                ackTransactionRepository.save(ackTransaction);

                //delete staged transaction
                stagedTransactionRepository.delete(stagedTransaction);

            }

            //verify total amount
            if(totalAmount.equals(globalTicketAck.getAmount()) && totalAmount.equals(localTicket.get().getAmount())){
                //mark local ticket as acked
                GlobalTransactionTicket ticketToUpdate = localTicket.get();
                ticketToUpdate.setAck(true);
                ticketToUpdate.setStatus(globalTicketAck.getStatus());
                globalTransactionTicketRepository.save(ticketToUpdate);
                log.info("Transaction Ticket Acknowledged successfully for key: {}", globalTicketAck.getKey());
            } else {
                log.error("Amount mismatch while processing Transaction Ticket Acknowledgement for key: {}", globalTicketAck.getKey());
                return ;
            }
        } catch (Exception e) {
            log.error("Error while processing Transaction Ticket Acknowledgement", e);
            throw new RuntimeException(e);
        }

    }



}
