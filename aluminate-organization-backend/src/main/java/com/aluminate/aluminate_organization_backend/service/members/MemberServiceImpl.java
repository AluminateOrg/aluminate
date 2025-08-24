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

import com.aluminate.aluminate_organization_backend.config.util.Jwt;

import com.aluminate.aluminate_organization_backend.dto.PublicMemberProfileDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {

    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final GroupsRepository groupRepository;
    private final OrganizationRepository organizationRepository;
    private final Jwt jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Override
    public Member createMember(MemberRequestDTO dto) {
        String rawPassword = dto.getPassword();

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

        emailService.sendEmail(dto.getEmail(), dto.getName(), dto.getNic());

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

    // ---------------- Profile: self-profile functionality ----------------

    @Override
    @Transactional(readOnly = true)
    public MemberResponseDTO getMyProfile() {
        return toResponseDto(getCurrentMemberOrThrow());
    }

    @Override
    @Transactional
    public MemberResponseDTO putMyProfile(MemberRequestDTO req) {
        Member me = getCurrentMemberOrThrow();

        if (!StringUtils.hasText(req.getName())) {
            throw new IllegalArgumentException("Name is required");
        }
        me.setName(req.getName());
        me.setPhone(req.getPhone());
        me.setAddress(req.getAddress());
        me.setCompany(req.getCompany());
        me.setPosition(req.getPosition());
        me.setDegree(req.getDegree());
        me.setLinkedinUrl(req.getLinkedinUrl());
        me.setGithubUrl(req.getGithubUrl());
        me.setWebsiteUrl(req.getWebsiteUrl());
        me.setBatch(req.getBatch());
        // photo/avatar is handled separately

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
        if (req.getCompany() != null) me.setCompany(req.getCompany());
        if (req.getPosition() != null) me.setPosition(req.getPosition());
        if (req.getDegree() != null) me.setDegree(req.getDegree());
        if (req.getLinkedinUrl() != null) me.setLinkedinUrl(req.getLinkedinUrl());
        if (req.getGithubUrl() != null) me.setGithubUrl(req.getGithubUrl());
        if (req.getWebsiteUrl() != null) me.setWebsiteUrl(req.getWebsiteUrl());
        if (req.getPhotoUrl() != null) me.setPhotoUrl(req.getPhotoUrl());

        memberRepository.save(me);
        return toResponseDto(me);
    }

    @Override
    @Transactional
    public void setMyAvatarUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("Avatar URL must not be empty");
        }
        Member me = getCurrentMemberOrThrow();
        me.setPhotoUrl(url);
        memberRepository.save(me);
    }

    // ---------------- Shareable public profile (QR link) ----------------

    @Override
    @Transactional
    public MemberResponseDTO enableShareLink() {
        Member me = getCurrentMemberOrThrow();
        if (!StringUtils.hasText(me.getPublicSlug())) {
            me.setPublicSlug(generateUniqueSlug());
        }
        me.setPublicProfileEnabled(true);
        memberRepository.save(me);
        return toResponseDto(me);
    }

    @Override
    @Transactional
    public MemberResponseDTO regenerateShareLink() {
        Member me = getCurrentMemberOrThrow();
        me.setPublicSlug(generateUniqueSlug());
        me.setPublicProfileEnabled(true);
        memberRepository.save(me);
        return toResponseDto(me);
    }

    @Override
    @Transactional
    public void disableShareLink() {
        Member me = getCurrentMemberOrThrow();
        me.setPublicProfileEnabled(false);
        memberRepository.save(me);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicMemberProfileDTO getPublicProfileBySlug(String slug) {
        Member m = memberRepository.findByPublicSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        if (!Boolean.TRUE.equals(m.getPublicProfileEnabled())) {
            throw new ResourceNotFoundException("Profile not public");
        }
        return PublicMemberProfileDTO.builder()
                .name(m.getName())
                .position(m.getPosition())
                .company(m.getCompany())
                .batch(m.getBatch())
                .degree(m.getDegree())
                .linkedinUrl(m.getLinkedinUrl())
                .githubUrl(m.getGithubUrl())
                .websiteUrl(m.getWebsiteUrl())
                .photoUrl(m.getPhotoUrl())
                .build();
    }

    private String generateUniqueSlug() {
        String slug;
        do {
            slug = java.util.UUID.randomUUID().toString().replace("-", "");
        } while (memberRepository.existsByPublicSlug(slug));
        return slug;
    }

    // ---------------- Helpers (profile only) ----------------

    private Member getCurrentMemberOrThrow() {
        String email = resolveCurrentEmail();
        if (!StringUtils.hasText(email)) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found for email: " + email));
    }

    /** Resolve email from SecurityContext or, as a fallback, from the JWT cookie. */
    private String resolveCurrentEmail() {
        var context = SecurityContextHolder.getContext();
        var auth = (context != null) ? context.getAuthentication() : null;
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof org.springframework.security.core.userdetails.User ud) {
                if (StringUtils.hasText(ud.getUsername())) return ud.getUsername();
            }
            try {
                if (principal != null) {
                    var m = principal.getClass().getMethod("getEmail");
                    Object v = m.invoke(principal);
                    if (v instanceof String s && StringUtils.hasText(s)) return s;

                    var u = principal.getClass().getMethod("getUsername");
                    Object vv = u.invoke(principal);
                    if (vv instanceof String s2 && StringUtils.hasText(s2)) return s2;
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
            }
            if (StringUtils.hasText(auth.getName())) return auth.getName();
        }
        return emailFromJwtCookie();
    }

    private String emailFromJwtCookie() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = (HttpServletRequest) attrs.resolveReference(RequestAttributes.REFERENCE_REQUEST);
            if (request == null || request.getCookies() == null) return null;

            String token = null;
            for (Cookie c : request.getCookies()) {
                if ("jwt".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
            if (!StringUtils.hasText(token)) return null;

            var claims = jwtUtil.extractAllClaims(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private MemberResponseDTO toResponseDto(Member m) {
        List<MemberGroup> memberships = memberGroupRepository.findByMember(m);
        List<Long> groupIds = (memberships == null ? Collections.<MemberGroup>emptyList() : memberships).stream()
                .filter(mg -> mg != null && mg.getGroup() != null && mg.getGroup().getId() != null)
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
                // NEW: fields the frontend QR page can read
                .publicSlug(m.getPublicSlug())
                .publicProfileEnabled(Boolean.TRUE.equals(m.getPublicProfileEnabled()))
                // Optional convenience mapping if your FE expects avatarUrl
                .avatarUrl(m.getPhotoUrl())
                .password(null) // never expose
                .build();
    }
}
