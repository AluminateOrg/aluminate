package com.aluminate.aluminate_organization_backend.dto.transactionSync;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalMainTransactionTicket {
    //encrypted ticket key & amount
    private String encryptedTicketKey;
    private BigDecimal amount;
    //rest
    private Long organizationId;
}
