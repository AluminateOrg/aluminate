package com.aluminate.aluminate_organization_backend.dto.mentor;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Getter
@Setter
public class MentorRequestDTO {
    private Long memberId;
    private int yearsExperience;
    private BigDecimal hourlyRate;
    private String bio;
    private String linkedinUrl;
    private String availability;
    private String portfolioUrl;
    private String motivation;
    private Set<String> languages;
    private Set<String> skills;
    private String preferredMenteeLevel;
    private int maxMentees;
}
