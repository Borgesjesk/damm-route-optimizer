package com.dammroute.repository;

import com.dammroute.entity.CommercialAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommercialAlertRepository extends JpaRepository<CommercialAlert, Long> {
    List<CommercialAlert> findByStatusOrderByPriorityAsc(String status);
    List<CommercialAlert> findByClientIdOrderByCreatedAtDesc(Long clientId);
    long countByStatus(String status);
}
