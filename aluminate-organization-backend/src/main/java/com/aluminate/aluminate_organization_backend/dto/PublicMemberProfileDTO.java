package com.aluminate.aluminate_organization_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicMemberProfileDTO {
    private String name;
    private String position;
    private String company;
    private Integer batch;
    private String degree;
    private String linkedinUrl;
    private String githubUrl;
    private String websiteUrl;
    private String photoUrl;   // or avatar
}
