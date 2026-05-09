package com.dammroute.service;

import com.dammroute.dto.CarrierDTO;
import com.dammroute.entity.Carrier;
import com.dammroute.exception.BusinessRuleException;
import com.dammroute.exception.ResourceNotFoundException;
import com.dammroute.repository.CarrierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarrierTrustServiceTest {

    @Mock CarrierRepository carrierRepository;

    private CarrierTrustService service;

    @BeforeEach
    void setUp() {
        service = new CarrierTrustService(carrierRepository);
    }

    // ── HAPPY PATH ─────────────────────────────────────────────────

    @Test
    @DisplayName("createCarrier with verified docs and no disputes = TRUSTED score 100")
    void createCarrier_perfectRecord_trustedScore() {
        when(carrierRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarrierDTO dto = new CarrierDTO("TransBarcelona", "B1234567", true, 0);
        Carrier result = service.createCarrier(dto);

        assertThat(result.getTrustScore()).isEqualTo(100);
        assertThat(result.getTrustLevel()).isEqualTo("TRUSTED");
    }

    @Test
    @DisplayName("createCarrier without verified docs = score 60, WARNING")
    void createCarrier_unverifiedDocs_warningScore() {
        when(carrierRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarrierDTO dto = new CarrierDTO("Risky SL", "B9999999", false, 0);
        Carrier result = service.createCarrier(dto);

        assertThat(result.getTrustScore()).isEqualTo(60);
        assertThat(result.getTrustLevel()).isEqualTo("WARNING");
    }

    @Test
    @DisplayName("createCarrier unverified + 3 disputes = score 30, BLOCKED")
    void createCarrier_blockedCarrier() {
        when(carrierRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarrierDTO dto = new CarrierDTO("Fraud SL", "B0000000", false, 3);
        Carrier result = service.createCarrier(dto);

        assertThat(result.getTrustScore()).isLessThan(50);
        assertThat(result.getTrustLevel()).isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("validateCarrierForRoute passes for TRUSTED carrier")
    void validateCarrier_trusted_passes() {
        Carrier trusted = buildCarrier(92, "TRUSTED", true, 0);
        when(carrierRepository.findById(1L)).thenReturn(Optional.of(trusted));

        assertThatCode(() -> service.validateCarrierForRoute(1L))
                .doesNotThrowAnyException();
    }

    // ── UNHAPPY PATH ───────────────────────────────────────────────

    @Test
    @DisplayName("validateCarrierForRoute throws for BLOCKED carrier")
    void validateCarrier_blocked_throwsBusinessRule() {
        Carrier blocked = buildCarrier(20, "BLOCKED", false, 5);
        when(carrierRepository.findById(1L)).thenReturn(Optional.of(blocked));

        assertThatThrownBy(() -> service.validateCarrierForRoute(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("blocked by FraudSentinel");
    }

    @Test
    @DisplayName("validateCarrierForRoute throws for null carrier ID")
    void validateCarrier_nullId_throwsIllegalArgument() {
        assertThatThrownBy(() -> service.validateCarrierForRoute(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("validateCarrierForRoute throws for non-existent carrier")
    void validateCarrier_notFound_throwsResourceNotFound() {
        when(carrierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateCarrierForRoute(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createCarrier throws for null DTO")
    void createCarrier_nullDto_throwsIllegalArgument() {
        assertThatThrownBy(() -> service.createCarrier(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Carrier buildCarrier(int score, String level,
                                  boolean verified, int disputes) {
        Carrier c = new Carrier();
        c.setTrustScore(score);
        c.setTrustLevel(level);
        c.setDocumentVerified(verified);
        c.setDisputeCount(disputes);
        c.setActive(true);
        return c;
    }
}
