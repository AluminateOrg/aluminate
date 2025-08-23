package com.aluminate.aluminate_organization_backend.service.members;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.group.GroupMembershipStatusDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import com.aluminate.aluminate_organization_backend.model.Organization;
import com.aluminate.aluminate_organization_backend.repository.GroupsRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberGroupRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.repository.OrganizationRepository;
import com.aluminate.aluminate_organization_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {

    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final GroupsRepository groupRepository;
    private final OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Override
    public Member createMember(MemberRequestDTO dto) {
        // Save the member
        String rawPassword = dto.getPassword();

        //get the organization by id
        Organization organization = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: ID " + dto.getOrganizationId()));

        Member member = Member.builder()
                .name(dto.getName())
                .nic(dto.getNic())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .regNo(dto.getRegNo())
                .address(dto.getAddress())
                .batch(dto.getBatch())
                .organization(organization)
                .password(passwordEncoder.encode(rawPassword))
                .build();

        Member savedMember = memberRepository.save(member);
        //send welcome email
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
    public List<GroupMembershipStatusDTO> getMemberGroupMembershipStatuses(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<MemberGroup> memberGroups = memberGroupRepository.findByMember(member);

        return memberGroups.stream()
                .map(memberGroup -> {
                    GroupMembershipStatusDTO statusDTO = new GroupMembershipStatusDTO();
                    statusDTO.setGroupId(memberGroup.getGroup().getId());

                    if (memberGroup.getRequestStatus() == null) {
                        statusDTO.setStatus("not_member");
                    } else {
                        statusDTO.setStatus(memberGroup.getRequestStatus().toString().toLowerCase());
                    }

                    return statusDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
        member.setActive(false);
        memberRepository.save(member);
    }

    // ---------------- New: self-profile functionality ----------------

    @Override
    public MemberResponseDTO getMyProfile() {
        return toResponseDto(getCurrentMemberOrThrow());
    }

    @Override
    @Transactional
    public MemberResponseDTO putMyProfile(MemberRequestDTO req) {
        Member me = getCurrentMemberOrThrow();

        // Full replace of editable fields (keep core identifiers immutable)
        if (!StringUtils.hasText(req.getName())) {
            throw new IllegalArgumentException("Name is required");
        }
        me.setName(req.getName());
        me.setPhone(req.getPhone());
        me.setAddress(req.getAddress());

        memberRepository.save(me);
        return toResponseDto(me);
    }

    @Override
    @Transactional
    public MemberResponseDTO patchMyProfile(MemberRequestDTO req) {
        Member me = getCurrentMemberOrThrow();

        if (req.getName() != null) me.setName(req.getName());
        if (req.getPhone() != null) me.setPhone(req.getPhone());
        if (req.getAddress() != null) me.setAddress(req.getAddress());

        memberRepository.save(me);
        return toResponseDto(me);
    }

    @Override
    @Transactional
    public void setMyAvatarUrl(String url) {
        Member me = getCurrentMemberOrThrow();
        me.setPhotoUrl(url);           // direct setter - your entity has photoUrl
        memberRepository.save(me);
    }

    // ---------------- Helpers ----------------

    private Member getCurrentMemberOrThrow() {
        String email = currentUserEmail();
        if (!StringUtils.hasText(email)) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found for email: " + email));
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User u) {
            return u.getUsername(); // commonly the email
        }
        return auth.getName();
    }

    private MemberResponseDTO toResponseDto(Member m) {
        List<Long> groupIds = memberGroupRepository.findByMember(m).stream()
                .map(mg -> mg.getGroup().getId())
                .collect(Collectors.toList());

        return MemberResponseDTO.builder()
                .name(m.getName())
                .nic(m.getNic())
                .is_active(m.isActive())
                .phone(m.getPhone())
                .email(m.getEmail())
                .regNo(m.getRegNo())
                .address(m.getAddress())
                .photoUrl(m.getPhotoUrl())
                .degree(m.getDegree())
                .company(m.getCompany())
                .position(m.getPosition())
                .linkedinUrl(m.getLinkedinUrl())
                .githubUrl(m.getGithubUrl())
                .websiteUrl(m.getWebsiteUrl())
                .batch(m.getBatch())
                .groupIds(groupIds)
                .password(null) // NEVER expose password
                .build();
    }
}
