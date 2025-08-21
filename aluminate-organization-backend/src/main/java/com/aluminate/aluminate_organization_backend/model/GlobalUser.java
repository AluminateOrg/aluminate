package com.aluminate.aluminate_organization_backend.model;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@MappedSuperclass
@Getter
@Setter
public abstract class GlobalUser implements UserDetails {
    private String email;
    private String password;

    // UserDetails requires getUsername()
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }


    // Abstract method — each subclass must implement
    @Override
    public abstract Collection<? extends GrantedAuthority> getAuthorities();

    // Default implementations
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

