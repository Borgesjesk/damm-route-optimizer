package com.dammroute.dto;

import com.dammroute.entity.TruckZone;

/**
 * Represents one client's cargo slot inside the truck.
 * Includes delivery products AND space reserved for empty returns.
 */
public record CargoSlotDTO(
    int stopOrder,           // delivery sequence number
    int loadingOrder,        // warehouse loading sequence (reverse of delivery)
    String clientName,
    String address,
    TruckZone truckZone,    // FRONT / MIDDLE / BACK
    String deliveryWindowStart,
    String deliveryWindowEnd,
    double estimatedVolumeM3,
    double returnVolumeM3,   // space reserved for empty returns
    double totalSlotM3,
    String parkingInstruction,
    String loadingNote       // human-readable instruction for warehouse
) {}
