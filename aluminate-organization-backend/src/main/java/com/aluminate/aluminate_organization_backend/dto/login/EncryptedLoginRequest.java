package com.aluminate.aluminate_organization_backend.dto.login;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EncryptedLoginRequest {
    private String payload;
}
