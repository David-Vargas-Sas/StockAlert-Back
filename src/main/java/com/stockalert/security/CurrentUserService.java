package com.stockalert.security;

import com.stockalert.shared.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public Long getUserId() {
        return getPrincipal().getId();
    }

    public Long getCompanyId() {
        return getPrincipal().getCompanyId();
    }

    public boolean hasRole(String roleName) {
        String authority = "ROLE_" + roleName;
        Authentication authentication = getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    public void validateSameCompanyOrSuperAdmin(Long companyId) {
        if (!hasRole("SUPER_ADMIN") && !getCompanyId().equals(companyId)) {
            throw new BusinessException("No puedes gestionar informacion de otra empresa");
        }
    }

    private UserPrincipal getPrincipal() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("No hay un usuario autenticado");
        }
        return principal;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
