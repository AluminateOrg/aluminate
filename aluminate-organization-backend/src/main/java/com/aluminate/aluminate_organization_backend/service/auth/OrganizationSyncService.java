package com.aluminate.aluminate_organization_backend.service.auth;

import com.aluminate.aluminate_organization_backend.dto.login.AdminGlobalDTO;
import com.aluminate.aluminate_organization_backend.dto.login.OrganizationGlobalDTO;
import com.aluminate.aluminate_organization_backend.model.Admin;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Organization;
import com.aluminate.aluminate_organization_backend.repository.AdminRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.repository.OrganizationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationSyncService {

    private final OrganizationRepository organizationRepository;
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Organization syncOrganizationFromGlobal(AdminGlobalDTO adminDTO, OrganizationGlobalDTO orgDTO) {
        // 1. Find or create Organization
        Organization organization = organizationRepository
                .findByOrganizationName(orgDTO.getOrganizationName())
                .orElseGet(() -> {
                    Organization newOrg = new Organization();
                    newOrg.setOrganizationName(orgDTO.getOrganizationName());
                    // Set other org fields from orgDTO
                    return organizationRepository.save(newOrg);
                });

        // Update organization fields
        organization.setMaxMemberCount(orgDTO.getMaxMemberCount());
        organization.setCurrentMemberCount(orgDTO.getCurrentMemberCount());
        organization.setStatus(orgDTO.getStatus());
        organization.setDeleted(orgDTO.isDeleted());
        organization.setMembershipFree(orgDTO.isMembershipFree());

        // 2. Handle Admin
        Organization finalOrganization = organization;
        Admin admin = adminRepository.findAdminByEmail(adminDTO.getEmail())
                .orElseGet(() -> {
                    Admin newAdmin = new Admin();
                    newAdmin.setEmail(adminDTO.getEmail());
                    newAdmin.setOrganization(finalOrganization);
                    return newAdmin;
                });

        // Update admin fields
        admin.setName(adminDTO.getName());
        admin.setPhone(adminDTO.getPhone());
        admin.setPassword(adminDTO.getPassword()); // Remember to encode
        admin.setOrganization(organization);
        adminRepository.save(admin);

        // Set the admin reference in organization
        organization.setAdmin(admin);
        organization = organizationRepository.save(organization);

        // 3. Link existing members to this organization
        List<Member> existingMembers = memberRepository.findByOrganizationIsNull();
        if (!existingMembers.isEmpty()) {
            Organization finalOrganization1 = organization;
            existingMembers.forEach(member -> {
                member.setOrganization(finalOrganization1);
                memberRepository.save(member);
            });
        }

        return organization;
    }
}
