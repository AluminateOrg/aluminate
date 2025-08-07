package com.aluminate.aluminate_organization_backend.service.members;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberGroupRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {

    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final GroupsRepository groupRepository; // ✅ Add this

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Override
    public Member createMember(MemberRequestDTO dto) {
        // Save the member
        String rawPassword = dto.getPassword();
        Member member = Member.builder()
                .name(dto.getName())
                .nic(dto.getNic())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .regNo(dto.getRegNo())
                .address(dto.getAddress())
                .batch(dto.getBatch())
                .password(passwordEncoder.encode(rawPassword))
                .build();

        Member savedMember = memberRepository.save(member);
        // send welcome email
        emailService.sendEmail(dto.getEmail(), dto.getName(), dto.getNic());

        // Link member to groups
        for (Long groupId : dto.getGroupIds()) {
            Groups group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found: ID " + groupId));

            MemberGroup memberGroup = new MemberGroup();
            memberGroup.setMember(savedMember);
            memberGroup.setGroup(group);
            memberGroup.setApproved(true);
            memberGroup.setRole("MEMBER");

            memberGroupRepository.save(memberGroup);
        }

        return savedMember;
    }

    @Override
    public long getMemberCount() {
        return memberRepository.count();
    }

    @Override
    public void deactivateMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));

        member.setIsActive(false);
        memberRepository.save(member);
    }

}
