package com.aluminate.aluminate_organization_backend.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
public class GlobalAuthResponse {
    private boolean success;
    private AdminGlobalDTO admin;
    private OrganizationGlobalDTO organization;
}
