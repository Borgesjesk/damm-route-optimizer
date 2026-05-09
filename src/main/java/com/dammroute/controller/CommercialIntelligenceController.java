package com.dammroute.controller;

import com.dammroute.dto.CommercialAlertDTO;
import com.dammroute.dto.SustainabilityBadgeDTO;
import com.dammroute.service.CommercialIntelligenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class CommercialIntelligenceController {

    private final CommercialIntelligenceService service;

    public CommercialIntelligenceController(
            CommercialIntelligenceService service) {
        this.service = service;
    }

    /**
     * GET /api/alerts
     * All open commercial alerts — for the sales dashboard.
     * Used by: sales reps, telesales, marketing team.
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<CommercialAlertDTO>> getAlerts() {
        return ResponseEntity.ok(service.getOpenAlerts());
    }

    /**
     * POST /api/clients/{id}/analyse
     * Trigger commercial analysis when a delivery is completed.
     * Called automatically after marking a stop as DELIVERED.
     */
    @PostMapping("/clients/{id}/analyse")
    public ResponseEntity<List<CommercialAlertDTO>> analyseClient(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.generateAlertsForClient(id));
    }

    /**
     * GET /api/routes/{id}/sustainability-badge
     * Generate sustainability badge for a completed route.
     * Ready to share on social media.
     */
    @GetMapping("/routes/{id}/sustainability-badge")
    public ResponseEntity<SustainabilityBadgeDTO> getSustainabilityBadge(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.generateSustainabilityBadge(id));
    }
}
