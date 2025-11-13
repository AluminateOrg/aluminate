package com.aluminate.aluminate_organization_backend.dto.transactionSync;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class KeyAndAmount {
    private String key;
    private BigDecimal amount;
}
