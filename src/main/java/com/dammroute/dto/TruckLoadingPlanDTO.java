package com.dammroute.dto;

import com.dammroute.entity.TruckZone;
import java.util.List;
import java.util.Map;

/**
 * Complete loading plan for one truck route.
 * Includes per-zone breakdown and warehouse instructions.
 */
public record TruckLoadingPlanDTO(
    Long routeId,
    String carrierName,
    int totalStops,
    double truckCapacityM3,
    double totalDeliveryVolumeM3,
    double totalReturnVolumeM3,
    double totalUsedVolumeM3,
    double occupancyPercent,

    // Ordered by LOADING sequence (warehouse loads in this order)
    List<CargoSlotDTO> loadingSequence,

    // Summary per zone
    Map<TruckZone, List<CargoSlotDTO>> zoneBreakdown,

    // Warehouse instructions in plain language
    List<String> warehouseInstructions,

    // Sustainability
    double co2SavedPercent,
    String loadingModel   // "CLIENT_ORIENTED" — our recommendation
) {}
