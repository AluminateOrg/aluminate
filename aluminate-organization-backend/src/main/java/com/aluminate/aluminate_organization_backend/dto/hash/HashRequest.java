package com.aluminate.aluminate_organization_backend.dto.hash;

import com.aluminate.aluminate_organization_backend.model.PaymentCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HashRequest {
    @NotNull(message = "Amount is required")
    private Double amount;
    private String currency;
    @NotNull(message = "Mode is required")
    private PaymentCategory mode;
}
