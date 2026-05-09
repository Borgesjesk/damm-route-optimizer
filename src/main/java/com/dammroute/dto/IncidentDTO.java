package com.dammroute.dto;

public record IncidentDTO(
    Long incidentId,
    Long routeId,
    String incidentType,     // DRIVER_SICK / TRUCK_BREAKDOWN / ACCIDENT / DELAY
    String description,
    String reportedAt,
    String affectedRoute,
    String suggestedAction,  // AUTO_REASSIGN / MANUAL_REVIEW / CANCEL
    String alternativeCarrier,
    String status            // OPEN / RESOLVED / ESCALATED
) {}
