package com.aluminate.aluminate_organization_backend.service.members;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {

    private final MemberRepository memberRepository;

    @Override
    public Member createMember(MemberRequestDTO request) {
        Member newMember = Member.builder()
                .name(request.getName())
                .nic(request.getNic())
                .phone(request.getPhone())
                .email(request.getEmail())
                .regNo(request.getRegNo())
                .address(request.getAddress())
                .photoUrl(request.getPhotoUrl())
                .password(request.getPassword()) // ⚠ Consider encrypting this
                .degree(request.getDegree())
                .company(request.getCompany())
                .position(request.getPosition())
                .linkedinUrl(request.getLinkedinUrl())
                .githubUrl(request.getGithubUrl())
                .websiteUrl(request.getWebsiteUrl())
                .batch(request.getBatch())
                .build();

        return memberRepository.save(newMember);
    }

    @Override
    public long getMemberCount() {
        return memberRepository.count();
    }

}
