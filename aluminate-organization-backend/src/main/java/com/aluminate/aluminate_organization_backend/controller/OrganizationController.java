package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.organization.GetOrgDTO;
import com.aluminate.aluminate_organization_backend.service.organization.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/common/get-org")
    public ResponseEntity<GetOrgDTO> getOrganization() {
        try {
            GetOrgDTO organization = organizationService.getOrg();
            System.out.println("Organization from the backend: " + organization);
            return ResponseEntity.ok(organization);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Handle the exception appropriately
        }
    }

}
