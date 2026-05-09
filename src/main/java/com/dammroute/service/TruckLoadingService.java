package com.dammroute.service;

import com.dammroute.dto.CargoSlotDTO;
import com.dammroute.dto.TruckLoadingPlanDTO;
import com.dammroute.entity.DeliveryRoute;
import com.dammroute.entity.RouteStop;
import com.dammroute.entity.TruckZone;
import com.dammroute.exception.ResourceNotFoundException;
import com.dammroute.repository.DeliveryRouteRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TruckLoadingService — Damm Smart Truck
 *
 * Solves the cargo loading problem:
 * Given an ordered delivery route, determine:
 * 1. Physical loading order (reverse of delivery)
 * 2. Truck zone assignment (FRONT/MIDDLE/BACK)
 * 3. Space allocation per client (delivery + returns)
 * 4. Warehouse instructions in plain language
 *
 * Key insight: Last delivery stop = loaded first (deepest in truck)
 *              First delivery stop = loaded last (easiest to reach)
 *
 * Damm context:
 * - ~60% of products are returnable (empties collected on route)
 * - Lateral tarpaulin access (not rear-door)
 * - 15-25 clients per truck
 * - Truck capacity ~40m³ standard DDI truck
 */
@Service
public class TruckLoadingService {

    // Standard DDI truck capacity (m³)
    private static final double TRUCK_CAPACITY_M3 = 40.0;

    // Average delivery volume per client stop (m³)
    // Based on typical Damm hospitality client order
    private static final double AVG_DELIVERY_VOLUME_M3 = 1.2;

    // 60% of products are returnable — reserve this fraction for empties
    private static final double RETURN_RATIO = 0.6;

    // Return products take ~70% of original volume (crushed/stacked)
    private static final double RETURN_VOLUME_FACTOR = 0.7;

    private final DeliveryRouteRepository routeRepository;

