package com.dammroute.repository;

import com.dammroute.entity.DamageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DamageReportRepository extends JpaRepository<DamageReport, Long> {
    List<DamageReport> findByStatus(String status);
    List<DamageReport> findByRouteStopId(Long stopId);
    long countByStatus(String status);
}
