package com.aluminate.aluminate_organization_backend.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Admin extends GlobalUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String nic;
    private String phone;
    private String password;

    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private Organization organization;

    // UserDetails interface methods below

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;  // usually username is email or unique login id
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // add logic here if you want
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // add logic here if you want
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // add logic here if you want
    }

    @Override
    public boolean isEnabled() {
        return true; // add logic here if you want
    }
}

