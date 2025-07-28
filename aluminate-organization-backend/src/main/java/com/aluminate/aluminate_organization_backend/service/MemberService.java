package com.aluminate.aluminate_organization_backend.service;

import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberGroupRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GroupsRepository groupsRepository;

    @Autowired
    private MemberGroupRepository memberGroupRepository;

    public int saveValidMembers(List<MemberRowDTO> validRows, long groupId) {
        int count = 0;
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: ID " + groupId));

        for (MemberRowDTO dto :  validRows) {
            if (!memberRepository.existsByNic(dto.getNic()) && !memberRepository.existsByEmail(dto.getEmail())) {
                String rawPassword = dto.getNic();
                Member member = Member.builder()
                        .name(dto.getName())
                        .nic(dto.getNic())
                        .email(dto.getEmail())
                        .phone(dto.getPhone())
                        .regNo(dto.getRegNo())
                        .batch(dto.getBatch())
                        .password(passwordEncoder.encode(rawPassword))
                        .build();



                memberRepository.save(member);
                // Link member to group
                MemberGroup memberGroup = new MemberGroup();
                memberGroup.setMember(member);
                memberGroup.setGroup(group);
                memberGroup.setApproved(true);
                memberGroup.setRole("MEMBER");
                memberGroupRepository.save(memberGroup);
                //check member add to the group
                System.out.println("Adding member to group: " + member.getName() + " to group: " + group.getName());
                count++;
                emailService.sendWelcomeEmail(dto.getEmail(), dto.getName(), dto.getNic());
            }
        }
        return count;
    }

    public List<MemberResponseDTO> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        //print the member id
        members.forEach(member -> System.out.println("Member ID: " + member.getId() + ", Name: " + member.getName()));
        //check the member id type


        return members.stream()
                .map(member     -> MemberResponseDTO.builder()
                        .name(member.getName())
                        .nic(member.getNic())
                        .phone(member.getPhone())
                        .email(member.getEmail())
                        .is_active(member.isActive())
                        .regNo(member.getRegNo())
                        .address(member.getAddress())
                        .photoUrl(member.getPhotoUrl())
                        .degree(member.getDegree())
                        .company(member.getCompany())
                        .position(member.getPosition())
                        .linkedinUrl(member.getLinkedinUrl())
                        .githubUrl(member.getGithubUrl())
                        .websiteUrl(member.getWebsiteUrl())
                        .batch(member.getBatch())
                        .groupIds(memberGroupRepository.findGroupIdsByMemberId(member.getId()))
                        .build())
                .toList();
    }

}
