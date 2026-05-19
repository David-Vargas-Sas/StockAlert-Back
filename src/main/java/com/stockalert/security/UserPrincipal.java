package com.stockalert.security;

import com.stockalert.users.model.Permission;
import com.stockalert.users.model.Role;
import com.stockalert.users.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final Long companyId;
    private final String username;
    private final String password;
    private final Boolean active;
    private final Set<GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.companyId = user.getCompany().getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.active = user.getActive();
        this.authorities = buildAuthorities(user);
    }

    private Set<GrantedAuthority> buildAuthorities(User user) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            if (!Boolean.TRUE.equals(role.getActive())) {
                continue;
            }
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission permission : role.getPermissions()) {
                grantedAuthorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        return grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(active);
    }
}
