package com.stockalert.shared.config;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.repository.CompanyRepository;
import com.stockalert.users.model.Permission;
import com.stockalert.users.model.Role;
import com.stockalert.users.model.User;
import com.stockalert.users.repository.PermissionRepository;
import com.stockalert.users.repository.RoleRepository;
import com.stockalert.users.repository.UserRepository;
import com.stockalert.users.service.DefaultRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BootstrapDataInitializer implements CommandLineRunner {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    private final CompanyRepository companyRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultRoleService defaultRoleService;

    @Value("${stockalert.bootstrap.company-name}")
    private String companyName;

    @Value("${stockalert.bootstrap.super-admin-username}")
    private String superAdminUsername;

    @Value("${stockalert.bootstrap.super-admin-email}")
    private String superAdminEmail;

    @Value("${stockalert.bootstrap.super-admin-password}")
    private String superAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Company company = companyRepository.findByName(companyName)
                .orElseGet(() -> companyRepository.save(Company.builder()
                        .name(companyName)
                        .taxId("SYSTEM")
                        .active(true)
                        .createdBy("SYSTEM")
                        .build()));

        Set<Permission> permissions = createDefaultPermissions();
        companyRepository.findAll().forEach(existingCompany ->
                defaultRoleService.createDefaultCompanyRoles(existingCompany, "SYSTEM"));

        Role superAdminRole = roleRepository.findByNameAndCompanyId(SUPER_ADMIN_ROLE, company.getId())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .company(company)
                        .name(SUPER_ADMIN_ROLE)
                        .description("Administrador global del sistema")
                        .createdBy("SYSTEM")
                        .permissions(permissions)
                        .build()));

        superAdminRole.getPermissions().addAll(permissions);

        if (!userRepository.existsByUsername(superAdminUsername)) {
            userRepository.save(User.builder()
                    .company(company)
                    .username(superAdminUsername)
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode(superAdminPassword))
                    .fullName("Super Administrador")
                    .active(true)
                    .createdBy("SYSTEM")
                    .roles(Set.of(superAdminRole))
                    .build());
        }
    }

    private Set<Permission> createDefaultPermissions() {
        List<String> names = List.of(
                "COMPANY_READ",
                "COMPANY_CREATE",
                "COMPANY_UPDATE",
                "COMPANY_DELETE",
                "USER_READ",
                "USER_CREATE",
                "USER_UPDATE",
                "USER_DELETE",
                "ROLE_READ",
                "ROLE_CREATE",
                "ROLE_UPDATE",
                "ROLE_DELETE",
                "PERMISSION_READ",
                "PERMISSION_CREATE",
                "PERMISSION_UPDATE",
                "PERMISSION_DELETE",
                "PRODUCT_READ",
                "PRODUCT_CREATE",
                "PRODUCT_UPDATE",
                "PRODUCT_DELETE",
                "SALE_READ",
                "SALE_CREATE",
                "ALERT_READ",
                "ALERT_RESOLVE",
                "SESSION_LOG_READ"
        );

        Set<Permission> permissions = new HashSet<>();
        for (String name : names) {
            Permission permission = permissionRepository.findByName(name)
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .name(name)
                            .description("Permiso " + name)
                            .build()));
            permissions.add(permission);
        }
        return permissions;
    }
}
