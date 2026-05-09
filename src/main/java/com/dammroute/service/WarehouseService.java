package com.dammroute.service;

import com.dammroute.dto.*;
import com.dammroute.entity.*;
import com.dammroute.exception.ResourceNotFoundException;
import com.dammroute.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WarehouseService — DDI Mollet del Vallès
 *
 * Replaces the "old guy with paper" system.
 *
 * Generates:
 * 1. Pick list ordered by warehouse aisle (AA→AC→BA→CA→DA→FA→ZCG)
 *    → Warehouse worker walks once, picks everything
 * 2. Staging list grouped by client
 *    → 1 pallet per client, not per product reference
 * 3. Loading list grouped by truck zone
 *    → FRONT loaded last, BACK loaded first
 *
 * Key insight from Damm engineer:
 * - Same product pallet: 1 min
 * - Mixed product pallet: 12 min
 * - CLIENT_ORIENTED model reduces 12→1 min per client stop
 */
@Service
public class WarehouseService {

    // Warehouse aisle order — walk path optimised
    // Based on Layout_Mollet.xlsx and Hoja de Carga location codes
    private static final List<String> AISLE_ORDER = List.of(
        "AA", "AB", "AC", "AD", "AE",  // Main beer aisle (Estrella, Voll, Free)
        "BA", "BB", "BC", "BD",         // Water, gaseosa aisle
        "CA", "CB", "CC",               // Cans, latas aisle
        "DA", "DB",                     // Damm bulk (DA01 = main Estrella)
        "EA", "EB", "EC",               // Wine, spirits
        "FA", "FB", "FC",               // Food, accessories, coffee
        "ZCG", "ZCB"                    // Dry goods, special items
    );

    // Returnable product codes (from Hoja de Carga)
    private static final Set<String> RETURNABLE_CODES = Set.of(
        "CJ13", "CJ15", "CJ11V",
        "BRL30V", "BRL20V",
        "3ENV0041", "3ENV0042", "3ENV0044", "3ENV0060",
        "3ENV0236", "3ENV0029", "3ENV0021", "3ENV1281",
        "TB8V"
    );

    private final DeliveryRouteRepository routeRepository;
    private final DamageReportRepository damageReportRepository;
    private final RouteStopRepository stopRepository;

    public WarehouseService(DeliveryRouteRepository routeRepository,
                            DamageReportRepository damageReportRepository,
                            RouteStopRepository stopRepository) {
        this.routeRepository = routeRepository;
        this.damageReportRepository = damageReportRepository;
        this.stopRepository = stopRepository;
    }

    // ── 1. WAREHOUSE LOADING SHEET ────────────────────────────────────

    /**
     * Generate complete warehouse loading sheet for a route.
     * Replaces paper system. Ready for iPad display.
     */
    @Transactional(readOnly = true)
    public WarehouseSheetDTO generateWarehouseSheet(Long routeId) {
        if (routeId == null) throw new IllegalArgumentException("Route ID must not be null");

        DeliveryRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", routeId));

        List<RouteStop> stops = route.getStops().stream()
                .sorted(Comparator.comparingInt(RouteStop::getStopOrder))
                .collect(Collectors.toList());

        // Build pick items from real Hoja de Carga product structure
        List<WarehousePickItemDTO> allItems = buildPickItems(stops);

        // Sort by aisle order (warehouse walk path)
        List<WarehousePickItemDTO> byAisle = sortByAisle(allItems);

        // Group by client for staging
        Map<String, List<WarehousePickItemDTO>> byClient = allItems.stream()
                .collect(Collectors.groupingBy(WarehousePickItemDTO::clientName));

        // Group by truck zone for loading
        Map<String, List<WarehousePickItemDTO>> byZone = allItems.stream()
                .collect(Collectors.groupingBy(WarehousePickItemDTO::truckZone));

        int totalItems = allItems.size();
        int returnableItems = (int) allItems.stream()
                .filter(WarehousePickItemDTO::isReturnable).count();

        // Loading time estimate (engineer data: 1 min same, 12 min mixed)
        // CLIENT_ORIENTED: ~2 min per client avg
        int estimatedMinutes = stops.size() * 2;

        List<String> instructions = buildWarehouseInstructions(stops, byZone);

        return new WarehouseSheetDTO(
                routeId,
                route.getCarrier().getName(),
                "DDI-" + routeId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                stops.size(),
                totalItems,
                returnableItems,
                byAisle,
                byClient,
                byZone,
                estimatedMinutes,
                "CLIENT_ORIENTED",
                instructions
        );
    }

    // ── 2. DELIVERY CONFIRMATION ──────────────────────────────────────

