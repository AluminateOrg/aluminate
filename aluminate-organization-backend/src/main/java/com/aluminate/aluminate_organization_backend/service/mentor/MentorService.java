package com.aluminate.aluminate_organization_backend.service.mentor;

import com.aluminate.aluminate_organization_backend.dto.mentor.MentorRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.mentor.MentorResponseDTO;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Mentor;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.repository.MentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MentorService {

    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private MemberRepository memberRepository;

    public MentorResponseDTO applyAsMentor(MentorRequestDTO request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (mentorRepository.existsByMember(member)) throw  new RuntimeException("Member already exists");

        Mentor mentor =  Mentor.builder()
                .member(member)
                .yearsOfExperience(request.getYearsOfExperience())
                .hourlyRate(request.getHourlyRate())
                .bio(request.getBio())
                .linkedInUrl(request.getLinkedInUrl())
                .portfolioUrl(request.getPortfolioUrl())
                .motivation(request.getMotivation())
                .languages(request.getLanguages())
                .preferredMenteeLevel(request.getPreferredMenteeLevel())
                .maxMentees(request.getMaxMentees())
                .isApproved(false)
                .rating(0.0)
                .sessionCount(0)
                .build();

        Mentor saved = mentorRepository.save(mentor);

        return MentorResponseDTO.builder()
                .id(saved.getId())
                .memberId(member.getId())
                .yearsOfExperience(saved.getYearsOfExperience())
                .hourlyRate(saved.getHourlyRate())
                .bio(saved.getBio())
                .linkedInUrl(saved.getLinkedInUrl())
                .portfolioUrl(saved.getPortfolioUrl())
                .motivation(saved.getMotivation())
                .languages(saved.getLanguages())
                .preferredMenteeLevel(saved.getPreferredMenteeLevel())
                .maxMentees(saved.getMaxMentees())
                .isApproved(saved.isApproved())
                .rating(saved.getRating())
                .sessionCount(saved.getSessionCount())
                .build();

    }
}