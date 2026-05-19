package com.stockalert.users.repository;

import com.stockalert.users.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"company", "roles", "roles.permissions"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"company", "roles", "roles.permissions"})
    Optional<User> findByIdAndCompanyId(Long id, Long companyId);

    @EntityGraph(attributePaths = {"company", "roles", "roles.permissions"})
    List<User> findByCompanyId(Long companyId);

    @EntityGraph(attributePaths = {"company", "roles", "roles.permissions"})
    Page<User> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByCompanyIdAndRoles_Name(Long companyId, String roleName);

    boolean existsByRoles_Id(Long roleId);
}
