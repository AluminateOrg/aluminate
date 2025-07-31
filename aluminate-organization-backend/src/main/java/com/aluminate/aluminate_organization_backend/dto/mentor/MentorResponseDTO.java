package com.aluminate.aluminate_organization_backend.dto.mentor;

import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Builder
public class MentorResponseDTO {
    private Long id;
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
    private boolean isApproved;
    private Double rating;
    private int sessionCount;


}
