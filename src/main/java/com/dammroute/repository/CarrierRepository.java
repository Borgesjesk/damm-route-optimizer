package com.dammroute.repository;

import com.dammroute.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    Optional<Carrier> findByLicenseNumber(String licenseNumber);
    List<Carrier> findByTrustLevelAndActiveTrue(String trustLevel);
    List<Carrier> findByActiveTrue();
}
