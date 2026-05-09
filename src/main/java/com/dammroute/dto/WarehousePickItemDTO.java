package com.dammroute.dto;

public record WarehousePickItemDTO(
    String aisleCode,        // AA01A1, BA03A3, DA01 etc
    String productCode,      // ED13, VO13, VE12SP etc
    String productName,
    int quantity,
    String unit,             // Caja, Barril, Unidad
    boolean isReturnable,
    String clientName,
    int stopOrder,
    String truckZone,        // FRONT / MIDDLE / BACK
    String loadingNote
) {}
