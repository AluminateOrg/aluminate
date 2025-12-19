package com.aluminate.aluminate_organization_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MemberResponseDTO {
    private String name;
    private String nic;
    @JsonProperty("is_active")
    private boolean isActive;
    private String phone;
    private String email;
    private String regNo;
    private String address;
    private String photoUrl;
    private String password;
    private String degree;
    private String company;
    private String position;
    private String linkedinUrl;
    private String githubUrl;
    private String websiteUrl;
    private String publicSlug;
    private Boolean publicProfileEnabled;
    private String avatarUrl;

    private int batch;
    private List<Long> groupIds;
}
