package com.dammroute.service;

import com.dammroute.dto.*;
import com.dammroute.entity.*;
import com.dammroute.exception.ResourceNotFoundException;
import com.dammroute.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    private final DeliveryRouteRepository routeRepository;
    private final ClientRepository clientRepository;
    private final RouteOptimizerService optimizer;
    private final Co2CalculatorService co2Calculator;
    private final CarrierTrustService carrierTrustService;

    public RouteService(DeliveryRouteRepository routeRepository,
                        ClientRepository clientRepository,
                        RouteOptimizerService optimizer,
                        Co2CalculatorService co2Calculator,
                        CarrierTrustService carrierTrustService) {
        this.routeRepository = routeRepository;
        this.clientRepository = clientRepository;
        this.optimizer = optimizer;
        this.co2Calculator = co2Calculator;
        this.carrierTrustService = carrierTrustService;
    }

    @Transactional
    public RouteResponseDTO createOptimisedRoute(RouteRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("Request must not be null");

        // SECURITY: validate carrier trust BEFORE doing anything else
        carrierTrustService.validateCarrierForRoute(request.carrierId());
        Carrier carrier = carrierTrustService.getById(request.carrierId());

        // Fetch and validate clients
        List<Client> clients = fetchClients(request.clientIds());

        // Optimise route
        List<Client> optimisedOrder = optimizer.optimise(clients);

        // Calculate CO2
        double optimisedDistanceKm = co2Calculator.calculateTotalDistanceKm(optimisedOrder);
        double optimisedCo2 = co2Calculator.calculateCo2Kg(optimisedDistanceKm);
        double baselineCo2 = co2Calculator.calculateBaselineCo2Kg(clients);
        double savedPercent = co2Calculator.calculateSavedPercent(baselineCo2, optimisedCo2);
        double trees = co2Calculator.calculateTreesEquivalent(baselineCo2 - optimisedCo2);

        // Build route entity
        DeliveryRoute route = DeliveryRoute.builder()
                .carrier(carrier)
                .status("PENDING")
                .totalDistanceKm(optimisedDistanceKm)
                .baselineCo2Kg(baselineCo2)
                .optimisedCo2Kg(optimisedCo2)
                .co2SavedPercent(savedPercent)
                .treesEquivalent(trees)
                .createdAt(LocalDateTime.now())
                .departureTime(LocalDateTime.now().plusMinutes(30))
                .stops(buildStops(optimisedOrder))
                .build();

        DeliveryRoute saved = routeRepository.save(route);
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public RouteResponseDTO getRouteById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID must not be null");
        DeliveryRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));
        return toResponseDTO(route);
    }

    @Transactional
    public RouteResponseDTO completeStop(Long routeId, Long stopId) {
        DeliveryRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", routeId));

        route.getStops().stream()
                .filter(s -> s.getId().equals(stopId))
                .findFirst()
                .ifPresent(stop -> stop.setStatus("DELIVERED"));

        boolean allDelivered = route.getStops().stream()
                .allMatch(s -> "DELIVERED".equals(s.getStatus()));
        if (allDelivered) route.setStatus("COMPLETED");

        return toResponseDTO(routeRepository.save(route));
    }

    @Transactional(readOnly = true)
    public DashboardDTO getDashboardStats() {
        long active = routeRepository.countByStatus("ACTIVE") +
                routeRepository.countByStatus("PENDING");
        long completed = routeRepository.countByStatus("COMPLETED");
        double co2Saved = routeRepository.sumCo2SavedToday();
        long trusted = clientRepository.count();
        long blocked = 0;
        long totalClients = clientRepository.count();

        return new DashboardDTO(active, completed, co2Saved,
                trusted, blocked, totalClients);
    }

    // ── Private helpers ────────────────────────────────────────────

    private List<Client> fetchClients(List<Long> clientIds) {
        List<Client> clients = new ArrayList<>();
        for (Long id : clientIds) {
            Client c = clientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Client", id));
            clients.add(c);
        }
        return clients;
    }

    private List<RouteStop> buildStops(List<Client> orderedClients) {
        List<RouteStop> stops = new ArrayList<>();
        LocalTime current = LocalTime.of(7, 0); // Routes start 07:00

        for (int i = 0; i < orderedClients.size(); i++) {
            Client client = orderedClients.get(i);
            current = current.plusMinutes(25); // avg 25 min between stops

            RouteStop stop = RouteStop.builder()
                    .client(client)
                    .stopOrder(i + 1)
                    .status("PENDING")
                    .estimatedArrival(current.format(DateTimeFormatter.ofPattern("HH:mm")))
                    .parkingInstruction(buildParkingInstruction(client))
                    .build();
            stops.add(stop);
        }
        return stops;
    }

    private String buildParkingInstruction(Client client) {
        if (client.getNearestLoadingBay() != null && !client.getNearestLoadingBay().isBlank()) {
            return "Use loading bay: " + client.getNearestLoadingBay()
                    + " | Difficulty: " + client.getParkingDifficulty();
        }
        return "Parking difficulty: " + client.getParkingDifficulty()
                + ". Check street signage on arrival.";
    }

    private RouteResponseDTO toResponseDTO(DeliveryRoute route) {
        int totalStops = route.getStops().size();

        List<RouteStopDTO> stopDTOs = route.getStops().stream()
                .sorted((a, b) -> Integer.compare(a.getStopOrder(), b.getStopOrder()))
                .map(s -> {
                    // Determine Truck Zone based on sequence (33/34/33 split)
                    double percentile = (double) s.getStopOrder() / totalStops;
                    String zone;
                    if (percentile <= 0.33) {
                        zone = "FRONT (Unload 1st)";
                    } else if (percentile <= 0.67) {
                        zone = "MIDDLE (Unload 2nd)";
                    } else {
                        zone = "BACK (Unload Last)";
                    }

                    return new RouteStopDTO(
                            s.getId(),
                            s.getStopOrder(),
                            s.getClient().getName(),
                            s.getClient().getAddress(),
                            s.getClient().getLatitude(),
                            s.getClient().getLongitude(),
                            s.getClient().getDeliveryWindowStart(),
                            s.getClient().getDeliveryWindowEnd(),
                            s.getClient().getParkingDifficulty(),
                            s.getParkingInstruction(),
                            s.getEstimatedArrival(),
                            s.getStatus(),
                            zone // <--- Assigned here
                    );
                })
                .toList();

        return new RouteResponseDTO(
                route.getId(),
                route.getCarrier().getName(),
                route.getCarrier().getTrustScore(),
                route.getCarrier().getTrustLevel(),
                route.getStatus(),
                route.getTotalDistanceKm(),
                route.getBaselineCo2Kg(),
                route.getOptimisedCo2Kg(),
                route.getCo2SavedPercent(),
                route.getTreesEquivalent(),
                stopDTOs
        );
    }
}
