package com.aluminate.aluminate_organization_backend.dto.hash;

import lombok.*;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HashResponse {
    private String hash;
    private Long transaction_id;

    public HashResponse(String organizationOrAdminNotFound) {
    }
}
