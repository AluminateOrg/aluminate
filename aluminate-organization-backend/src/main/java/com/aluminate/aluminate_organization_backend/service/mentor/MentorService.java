package com.aluminate.aluminate_organization_backend.service.mentor;

import com.aluminate.aluminate_organization_backend.dto.mentor.*;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Mentor;
import com.aluminate.aluminate_organization_backend.model.MentorProgram;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.repository.MentorProgramRepository;
import com.aluminate.aluminate_organization_backend.repository.MentorRepository;
import com.aluminate.aluminate_organization_backend.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MentorService {


    private final MentorRepository mentorRepository;
    private final MemberRepository memberRepository;
    private final MentorProgramRepository mentorProgramRepository;
    private final EmailService emailService;

    public MentorService(MentorRepository mentorRepository, MemberRepository memberRepository, MentorProgramRepository mentorProgramRepository, EmailService emailService) {
        this.mentorRepository = mentorRepository;
        this.memberRepository = memberRepository;
        this.mentorProgramRepository = mentorProgramRepository;
        this.emailService = emailService;
    }

    // Apply as a mentor
    @Transactional
    public MentorResponseDTO applyAsMentor(MentorRequestDTO request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        System.out.println("Member details: " + member);

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
            dto.setHourlyRate(mentor.getHourlyRate());
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

    // request a session with a mentor
    @Transactional
    public boolean requestSessionWithMentor(SessionRequestDTO requestDTO) {
        Mentor mentor = mentorRepository.findById(requestDTO.getMentorId())
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        Member member = null;
        //create a set to hold members
        Set<Member> participants = requestDTO.getUserId().stream()
                .map(userId -> memberRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Member not found")))
                .collect(Collectors.toSet());

        Member createdBy = memberRepository.findById(requestDTO.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        //save to the mentor program table
        MentorProgram mentorProgram = MentorProgram.builder()
                .mentor(mentor)
                .participants(participants)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .status("PENDING")
                .build();

        //save the mentor program

//        return mentorProgramRepository.save(mentorProgram);
        MentorProgram savedProgram = mentorProgramRepository.save(mentorProgram);

        //update the mentor's session count
        mentor.setSessionCount(mentor.getSessionCount() + 1);
        mentorRepository.save(mentor);

//        if (participants.size() > 1) {
//            //
//        }

        //send email to the mentor
        emailService.sendEmail(mentor.getMember().getEmail(), "New Mentorship Session Request",
                "You have a new mentorship session request from " +
                        participants.stream().map(Member::getName).collect(Collectors.joining(", ")) +
                        participants.stream().map(Member::getEmail).collect(Collectors.joining(", ")) +
                        ". Please log in to your account to accept or reject the request.");

        return true;

    }

    //accept a session and update the session date, time and url
    @Transactional
    public boolean acceptSession(SessionRespondDTO sessionRespondDTO){
        MentorProgram mentorProgram = mentorProgramRepository.findById(sessionRespondDTO.getId())
                .orElseThrow(() -> new RuntimeException("Mentor program not found"));

        mentorProgram.setProgramUrl(sessionRespondDTO.getProgramUrl());
        mentorProgram.setStatus("ACCEPTED");
        mentorProgram.setDate(LocalDate.now());
        mentorProgram.setTime(LocalTime.now());
        mentorProgramRepository.save(mentorProgram);

        return true;
    }

    //reject a session
    @Transactional
    public boolean rejectSession(SessionRespondDTO sessionRespondDTO) {
        MentorProgram mentorProgram = mentorProgramRepository.findById(sessionRespondDTO.getId())
                .orElseThrow(() -> new RuntimeException("Mentor program not found"));

        mentorProgram.setStatus("REJECTED");
        mentorProgramRepository.save(mentorProgram);

        return true;
    }

    //get all sessions by mentor
    @Transactional(readOnly = true)
    public List<MentorSessionDTO> getAllSessionsByMentor(Long mentorId) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        return mentorProgramRepository.findAllByMentorId(mentorId).stream()
                .map(program -> MentorSessionDTO.builder()
                        .id(program.getId())
                        .programUrl(program.getProgramUrl())
                        .mentorName(mentor.getMember().getName())
                        .menteeName(
                                program.getParticipants().stream()
                                        .map(Member::getName)
                                        .collect(Collectors.joining(", "))
                        )
                        .menteeEmail(
                                program.getParticipants().stream()
                                        .map(Member::getEmail)
                                        .collect(Collectors.joining(", "))
                        )
                        .status(program.getStatus())
                        .date(program.getDate())
                        .time(program.getTime())
                        .sessionDuration("1 hour")
                        .createdAt(program.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MentorSessionDTO> getAllSessionsByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        System.out.println("Fetching sessions for member: " + member.getId());
        //mentor programs where the member is a participant
        List<MentorProgram> mentorProgram = mentorProgramRepository.findAllByParticipantId(memberId);
        System.out.println("mentorProgram: " + mentorProgram);
        if (mentorProgram == null) {
            throw new RuntimeException("No mentor programs found for member");
        }
        return mentorProgram.stream()
                .map(program -> MentorSessionDTO.builder()
                        .id(program.getId())
                        .programUrl(program.getProgramUrl())
                        .mentorName(program.getMentor().getMember().getName())
                        .menteeName(member.getName())
                        .status(program.getStatus())
                        .date(program.getDate())
                        .hourly_rate(program.getMentor().getHourlyRate())
                        .time(program.getTime())
                        .isPaid(program.isPaid())
                        .sessionDuration("1 hour") // Placeholder, can be calculated based on program data
                        .build())
                .collect(Collectors.toList());
    }

    //get all members for connecting to the session
    public List<ConnectMemberResponse> getAllMembers(Long id) {
        List<Member> members = memberRepository.findAll();
        //remove the member with the given id
        members = members.stream()
                .filter(member -> !Objects.equals(member.getId(), id)).toList();
        return members.stream().map(member -> {
            ConnectMemberResponse response = new ConnectMemberResponse();
            response.setId(member.getId());
            response.setName(member.getName());
            response.setEmail(member.getEmail());
            return response;
        }).collect(Collectors.toList());
    }

    public boolean isMentor(Long id) {

        return mentorRepository.findByMemberId(id)
                .map(Mentor::isApproved)
                .orElse(false);
    }

    public boolean updateMentorSessionDetails(MentorSessionAcceptDTO mentorSessionAcceptDTO, Long id) {
        MentorProgram mentorProgram = mentorProgramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mentor program not found"));
        mentorProgram.setProgramUrl(mentorSessionAcceptDTO.getProgramUrl());
        mentorProgram.setDate(mentorSessionAcceptDTO.getDate());
        mentorProgram.setTime(mentorSessionAcceptDTO.getTime());
        mentorProgram.setStatus("SCHEDULED");
        mentorProgramRepository.save(mentorProgram);
        return true;
    }

    public boolean updateProgramIsPaid(Long id) {
        MentorProgram mentorProgram = mentorProgramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mentor program not found"));
        mentorProgram.setPaid(true);
        return true;
    }

    public List<MentorApplicationDTO> getAllApprovedMentorsExceptSelf(Long id) {
        List<Mentor> approvedMentors = mentorRepository.findAll().stream()
                .filter(mentor -> mentor.isApproved() && !mentor.getMember().getId().equals(id))
                .toList();

        return getMentorApplicationDTOS(approvedMentors);
    }


    public List<MentorSessionDTO> getAllSessionsExceptSelf(Long userId) {
        Mentor mentor = mentorRepository.findByMemberId(userId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        List<MentorSessionDTO> allSessions = mentorProgramRepository.findAll().stream()
                .filter(mentorProgram -> mentorProgram.getCreatedBy().equals(member))
                .map(program -> MentorSessionDTO.builder()
                        .id(program.getId())
                        .programUrl(program.getProgramUrl())
                        .mentorName(program.getMentor().getMember().getName())
                        .menteeName(
                                program.getParticipants().stream()
                                        .map(Member::getName)
                                        .collect(Collectors.joining(", "))
                        )
                        .menteeEmail(
                                program.getParticipants().stream()
                                        .map(Member::getEmail)
                                        .collect(Collectors.joining(", "))
                        )
                        .status(program.getStatus())
                        .date(program.getDate())
                        .time(program.getTime())
                        .sessionDuration("1 hour")
                        .createdAt(program.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return allSessions;
    }
}