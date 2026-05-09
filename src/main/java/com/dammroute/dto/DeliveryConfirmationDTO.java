package com.dammroute.dto;

public record DeliveryConfirmationDTO(
    Long stopId,
    String clientName,
    String deliveredAt,      // timestamp
    String driverName,
    String signatureBase64,  // client signature as base64 image
    String notes,
    boolean hasReturnables,
    int returnablesCollected,
    String status            // DELIVERED / PARTIAL / FAILED
) {}
