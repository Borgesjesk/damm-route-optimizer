package com.dammroute.dto;

import java.util.List;
import java.util.Map;

public record WarehouseSheetDTO(
    Long routeId,
    String carrierName,
    String vehiclePlate,
    String routeDate,
    int totalClients,
    int totalItems,
    int totalReturnableItems,

    // Ordered by warehouse walk path (aisle order)
    List<WarehousePickItemDTO> pickListByAisle,

    // Grouped by client (for staging)
    Map<String, List<WarehousePickItemDTO>> pickListByClient,

    // Grouped by truck zone (for loading)
    Map<String, List<WarehousePickItemDTO>> pickListByZone,

    // Time estimate
    int estimatedLoadingMinutes,
    String loadingModel,
    List<String> warehouseInstructions
) {}
