package com.dammroute.controller;

import com.dammroute.dto.*;
import com.dammroute.service.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    /**
     * GET /api/routes/{id}/warehouse-sheet
     *
     * Complete warehouse loading sheet for a route.
     * Replaces the paper system and the "old guy" knowledge.
     * Ready for iPad display.
     *
     * Returns:
     * - Pick list ordered by warehouse aisle (walk path)
     * - Staging list grouped by client
     * - Loading list grouped by truck zone
     * - Estimated loading time
     * - Plain language instructions
     */
    @GetMapping("/routes/{id}/warehouse-sheet")
    public ResponseEntity<WarehouseSheetDTO> getWarehouseSheet(
            @PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.generateWarehouseSheet(id));
    }

    /**
     * POST /api/routes/{routeId}/stops/{stopId}/confirm-delivery
     *
     * Driver confirms delivery with optional client signature.
     * Replaces paper albaran.
     *
     * Body: { signatureBase64, notes, returnablesCollected }
     */
    @PostMapping("/routes/{routeId}/stops/{stopId}/confirm-delivery")
    public ResponseEntity<DeliveryConfirmationDTO> confirmDelivery(
            @PathVariable Long routeId,
            @PathVariable Long stopId,
            @RequestBody Map<String, Object> body) {

        String signature = (String) body.getOrDefault("signatureBase64", "");
        String notes = (String) body.getOrDefault("notes", "");
        int returnables = (int) body.getOrDefault("returnablesCollected", 0);

        return ResponseEntity.ok(
            warehouseService.confirmDelivery(routeId, stopId, signature, notes, returnables));
    }

    /**
     * POST /api/routes/{routeId}/stops/{stopId}/damage
     *
     * Driver reports damaged goods on delivery or damaged returns.
     * Auto-assigns to correct team: WAREHOUSE / COMMERCIAL / SUPERVISOR
     *
     * Body: { productReference, productName, quantityAffected, damageType, notes }
     * damageType: DELIVERY_DAMAGED | RETURN_REJECTED | MISSING
     */
    @PostMapping("/routes/{routeId}/stops/{stopId}/damage")
    public ResponseEntity<DamageReportDTO> reportDamage(
            @PathVariable Long routeId,
            @PathVariable Long stopId,
            @RequestBody Map<String, Object> body) {

        String ref    = (String) body.get("productReference");
        String name   = (String) body.getOrDefault("productName", ref);
        int qty       = (int) body.get("quantityAffected");
        String type   = (String) body.get("damageType");
        String notes  = (String) body.getOrDefault("notes", "");

        return ResponseEntity.status(HttpStatus.CREATED).body(
            warehouseService.reportDamage(routeId, stopId, ref, name, qty, type, notes));
    }

    /**
     * GET /api/damage-reports
     * All open damage reports — for manager dashboard.
     */
    @GetMapping("/damage-reports")
    public ResponseEntity<List<DamageReportDTO>> getOpenDamageReports() {
        return ResponseEntity.ok(warehouseService.getOpenDamageReports());
    }

    /**
     * POST /api/routes/{id}/incident
     *
     * Report an incident: DRIVER_SICK | TRUCK_BREAKDOWN | ACCIDENT | DELAY
     * System suggests action and alternative carrier automatically.
     *
     * Body: { incidentType, description }
     */
    @PostMapping("/routes/{id}/incident")
    public ResponseEntity<IncidentDTO> createIncident(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        String type = (String) body.get("incidentType");
        String desc = (String) body.getOrDefault("description", "");

        return ResponseEntity.status(HttpStatus.CREATED).body(
            warehouseService.createIncident(id, type, desc));
    }
}
