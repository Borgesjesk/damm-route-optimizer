package com.dammroute.controller;

import com.dammroute.dto.TruckLoadingPlanDTO;
import com.dammroute.service.TruckLoadingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TruckLoadingController {

    private final TruckLoadingService truckLoadingService;

    public TruckLoadingController(TruckLoadingService truckLoadingService) {
        this.truckLoadingService = truckLoadingService;
    }

    /**
     * GET /api/routes/{id}/loading-plan
     *
     * Returns complete truck loading plan for a route:
     * - Loading sequence (reverse of delivery order)
     * - Zone breakdown (FRONT/MIDDLE/BACK)
     * - Volume calculations including return space
     * - Warehouse instructions in plain language
     *
     * Requires: X-API-Key header
     * Requires: Route must exist and have stops
     */
    @GetMapping("/{id}/loading-plan")
    public ResponseEntity<TruckLoadingPlanDTO> getLoadingPlan(
            @PathVariable Long id) {
        return ResponseEntity.ok(truckLoadingService.generateLoadingPlan(id));
    }
}
