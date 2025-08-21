package com.aluminate.aluminate_organization_backend.dto;

import com.aluminate.aluminate_organization_backend.dto.login.UserDTO;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class MemberDTO implements UserDTO{
    private Long id;
    private String name;
    private String nic;
    private String phone;
    private String email;
    private String regNo;
    private String address;
    private String photoUrl;
    private String degree;
    private String company;
    private String position;
    private String linkedinUrl;
    private String githubUrl;
    private String websiteUrl;
    private int batch;

}
