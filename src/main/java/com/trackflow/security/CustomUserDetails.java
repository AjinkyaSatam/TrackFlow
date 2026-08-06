package com.trackflow.security;

import com.trackflow.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom implementation of {@link UserDetails} wrapping our database {@link User} entity.
 *
 * <p>This acts as an Adapter pattern, connecting Spring Security's authorization model
 * with our custom database User schema.</p>
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Converts our custom Role enum into Spring Security's SimpleGrantedAuthority.
     * We prefix the role with "ROLE_" (e.g. ROLE_DEVELOPER) which is the default
     * convention Spring Security expects for role-based authorization.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * We use email as the unique username in our system.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // We don't implement expiration policy
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    /**
     * Helper to retrieve the underlying user entity details.
     */
    public User getUser() {
        return user;
    }
}
