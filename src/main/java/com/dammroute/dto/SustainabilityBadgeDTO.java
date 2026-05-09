package com.dammroute.dto;

public record SustainabilityBadgeDTO(
    Long routeId,
    String carrierName,
    int totalStops,
    double co2SavedKg,
    double co2SavedPercent,
    double treesEquivalent,
    double totalDistanceKm,
    double distanceSavedKm,
    String badgeLevel,      // BRONZE / SILVER / GOLD / PLATINUM
    String shareMessage,    // Ready to post on social media
    String hashtags
) {}
