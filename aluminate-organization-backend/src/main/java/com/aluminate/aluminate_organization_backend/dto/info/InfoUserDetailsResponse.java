package com.aluminate.aluminate_organization_backend.dto.info;


import com.aluminate.aluminate_organization_backend.dto.login.UserDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InfoUserDetailsResponse {
    private UserDTO user;
}
