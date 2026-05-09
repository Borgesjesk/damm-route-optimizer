package com.dammroute.dto;

public record RouteStopDTO(
    Long stopId,
    int stopOrder,
    String clientName,
    String address,
    double latitude,
    double longitude,
    String deliveryWindowStart,
    String deliveryWindowEnd,
    String parkingDifficulty,
    String parkingInstruction,
    String estimatedArrival,
    String status
) {}
