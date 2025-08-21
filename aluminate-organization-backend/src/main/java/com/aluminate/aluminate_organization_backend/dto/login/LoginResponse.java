package com.aluminate.aluminate_organization_backend.dto.login;

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
