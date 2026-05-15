package com.stockalert.users.service;

import com.stockalert.companies.model.Company;
import com.stockalert.users.model.Permission;
import com.stockalert.users.model.Role;
import com.stockalert.users.repository.PermissionRepository;
import com.stockalert.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultRoleService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void createDefaultCompanyRoles(Company company, String createdBy) {
        createRoleIfMissing(company, "ADMINISTRADOR", "Administrador de empresa", createdBy, Set.of(
                "USER_READ",
                "USER_CREATE",
                "USER_UPDATE",
                "ROLE_READ",
                "ROLE_CREATE",
                "ROLE_UPDATE",
                "PRODUCT_READ",
                "PRODUCT_CREATE",
                "PRODUCT_UPDATE",
                "PRODUCT_DELETE",
                "SALE_READ",
                "SALE_CREATE",
                "ALERT_READ",
                "ALERT_RESOLVE",
                "SESSION_LOG_READ"
        ));

        createRoleIfMissing(company, "VENDEDOR", "Usuario de ventas", createdBy, Set.of(
                "PRODUCT_READ",
                "SALE_READ",
                "SALE_CREATE",
                "ALERT_READ"
        ));

        createRoleIfMissing(company, "BODEGUERO", "Usuario de inventario y bodega", createdBy, Set.of(
                "PRODUCT_READ",
                "PRODUCT_CREATE",
                "PRODUCT_UPDATE",
                "PRODUCT_DELETE",
                "ALERT_READ",
                "ALERT_RESOLVE"
        ));

        createRoleIfMissing(company, "CONSULTOR", "Usuario de solo consulta", createdBy, Set.of(
                "PRODUCT_READ",
                "SALE_READ",
                "ALERT_READ"
        ));
    }

    private void createRoleIfMissing(
            Company company,
            String name,
            String description,
            String createdBy,
            Set<String> permissionNames
    ) {
        Role role = roleRepository.findByNameAndCompanyId(name, company.getId())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .company(company)
                        .name(name)
                        .description(description)
                        .createdBy(createdBy)
                        .build()));

        role.getPermissions().addAll(findPermissions(permissionNames));
    }

    private Set<Permission> findPermissions(Set<String> permissionNames) {
        return permissionNames.stream()
                .map(name -> permissionRepository.findByName(name)
                        .orElseThrow(() -> new IllegalStateException("Permiso base no encontrado: " + name)))
                .collect(Collectors.toSet());
    }
}