    /**
     * Driver confirms delivery with optional client signature.
     * Replaces paper albaran.
     */
    @Transactional
    public DeliveryConfirmationDTO confirmDelivery(Long routeId, Long stopId,
                                                    String signatureBase64,
                                                    String notes,
                                                    int returnablesCollected) {
        if (routeId == null || stopId == null) {
            throw new IllegalArgumentException("Route ID and Stop ID must not be null");
        }

        RouteStop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop", stopId));

        stop.setStatus("DELIVERED");
        stop.setDeliveredAt(LocalDateTime.now());
        stop.setSignatureBase64(signatureBase64);
        stop.setDeliveryNotes(notes);
        stop.setReturnablesCollected(returnablesCollected);
        stop.setHasReturnables(returnablesCollected > 0);

        stopRepository.save(stop);

        return new DeliveryConfirmationDTO(
                stopId,
                stop.getClient().getName(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                "Driver",
                signatureBase64,
                notes,
                returnablesCollected > 0,
                returnablesCollected,
                "DELIVERED"
        );
    }

    // ── 3. DAMAGE REPORT ──────────────────────────────────────────────

    /**
     * Driver reports damaged goods on delivery or damaged returns.
     * Auto-assigns to correct team based on damage type.
     */
    @Transactional
    public DamageReportDTO reportDamage(Long routeId, Long stopId,
                                         String productReference,
                                         String productName,
                                         int quantityAffected,
                                         String damageType,
                                         String notes) {
        if (stopId == null) throw new IllegalArgumentException("Stop ID must not be null");
        if (productReference == null || productReference.isBlank()) {
            throw new IllegalArgumentException("Product reference must not be empty");
        }
        if (quantityAffected <= 0) {
            throw new IllegalArgumentException("Quantity affected must be greater than 0");
        }

        RouteStop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop", stopId));

        String assignedTo = assignDamageReport(damageType);

        DamageReport report = DamageReport.builder()
                .routeStop(stop)
                .productReference(productReference)
                .productName(productName != null ? productName : productReference)
                .quantityAffected(quantityAffected)
                .damageType(damageType)
                .notes(notes)
                .reportedAt(LocalDateTime.now())
                .assignedTo(assignedTo)
                .status("OPEN")
                .build();

        DamageReport saved = damageReportRepository.save(report);

        return new DamageReportDTO(
                saved.getId(),
                stopId,
                stop.getClient().getName(),
                productReference,
                saved.getProductName(),
                quantityAffected,
                damageType,
                notes,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                assignedTo,
                "OPEN"
        );
    }

    /**
     * Get all open damage reports — for manager dashboard.
     */
    @Transactional(readOnly = true)
    public List<DamageReportDTO> getOpenDamageReports() {
        return damageReportRepository.findByStatus("OPEN")
                .stream()
                .map(r -> new DamageReportDTO(
                        r.getId(),
                        r.getRouteStop().getId(),
                        r.getRouteStop().getClient().getName(),
                        r.getProductReference(),
                        r.getProductName(),
                        r.getQuantityAffected(),
                        r.getDamageType(),
                        r.getNotes(),
                        r.getReportedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        r.getAssignedTo(),
                        r.getStatus()
                ))
                .collect(Collectors.toList());
    }

    // ── 4. INCIDENT ALERT ─────────────────────────────────────────────

    /**
     * Generate incident alert with suggested action.
     * Driver sick / truck breakdown / accident / delay.
     */
    public IncidentDTO createIncident(Long routeId, String incidentType,
                                       String description) {
        if (routeId == null) throw new IllegalArgumentException("Route ID must not be null");
        if (incidentType == null || incidentType.isBlank()) {
            throw new IllegalArgumentException("Incident type must not be empty");
        }

        DeliveryRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", routeId));

        String suggestedAction = suggestAction(incidentType);
        String alternative = suggestAlternativeCarrier(incidentType);

        return new IncidentDTO(
                System.currentTimeMillis(),
                routeId,
                incidentType,
                description,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                route.getCarrier().getName(),
                suggestedAction,
                alternative,
                "OPEN"
        );
    }

    // ── Private helpers ───────────────────────────────────────────────

    private List<WarehousePickItemDTO> buildPickItems(List<RouteStop> stops) {
        List<WarehousePickItemDTO> items = new ArrayList<>();
        int total = stops.size();

        for (RouteStop stop : stops) {
            String zone = assignZone(stop.getStopOrder(), total);
            String client = stop.getClient().getName();
            int order = stop.getStopOrder();

            // Simulate real products based on DDI typical bar order
            // In production: reads from actual order system
            items.addAll(buildTypicalBarOrder(client, order, zone, stop));
        }
        return items;
    }

    private List<WarehousePickItemDTO> buildTypicalBarOrder(
            String clientName, int stopOrder, String zone, RouteStop stop) {

        List<WarehousePickItemDTO> items = new ArrayList<>();

        // Main beer (always present)
        items.add(new WarehousePickItemDTO(
            "AA09A1", "ED13", "ESTRELLA DAMM 1/3 RET. PP",
            6, "Caja", false, clientName, stopOrder, zone,
            "Pick from AA09 — main Estrella shelf"));

        // Returnable crates (always paired with beer)
        items.add(new WarehousePickItemDTO(
            "AA09A1", "CJ13", "CAJA DAMM+BOT.1/3RET VACIO",
            6, "Caja", true, clientName, stopOrder, zone,
            "Return crates — collect empties at delivery"));

        // Barrel (if bar client)
        if (stop.getClient().getName().toLowerCase().contains("bar") ||
            stop.getClient().getName().toLowerCase().contains("restaurant")) {
            items.add(new WarehousePickItemDTO(
                "AA10A1", "ED30", "ESTRELLA DAMM BARRIL 30L",
                1, "Barril", false, clientName, stopOrder, zone,
                "Pick from AA10 — barrel zone. Handle with forklift."));
            items.add(new WarehousePickItemDTO(
                "AA10A1", "BRL30V", "BARRIL INOX 30L EURONORMA",
                1, "Barril", true, clientName, stopOrder, zone,
                "Empty barrel return — collect from client"));
        }

        // Water (most clients)
        items.add(new WarehousePickItemDTO(
            "BC04A3", "VE12SP", "AGUA VERI 1/2 PET CAJA 24U",
            2, "Caja", false, clientName, stopOrder, zone,
            "Pick from BC04 — Veri water zone"));

        return items;
    }

    private List<WarehousePickItemDTO> sortByAisle(List<WarehousePickItemDTO> items) {
        return items.stream()
                .sorted(Comparator.comparingInt(item -> {
                    String prefix = item.aisleCode().length() >= 2
                            ? item.aisleCode().substring(0, 2) : item.aisleCode();
                    int idx = AISLE_ORDER.indexOf(prefix);
                    return idx >= 0 ? idx : 999;
                }))
                .collect(Collectors.toList());
    }

    private String assignZone(int stopOrder, int total) {
        double pos = (double) stopOrder / total;
        if (pos <= 0.33) return "FRONT";
        if (pos <= 0.66) return "MIDDLE";
        return "BACK";
    }

    private String assignDamageReport(String damageType) {
        return switch (damageType) {
            case "DELIVERY_DAMAGED" -> "WAREHOUSE";
            case "RETURN_REJECTED"  -> "COMMERCIAL";
            case "MISSING"          -> "SUPERVISOR";
            default                 -> "SUPERVISOR";
        };
    }

    private String suggestAction(String incidentType) {
        return switch (incidentType) {
            case "DRIVER_SICK"      -> "AUTO_REASSIGN — reassign route to next available trusted carrier";
            case "TRUCK_BREAKDOWN"  -> "AUTO_REASSIGN — call backup vehicle, transfer load";
            case "ACCIDENT"         -> "ESCALATE — notify supervisor immediately, call emergency services";
            case "DELAY"            -> "NOTIFY_CLIENTS — alert affected clients of new ETA";
            default                 -> "MANUAL_REVIEW — supervisor assessment required";
        };
    }

    private String suggestAlternativeCarrier(String incidentType) {
        if ("ACCIDENT".equals(incidentType)) return "EMERGENCY — no auto-reassignment";
        return "Rutes Express Osona SL (WARNING — score 62) — available as backup";
    }

    private List<String> buildWarehouseInstructions(
            List<RouteStop> stops,
            Map<String, List<WarehousePickItemDTO>> byZone) {

        List<String> instructions = new ArrayList<>();
        instructions.add("=== DDI MOLLET — WAREHOUSE LOADING SHEET ===");
        instructions.add("Model: CLIENT_ORIENTED (replaces product-reference model)");
        instructions.add("Load BACK zone first. Load FRONT zone last.");
        instructions.add("");
        instructions.add("LOADING ORDER:");

        // Back zone first
        List<String> zones = List.of("BACK", "MIDDLE", "FRONT");
        int loadStep = 1;
        for (String zone : zones) {
            List<RouteStop> zoneStops = stops.stream()
                    .filter(s -> assignZone(s.getStopOrder(), stops.size()).equals(zone))
                    .collect(Collectors.toList());
            for (RouteStop stop : zoneStops) {
                instructions.add(String.format("[%d] %s ZONE → %s | Window: %s–%s",
                        loadStep++, zone,
                        stop.getClient().getName(),
                        stop.getClient().getDeliveryWindowStart(),
                        stop.getClient().getDeliveryWindowEnd()));
            }
        }

        instructions.add("");
        instructions.add("REMINDER: Group all products per client on same staging pallet.");
        instructions.add("Target: 2 min per client. Same product: 1 min. Mixed: stage first.");
        instructions.add(String.format("Estimated total loading time: %d minutes", stops.size() * 2));
        return instructions;
    }
}
