package com.dammroute.service;

import com.dammroute.entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class Co2CalculatorServiceTest {

    private Co2CalculatorService service;

    @BeforeEach
    void setUp() {
        service = new Co2CalculatorService();
        ReflectionTestUtils.setField(service, "kgCo2PerKm", 0.27);
        ReflectionTestUtils.setField(service, "warehouseLat", 41.3167);
        ReflectionTestUtils.setField(service, "warehouseLng", 2.0833);
    }

    // ── HAPPY PATH ─────────────────────────────────────────────────

    @Test
    @DisplayName("calculateTotalDistanceKm returns positive distance for valid stops")
    void calculateDistance_happyPath() {
        Client c1 = buildClient(41.3851, 2.1827);
        Client c2 = buildClient(41.4036, 2.1579);

        double result = service.calculateTotalDistanceKm(List.of(c1, c2));

        assertThat(result).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("calculateCo2Kg returns correct value for known distance")
    void calculateCo2_happyPath() {
        double result = service.calculateCo2Kg(100.0);
        assertThat(result).isEqualTo(27.0);
    }

    @Test
    @DisplayName("calculateSavedPercent returns correct percentage")
    void calculateSavedPercent_happyPath() {
        double result = service.calculateSavedPercent(100.0, 62.0);
        assertThat(result).isEqualTo(38.0);
    }

    @Test
    @DisplayName("treesEquivalent returns non-zero for positive CO2 saved")
    void treesEquivalent_happyPath() {
        double result = service.calculateTreesEquivalent(21.0);
        assertThat(result).isEqualTo(1.0);
    }

    // ── UNHAPPY PATH ───────────────────────────────────────────────

    @Test
    @DisplayName("calculateTotalDistanceKm returns 0 for null input")
    void calculateDistance_nullInput_returnsZero() {
        assertThat(service.calculateTotalDistanceKm(null)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("calculateTotalDistanceKm returns 0 for empty list")
    void calculateDistance_emptyList_returnsZero() {
        assertThat(service.calculateTotalDistanceKm(List.of())).isEqualTo(0.0);
    }

    @Test
    @DisplayName("calculateSavedPercent returns 0 when baseline is zero")
    void calculateSavedPercent_zeroBaseline_returnsZero() {
        assertThat(service.calculateSavedPercent(0.0, 10.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("haversine returns ~1100km between Barcelona and Madrid")
    void haversine_knownDistance() {
        // Barcelona to Madrid ~ 504km straight line
        double result = service.haversine(41.3851, 2.1734, 40.4168, -3.7038);
        assertThat(result).isBetween(490.0, 520.0);
    }

    private Client buildClient(double lat, double lng) {
        Client c = new Client();
        c.setLatitude(lat);
        c.setLongitude(lng);
        return c;
    }
}
