package com.stockalert.sales.repository;

import com.stockalert.sales.model.SaleDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {

    @Query("""
            select d.product.id as productId,
                   d.product.name as productName,
                   sum(d.quantity) as quantitySold,
                   sum(d.subtotal) as totalSold
            from SaleDetail d
            where d.company.id = :companyId
              and d.sale.status = com.stockalert.sales.model.SaleStatus.ACTIVE
            group by d.product.id, d.product.name
            order by sum(d.quantity) desc
            """)
    List<TopProductProjection> findTopProductsByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
