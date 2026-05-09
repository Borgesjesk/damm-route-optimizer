package com.dammroute.service;

import com.dammroute.dto.CarrierDTO;
import com.dammroute.entity.Carrier;
import com.dammroute.exception.BusinessRuleException;
import com.dammroute.exception.ResourceNotFoundException;
import com.dammroute.repository.CarrierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FraudSentinel Trust Score logic.
 *
 * Score calculation:
 * - Base: 100
 * - documentVerified = false:  -40
 * - identityFlagged = true:    -30
 * - each dispute:              -10 (max -30)
 *
 * Levels:
 * - TRUSTED:  score >= 80
 * - WARNING:  score 50–79
 * - BLOCKED:  score < 50
 */
@Service
public class CarrierTrustService {

    private static final int MIN_TRUST_SCORE_FOR_ROUTE = 50;

    private final CarrierRepository carrierRepository;

    public CarrierTrustService(CarrierRepository carrierRepository) {
        this.carrierRepository = carrierRepository;
    }

    public Carrier createCarrier(CarrierDTO dto) {
        if (dto == null) throw new IllegalArgumentException("CarrierDTO must not be null");

        int score = calculateTrustScore(dto.documentVerified(),
                                        dto.disputeCount(), false);
        String level = determineTrustLevel(score);

        Carrier carrier = Carrier.builder()
                .name(dto.name().strip())
                .licenseNumber(dto.licenseNumber().strip().toUpperCase())
                .documentVerified(dto.documentVerified())
                .disputeCount(dto.disputeCount())
                .identityFlagged(false)
                .trustScore(score)
                .trustLevel(level)
                .active(true)
                .build();

        return carrierRepository.save(carrier);
    }

    /**
     * Guard: throws BusinessRuleException if carrier is blocked.
     * Call this BEFORE assigning carrier to any route.
     */
    public void validateCarrierForRoute(Long carrierId) {
        if (carrierId == null) throw new IllegalArgumentException("Carrier ID must not be null");

        Carrier carrier = carrierRepository.findById(carrierId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier", carrierId));

        if (!carrier.isActive()) {
            throw new BusinessRuleException(
                "Carrier is inactive and cannot be assigned to routes");
        }

        if (carrier.getTrustScore() < MIN_TRUST_SCORE_FOR_ROUTE) {
            throw new BusinessRuleException(
                String.format(
                    "Carrier '%s' has trust score %d/100 (minimum %d required). " +
                    "Level: %s. This carrier is blocked by FraudSentinel.",
                    carrier.getName(),
                    carrier.getTrustScore(),
                    MIN_TRUST_SCORE_FOR_ROUTE,
                    carrier.getTrustLevel()
                )
            );
        }
    }

    public Carrier getById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID must not be null");
        return carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier", id));
    }

    public List<Carrier> getAllActive() {
        return carrierRepository.findByActiveTrue();
    }

    private int calculateTrustScore(boolean documentVerified,
                                    int disputeCount,
                                    boolean identityFlagged) {
        int score = 100;
        if (!documentVerified) score -= 40;
        if (identityFlagged)   score -= 30;
        score -= Math.min(disputeCount * 10, 30);
        return Math.max(0, score);
    }

    private String determineTrustLevel(int score) {
        if (score >= 80) return "TRUSTED";
        if (score >= 50) return "WARNING";
        return "BLOCKED";
    }
}