    public TruckLoadingService(DeliveryRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Generate complete loading plan for a route.
     * Guard: throws if route not found or has no stops.
     */
    public TruckLoadingPlanDTO generateLoadingPlan(Long routeId) {
        if (routeId == null) {
            throw new IllegalArgumentException("Route ID must not be null");
        }

        DeliveryRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", routeId));

        List<RouteStop> stops = route.getStops();
        if (stops == null || stops.isEmpty()) {
            throw new IllegalStateException(
                "Route " + routeId + " has no stops — cannot generate loading plan");
        }

        // Sort stops by delivery order
        List<RouteStop> orderedStops = stops.stream()
                .sorted(Comparator.comparingInt(RouteStop::getStopOrder))
                .collect(Collectors.toList());

        int totalStops = orderedStops.size();

        // Build cargo slots — loading order is REVERSE of delivery order
        List<CargoSlotDTO> slots = buildCargoSlots(orderedStops, totalStops);

        // Loading sequence = sorted by loadingOrder (warehouse loads in this order)
        List<CargoSlotDTO> loadingSequence = slots.stream()
                .sorted(Comparator.comparingInt(CargoSlotDTO::loadingOrder))
                .collect(Collectors.toList());

        // Zone breakdown
        Map<TruckZone, List<CargoSlotDTO>> zoneBreakdown = slots.stream()
                .collect(Collectors.groupingBy(CargoSlotDTO::truckZone));

        // Volume calculations
        double totalDelivery = slots.stream()
                .mapToDouble(CargoSlotDTO::estimatedVolumeM3).sum();
        double totalReturn = slots.stream()
                .mapToDouble(CargoSlotDTO::returnVolumeM3).sum();
        double totalUsed = totalDelivery + totalReturn;
        double occupancy = Math.min(100.0,
                Math.round((totalUsed / TRUCK_CAPACITY_M3) * 1000.0) / 10.0);

        // Generate warehouse instructions
        List<String> instructions = generateWarehouseInstructions(loadingSequence);

        return new TruckLoadingPlanDTO(
                routeId,
                route.getCarrier().getName(),
                totalStops,
                TRUCK_CAPACITY_M3,
                Math.round(totalDelivery * 100.0) / 100.0,
                Math.round(totalReturn * 100.0) / 100.0,
                Math.round(totalUsed * 100.0) / 100.0,
                occupancy,
                loadingSequence,
                zoneBreakdown,
                instructions,
                route.getCo2SavedPercent(),
                "CLIENT_ORIENTED"
        );
    }

    // ── Private helpers ────────────────────────────────────────────────

    private List<CargoSlotDTO> buildCargoSlots(List<RouteStop> orderedStops,
                                                int totalStops) {
        List<CargoSlotDTO> slots = new ArrayList<>();

        for (RouteStop stop : orderedStops) {
            int stopOrder = stop.getStopOrder();

            // Loading order = reverse of delivery order
            // Stop 1 delivered first → loaded LAST (loadingOrder = totalStops)
            // Stop N delivered last → loaded FIRST (loadingOrder = 1)
            int loadingOrder = (totalStops + 1) - stopOrder;

            TruckZone zone = assignZone(stopOrder, totalStops);

            double deliveryVol = calculateDeliveryVolume(stopOrder);
            double returnVol = calculateReturnVolume(deliveryVol);
            double totalVol = deliveryVol + returnVol;

            String loadingNote = buildLoadingNote(stop, loadingOrder, zone);

            slots.add(new CargoSlotDTO(
                    stopOrder,
                    loadingOrder,
                    stop.getClient().getName(),
                    stop.getClient().getAddress(),
                    zone,
                    stop.getClient().getDeliveryWindowStart(),
                    stop.getClient().getDeliveryWindowEnd(),
                    Math.round(deliveryVol * 100.0) / 100.0,
                    Math.round(returnVol * 100.0) / 100.0,
                    Math.round(totalVol * 100.0) / 100.0,
                    stop.getParkingInstruction(),
                    loadingNote
            ));
        }
        return slots;
    }

    /**
     * Assigns truck zone based on stop position in route.
     * BACK = early stops in loading (late stops in delivery)
     * FRONT = late stops in loading (early stops in delivery)
     */
    private TruckZone assignZone(int stopOrder, int totalStops) {
        double position = (double) stopOrder / totalStops;
        if (position <= 0.33) return TruckZone.FRONT;   // first third of deliveries
        if (position <= 0.66) return TruckZone.MIDDLE;  // middle third
        return TruckZone.BACK;                           // last third of deliveries
    }

    /**
     * Estimate delivery volume per client.
     * In production: use actual order data from Damm's system.
     * For demo: use average with slight variation by stop number.
     */
    private double calculateDeliveryVolume(int stopOrder) {
        // Slight variation — early stops tend to be larger clients
        double factor = 1.0 + (0.3 * (1.0 - (double) stopOrder / 25));
        return AVG_DELIVERY_VOLUME_M3 * factor;
    }

    /**
     * 60% of Damm products are returnable.
     * Empties take ~70% of original volume when stacked.
     */
    private double calculateReturnVolume(double deliveryVolume) {
        return deliveryVolume * RETURN_RATIO * RETURN_VOLUME_FACTOR;
    }

    /**
     * Human-readable instruction for warehouse team.
     */
    private String buildLoadingNote(RouteStop stop, int loadingOrder,
                                    TruckZone zone) {
        return String.format(
            "LOAD #%d → %s ZONE | Client: %s | Window: %s–%s | Reserve return space",
            loadingOrder,
            zone.name(),
            stop.getClient().getName(),
            stop.getClient().getDeliveryWindowStart(),
            stop.getClient().getDeliveryWindowEnd()
        );
    }

    /**
     * Generate plain-language warehouse instructions.
     * These are printed and given to the warehouse team.
     */
    private List<String> generateWarehouseInstructions(
            List<CargoSlotDTO> loadingSequence) {

        List<String> instructions = new ArrayList<>();
        instructions.add("=== DAMM DDI LOADING INSTRUCTIONS ===");
        instructions.add("Loading model: CLIENT_ORIENTED (optimised by DammRoute)");
        instructions.add("Load in this exact order — do NOT mix client products:");
        instructions.add("");

        for (CargoSlotDTO slot : loadingSequence) {
            instructions.add(String.format(
                "[%d] %s → %s zone | Delivery stop #%d | Window %s–%s",
                slot.loadingOrder(),
                slot.clientName(),
                slot.truckZone().name(),
                slot.stopOrder(),
                slot.deliveryWindowStart(),
                slot.deliveryWindowEnd()
            ));
        }

        instructions.add("");
        instructions.add("REMINDER: Reserve 60% of each client's space for empty returns.");
        instructions.add("DO NOT load by product reference — load by client.");
        return instructions;
    }
}
