package com.dammroute.dto;

import java.util.List;

public record RouteResponseDTO(
    Long routeId,
    String carrierName,
    int carrierTrustScore,
    String carrierTrustLevel,
    String status,
    double totalDistanceKm,
    double baselineCo2Kg,
    double optimisedCo2Kg,
    double co2SavedPercent,
    double treesEquivalent,
    List<RouteStopDTO> stops
) {}
