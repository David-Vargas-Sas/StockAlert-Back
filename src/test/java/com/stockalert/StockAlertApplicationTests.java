package com.stockalert;

import com.stockalert.auth.dto.AuthResponseDto;
import com.stockalert.auth.dto.LoginRequestDto;
import com.stockalert.auth.dto.RefreshTokenRequestDto;
import com.stockalert.auth.repository.RefreshTokenRepository;
import com.stockalert.auth.service.AuthService;
import com.stockalert.companies.model.Company;
import com.stockalert.companies.model.CompanyStatus;
import com.stockalert.companies.repository.CompanyRepository;
import com.stockalert.products.controller.ProductController;
import com.stockalert.products.model.Product;
import com.stockalert.products.repository.ProductRepository;
import com.stockalert.products.service.ProductService;
import com.stockalert.users.model.Permission;
import com.stockalert.users.model.Role;
import com.stockalert.users.model.User;
import com.stockalert.users.repository.PermissionRepository;
import com.stockalert.users.repository.RoleRepository;
import com.stockalert.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class StockAlertApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductController productController;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void loginReturnsAccessAndRefreshToken() {
        AuthResponseDto response = authService.login(
                LoginRequestDto.builder().username("superadmin").password("Admin123*").build(),
                request()
        );

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    void refreshReturnsNewAccessToken() {
        AuthResponseDto login = authService.login(
                LoginRequestDto.builder().username("superadmin").password("Admin123*").build(),
                request()
        );

        AuthResponseDto refreshed = authService.refresh(
                RefreshTokenRequestDto.builder().refreshToken(login.getRefreshToken()).build(),
                request()
        );

        assertThat(refreshed.getAccessToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isNotEqualTo(login.getRefreshToken());
    }

    @Test
    void loginClosesPreviousActiveSessionsForSameUser() {
        AuthResponseDto firstLogin = authService.login(
                LoginRequestDto.builder().username("superadmin").password("Admin123*").build(),
                request()
        );

        AuthResponseDto secondLogin = authService.login(
                LoginRequestDto.builder().username("superadmin").password("Admin123*").build(),
                request()
        );

        assertThat(refreshTokenRepository.findByToken(firstLogin.getRefreshToken()).orElseThrow().getRevoked()).isTrue();
        assertThat(refreshTokenRepository.findByToken(secondLogin.getRefreshToken()).orElseThrow().getRevoked()).isFalse();
    }

    @Test
    void userWithoutProductPermissionCannotAccessProducts() {
        Company company = createCompany("Empresa sin productos");
        User user = createUserWithRole(company, "no_products_user", "no_products_user@test.com", "ROLE_ONLY", Set.of());
        authenticate(user);

        assertThatThrownBy(() -> productController.findAll())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminOnlySeesProductsFromOwnCompany() {
        Permission productPermission = permissionRepository.findByName("PRODUCT_READ").orElseThrow();
        Company companyA = createCompany("Empresa A");
        Company companyB = createCompany("Empresa B");
        User adminA = createUserWithRole(companyA, "admin_a", "admin_a@test.com", "ADMIN_A", Set.of(productPermission));

        Product productA = productRepository.save(Product.builder()
                .company(companyA)
                .name("Producto A")
                .price(BigDecimal.TEN)
                .stock(10)
                .minimumStock(2)
                .active(true)
                .createdBy("test")
                .build());
        productRepository.save(Product.builder()
                .company(companyB)
                .name("Producto B")
                .price(BigDecimal.TEN)
                .stock(10)
                .minimumStock(2)
                .active(true)
                .createdBy("test")
                .build());

        authenticate(adminA);

        assertThat(productService.findAll())
                .hasSize(1)
                .first()
                .extracting("id")
                .isEqualTo(productA.getId());
    }

    private Company createCompany(String name) {
        return companyRepository.findByName(name)
                .orElseGet(() -> companyRepository.save(Company.builder()
                        .name(name)
                        .taxId(name.replace(" ", "-").toUpperCase())
                        .status(CompanyStatus.ACTIVE)
                        .createdBy("test")
                        .build()));
    }

    private User createUserWithRole(Company company, String username, String email, String roleName, Set<Permission> permissions) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    Role role = roleRepository.save(Role.builder()
                            .company(company)
                            .name(roleName)
                            .description(roleName)
                            .permissions(permissions)
                            .createdBy("test")
                            .build());
                    return userRepository.save(User.builder()
                            .company(company)
                            .username(username)
                            .email(email)
                            .password(passwordEncoder.encode("Admin123*"))
                            .fullName(username)
                            .active(true)
                            .createdBy("test")
                            .roles(Set.of(role))
                            .build());
                });
    }

    private void authenticate(User user) {
        com.stockalert.security.UserPrincipal principal = new com.stockalert.security.UserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        return request;
    }
}
