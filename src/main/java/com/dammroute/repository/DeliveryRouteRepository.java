package com.dammroute.repository;

import com.dammroute.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {
    List<DeliveryRoute> findByStatus(String status);

    @Query("SELECT COALESCE(SUM(r.co2SavedPercent), 0) FROM DeliveryRoute r WHERE r.status = 'COMPLETED'")
    double sumCo2SavedToday();

    long countByStatus(String status);
}
