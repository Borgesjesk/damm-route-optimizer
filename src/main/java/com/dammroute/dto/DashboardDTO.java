package com.dammroute.dto;

public record DashboardDTO(
    long activeRoutes,
    long completedDeliveries,
    double co2SavedTotalKg,
    long trustedCarriers,
    long blockedCarriers,
    long totalClients
) {}
