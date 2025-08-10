package com.aluminate.aluminate_organization_backend.dto.login;

import com.aluminate.aluminate_organization_backend.dto.MemberDTO;
import com.aluminate.aluminate_organization_backend.model.Member;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Setter
public class LoginResponse {
    private String token;
    private UserDTO user;

}
