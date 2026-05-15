package com.stockalert.users.repository;

import com.stockalert.users.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findByCompanyId(Long companyId);

    Page<Role> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Role> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Role> findByNameAndCompanyId(String name, Long companyId);

    Set<Role> findByIdInAndCompanyId(Set<Long> ids, Long companyId);
}
