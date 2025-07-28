package com.aluminate.aluminate_organization_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MemberResponseDTO {
    private String name;
    private String nic;
    private boolean is_active;
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
    private int batch;
    private List<Long> groupIds;
}
