package com.dammroute.dto;

public record DamageReportDTO(
    Long reportId,
    Long stopId,
    String clientName,
    String productReference,
    String productName,
    int quantityAffected,
    String damageType,       // DELIVERY_DAMAGED / RETURN_REJECTED / MISSING
    String notes,
    String reportedAt,
    String assignedTo,       // SUPERVISOR / WAREHOUSE / COMMERCIAL
    String status            // OPEN / RESOLVED
) {}
