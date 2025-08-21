package com.aluminate.aluminate_organization_backend.service.organization;

import com.aluminate.aluminate_organization_backend.dto.organization.GetOrgDTO;
import com.aluminate.aluminate_organization_backend.model.Admin;
import com.aluminate.aluminate_organization_backend.model.Organization;
import com.aluminate.aluminate_organization_backend.repository.AdminRepository;
import com.aluminate.aluminate_organization_backend.repository.OrganizationRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AdminRepository adminRepository;

    public OrganizationService(OrganizationRepository organizationRepository, AdminRepository adminRepository) {
        this.organizationRepository = organizationRepository;
        this.adminRepository = adminRepository;
    }

    // fetch the organization by admin id
    @Transactional(readOnly = true)
    public GetOrgDTO getOrg(Long adminId) {
        Organization organization = organizationRepository.findByAdminId(adminId);
        if (organization == null) {
            throw new RuntimeException("Organization not found");
        }

        //get admin details
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ObjectNotFoundException(adminId, "Admin not found"));

        return GetOrgDTO.builder()
                .id(organization.getId())
                .organizationName(organization.getOrganizationName())
                .maxMemberCount(organization.getMaxMemberCount())
                .currentMemberCount(organization.getCurrentMemberCount())
                .isDeleted(organization.isDeleted())
                .isMembershipFree(organization.isMembershipFree())
                .status(organization.getStatus())
                .build();
    }

}
