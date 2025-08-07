package com.aluminate.aluminate_organization_backend.service.auth;

import com.aluminate.aluminate_organization_backend.config.util.Jwt;
import com.aluminate.aluminate_organization_backend.dto.login.LoginResponse;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwt jwt;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, Jwt jwt) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    public LoginResponse login(String email, String password,String role) {
        //check if role is 'member' or 'admin'



    }

}
