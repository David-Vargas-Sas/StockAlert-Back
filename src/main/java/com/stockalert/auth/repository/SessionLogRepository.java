package com.stockalert.auth.repository;

import com.stockalert.auth.model.SessionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {

    Page<SessionLog> findByCompanyId(Long companyId, Pageable pageable);
}
