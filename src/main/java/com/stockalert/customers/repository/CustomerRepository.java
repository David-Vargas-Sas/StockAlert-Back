package com.stockalert.customers.repository;

import com.stockalert.customers.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByCompanyId(Long companyId);

    Page<Customer> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Customer> findByIdAndCompanyId(Long id, Long companyId);
}
