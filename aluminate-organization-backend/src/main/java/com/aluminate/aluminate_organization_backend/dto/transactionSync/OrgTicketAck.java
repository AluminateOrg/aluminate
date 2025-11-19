package com.aluminate.aluminate_organization_backend.dto.transactionSync;


import com.aluminate.aluminate_organization_backend.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgTicketAck {
    private String key;
    private Long organizationId;
    private TransactionStatus status;
    private BigDecimal amount;
}
