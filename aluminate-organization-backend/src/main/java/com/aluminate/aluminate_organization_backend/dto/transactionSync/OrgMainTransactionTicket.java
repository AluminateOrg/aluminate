package com.aluminate.aluminate_organization_backend.dto.transactionSync;

import com.aluminate.aluminate_organization_backend.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgMainTransactionTicket {
    private String encryptedTicketKey;
    private BigDecimal amount;
    private Long organizationId;
    private TransactionStatus transactionStatus;
}
