package com.stockalert.products.repository;

import com.stockalert.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p where p.stock <= p.minimumStock")
    List<Product> findLowStockProducts();

    List<Product> findByCompanyId(Long companyId);

    Page<Product> findByCompanyId(Long companyId, Pageable pageable);

    java.util.Optional<Product> findByIdAndCompanyId(Long id, Long companyId);

    @Query("select p from Product p where p.company.id = :companyId and p.stock <= p.minimumStock")
    List<Product> findLowStockProductsByCompanyId(@Param("companyId") Long companyId);
}
