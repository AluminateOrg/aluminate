package com.aluminate.aluminate_organization_backend.service;


import com.aluminate.aluminate_organization_backend.repository.AdminRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AdminRepository adminRepo;
    private final MemberRepository memberRepository;

    public CustomUserDetailsService(AdminRepository adminRepo, MemberRepository memberRepository) {
        this.adminRepo = adminRepo;
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return adminRepo.findAdminByEmail(email)
                .<UserDetails>map(admin -> admin)
                .or(() -> memberRepository.findByEmail(email).<UserDetails>
                        map( member -> member))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
