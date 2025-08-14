package com.aluminate.aluminate_organization_backend.dto.login;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalAuthRequest {
    private String email;
    private String password;
}
