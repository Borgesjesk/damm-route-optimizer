package com.dammroute.dto;

public record CommercialAlertDTO(
    Long alertId,
    String clientName,
    String address,
    String alertType,
    String priority,
    String message,
    String recommendation,
    String assignedChannel,
    double expectedRevenueEur,
    double conversionProbability,
    int urgencyDays,
    String status
) {}
