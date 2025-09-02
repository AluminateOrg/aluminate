package com.aluminate.aluminate_organization_backend.dto.login;

import com.aluminate.aluminate_organization_backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminDTO implements UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
