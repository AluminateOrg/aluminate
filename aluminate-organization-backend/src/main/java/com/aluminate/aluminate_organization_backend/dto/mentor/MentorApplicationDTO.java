package com.aluminate.aluminate_organization_backend.dto.mentor;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Data
@Getter
@Setter
public class MentorApplicationDTO {
    private Long id;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private String applicantAvatar;
    private String currentPosition;
    private String company;
    private int yearsExperience;
    private List<String> expertise;
    private String bio;
    private String motivation;
    private String availability;
    private String preferredMenteeLevel;
    private int maxMentees;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String reviewNotes;
    private String linkedInUrl;
    private String portfolioUrl;
    private Set<String> languages;
    private Set<String> skills;
    private boolean isApproved;
}
