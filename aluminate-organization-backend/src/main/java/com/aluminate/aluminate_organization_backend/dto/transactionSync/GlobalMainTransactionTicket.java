package com.aluminate.aluminate_organization_backend.dto.transactionSync;


import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GlobalMainTransactionTicket {
    //encrypted ticket key & amount
    private String keyAndAmountEncrypted;

    //rest
    private Long organizationId;
}
