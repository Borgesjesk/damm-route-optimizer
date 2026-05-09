package com.dammroute.controller;

import com.dammroute.dto.DashboardDTO;
import com.dammroute.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DashboardController {

    private final RouteService routeService;

    public DashboardController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardDTO> getStats() {
        return ResponseEntity.ok(routeService.getDashboardStats());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "DammRoute API",
            "version", "1.0.0-hackathon"
        ));
    }
}
