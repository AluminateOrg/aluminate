package com.aluminate.aluminate_organization_backend.service.info;

import com.aluminate.aluminate_organization_backend.dto.MemberDTO;
import com.aluminate.aluminate_organization_backend.dto.info.InfoUserDetailsResponse;
import com.aluminate.aluminate_organization_backend.dto.login.AdminDTO;
import com.aluminate.aluminate_organization_backend.model.Admin;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Organization;
import com.aluminate.aluminate_organization_backend.model.Status;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class infoService {
    // This service will handle information retrieval logic.


    public InfoUserDetailsResponse getUser() {
        //get the user from security context holder
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!(principal instanceof Admin) && !(principal instanceof Member)) {
            throw new IllegalStateException("Authenticated principal is not an Admin or Member");
        }
        Organization organization;
        if(principal instanceof Member) {
            Member member = (Member) principal;

            MemberDTO sendingDTO = new MemberDTO(
                    member.getId(),
                    member.getName(),
                    member.getNic(),
                    member.getPhone(),
                    member.getEmail(),
                    member.getRegNo(),
                    member.getAddress(),
                    member.getPhotoUrl(),
                    member.getDegree(),
                    member.getCompany(),
                    member.getPosition(),
                    member.getLinkedinUrl(),
                    member.getGithubUrl(),
                    member.getWebsiteUrl(),
                    member.getBatch()
            );
            //get the organization from the member
             organization = member.getOrganization();
            if(organization == null) {
                throw new IllegalStateException("Member does not belong to any organization");
            }
            if(organization.getStatus() != Status.ACTIVE) {
                throw new IllegalStateException("Organization is not active");
            }
            return new InfoUserDetailsResponse(sendingDTO);
        }else{
            Admin admin = (Admin) principal;

            if(admin.getOrganization() == null) {
                throw new IllegalStateException("Admin does not belong to any organization");
            }
            //create the AdminDTO
            AdminDTO sendingDTO = new AdminDTO(
                    admin.getId(),
                    admin.getName(),
                    admin.getEmail(),
                    admin.getPhone()
            );
            //get the organization from the admin
            organization = admin.getOrganization();

            if(organization.getStatus() != Status.ACTIVE) {
                throw new IllegalStateException("Organization is not active");
            }
            return new InfoUserDetailsResponse(sendingDTO);

        }

    }
}
