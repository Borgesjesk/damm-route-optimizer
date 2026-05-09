package com.dammroute.controller;

import com.dammroute.dto.RouteRequestDTO;
import com.dammroute.dto.RouteResponseDTO;
import com.dammroute.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/optimise")
    public ResponseEntity<RouteResponseDTO> createOptimisedRoute(
            @Valid @RequestBody RouteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routeService.createOptimisedRoute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> getRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PutMapping("/{routeId}/stops/{stopId}/complete")
    public ResponseEntity<RouteResponseDTO> completeStop(
            @PathVariable Long routeId,
            @PathVariable Long stopId) {
        return ResponseEntity.ok(routeService.completeStop(routeId, stopId));
    }
}
