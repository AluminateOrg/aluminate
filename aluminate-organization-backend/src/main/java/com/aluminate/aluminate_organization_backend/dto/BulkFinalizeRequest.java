package com.aluminate.aluminate_organization_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkFinalizeRequest {
    private List<MemberRowDTO> rows;
    private List<String> groups;
    private Long organizationId;
}
