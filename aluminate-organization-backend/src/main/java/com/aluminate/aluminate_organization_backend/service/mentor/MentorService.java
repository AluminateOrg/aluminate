package com.aluminate.aluminate_organization_backend.service.mentor;

import com.aluminate.aluminate_organization_backend.dto.mentor.MentorApplicationDTO;
import com.aluminate.aluminate_organization_backend.dto.mentor.MentorRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.mentor.MentorResponseDTO;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Mentor;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.repository.MentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorService {


    private final MentorRepository mentorRepository;
    private final MemberRepository memberRepository;

    public MentorService(MentorRepository mentorRepository, MemberRepository memberRepository) {
        this.mentorRepository = mentorRepository;
        this.memberRepository = memberRepository;
    }

    // Apply as a mentor
    @Transactional
    public MentorResponseDTO applyAsMentor(MentorRequestDTO request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (mentorRepository.existsByMember(member)) throw  new RuntimeException("Member already exists");

        Mentor mentor =  Mentor.builder()
                .member(member)
                .yearsOfExperience(request.getYearsExperience())
                .hourlyRate(request.getHourlyRate())
                .bio(request.getBio())
                .linkedInUrl(request.getLinkedinUrl())
                .portfolioUrl(request.getPortfolioUrl())
                .motivation(request.getMotivation())
                .languages(request.getLanguages())
                .skills(request.getSkills())
                .preferredMenteeLevel(request.getPreferredMenteeLevel())
                .maxMentees(request.getMaxMentees())
                .isApproved(false)
                .rating(0.0)
                .sessionCount(0)
                .createdAt(LocalDateTime.now())
                .availability(request.getAvailability())
                .status("PENDING")
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
                .skills(saved.getSkills())
                .preferredMenteeLevel(saved.getPreferredMenteeLevel())
                .maxMentees(saved.getMaxMentees())
                .isApproved(saved.isApproved())
                .rating(saved.getRating())
                .sessionCount(saved.getSessionCount())
                .build();

    }

    // fetch all mentor applications
    @Transactional(readOnly = true)
    public List<MentorResponseDTO> getAllMentorApplications() {
        return mentorRepository.findAll().stream()
                .map(mentor -> MentorResponseDTO.builder()
                        .id(mentor.getId())
                        .memberId(mentor.getMember().getId())
                        .yearsOfExperience(mentor.getYearsOfExperience())
                        .hourlyRate(mentor.getHourlyRate())
                        .bio(mentor.getBio())
                        .linkedInUrl(mentor.getLinkedInUrl())
                        .portfolioUrl(mentor.getPortfolioUrl())
                        .motivation(mentor.getMotivation())
                        .languages(mentor.getLanguages())
                        .preferredMenteeLevel(mentor.getPreferredMenteeLevel())
                        .maxMentees(mentor.getMaxMentees())
                        .isApproved(mentor.isApproved())
                        .rating(mentor.getRating())
                        .sessionCount(mentor.getSessionCount())
                        .build())
                .collect(Collectors.toList());
    }

    // get all unapproved mentors
    @Transactional
    public List<MentorApplicationDTO> getAllUnapprovedMentors() {
        List<Mentor> unapprovedMentors = mentorRepository.findAll().stream()
                .filter(mentor -> !mentor.isApproved())
                .toList();

        return getMentorApplicationDTOS(unapprovedMentors);
    }

    // Approve a mentor application
    @Transactional
    public boolean approveMentorApplication(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mentor application not found"));
        if (mentor.isApproved()) throw new RuntimeException("Mentor application is already approved");

        mentor.setApproved(true);
        mentorRepository.save(mentor);
        return true;
    }

    //delete a mentor application
    @Transactional
    public boolean rejectMentorApplication(Long applicationId) {
        Mentor mentor = mentorRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Mentor application not found"));
        if (mentor.isApproved()) throw new RuntimeException("Mentor application is already approved");

        mentor.getLanguages().clear();
        mentor.getSkills().clear();
        mentorRepository.saveAndFlush(mentor);

        mentorRepository.delete(mentor);
        return true;
    }

    //get all approved mentors
    @Transactional(readOnly = true)
    public List<MentorApplicationDTO> getAllApprovedMentors() {
        List<Mentor> approvedMentors = mentorRepository.findAll().stream()
                .filter(Mentor::isApproved)
                .toList();

        return getMentorApplicationDTOS(approvedMentors);
    }

    //mentor application DTO conversion
    private List<MentorApplicationDTO> getMentorApplicationDTOS(List<Mentor> unapprovedMentors) {
        return unapprovedMentors.stream().map(mentor -> {
            Member member = mentor.getMember();

            MentorApplicationDTO dto = new MentorApplicationDTO();
            dto.setId(mentor.getId());
            dto.setApplicantId(member.getId());
            dto.setApplicantName(member.getName());
            dto.setApplicantEmail(member.getEmail());
            dto.setApplicantAvatar(member.getPhotoUrl());
            dto.setBio(mentor.getBio());
            dto.setLinkedInUrl(mentor.getLinkedInUrl());
            dto.setPortfolioUrl(mentor.getPortfolioUrl());
            dto.setMotivation(mentor.getMotivation());
            dto.setYearsExperience(mentor.getYearsOfExperience());
            dto.setLanguages(mentor.getLanguages());
            dto.setSkills(mentor.getSkills());
            dto.setPreferredMenteeLevel(mentor.getPreferredMenteeLevel());
            dto.setMaxMentees(mentor.getMaxMentees());
            dto.setApproved(mentor.isApproved());
            return dto;
        }).toList();
    }

    //deactivate mentors
    @Transactional
    public boolean deactivateMentor(Long id) {
        Mentor mentor = mentorRepository.findByMemberId(id)
                .orElseThrow(() -> new RuntimeException("Mentor application not found"));
        System.out.println("Deactivating mentor: " + mentor.getId());
        if (!mentor.isApproved()) {
            return false;
        }
        mentor.setApproved(false);
        mentorRepository.save(mentor);
        return true;
    }

}