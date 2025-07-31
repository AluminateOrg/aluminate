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
    private int yearsOfExperience;
    private BigDecimal hourlyRate;
    private String bio;
    private String linkedInUrl;
    private String portfolioUrl;
    private String motivation;
    private Set<String> languages;
    private String preferredMenteeLevel;
    private int maxMentees;
}
