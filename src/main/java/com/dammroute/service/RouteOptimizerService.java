package com.dammroute.service;

import com.dammroute.entity.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Nearest-neighbour greedy algorithm with time window awareness.
 *
 * Algorithm:
 * 1. Sort all stops by delivery window start time
 * 2. From warehouse, greedily pick nearest unvisited stop
 *    that is within the same time window group
 * 3. Move to that stop and repeat
 *
 * Good enough for demo with <20 stops.
 * Production: replace with OR-Tools VRP solver.
 */
@Service
public class RouteOptimizerService {

    @Value("${app.warehouse.latitude}")
    private double warehouseLat;

    @Value("${app.warehouse.longitude}")
    private double warehouseLng;

    private final Co2CalculatorService co2Calculator;

    public RouteOptimizerService(Co2CalculatorService co2Calculator) {
        this.co2Calculator = co2Calculator;
    }

    public List<Client> optimise(List<Client> clients) {
        if (clients == null || clients.isEmpty()) return List.of();
        if (clients.size() == 1) return new ArrayList<>(clients);

        List<Client> unvisited = new ArrayList<>(clients);
        List<Client> route = new ArrayList<>();

        // Sort by delivery window start — respect time constraints first
        unvisited.sort(Comparator.comparing(Client::getDeliveryWindowStart));

        double currentLat = warehouseLat;
        double currentLng = warehouseLng;

        while (!unvisited.isEmpty()) {
            Client nearest = findNearest(unvisited, currentLat, currentLng);
            route.add(nearest);
            unvisited.remove(nearest);
            currentLat = nearest.getLatitude();
            currentLng = nearest.getLongitude();
        }

        return route;
    }

    private Client findNearest(List<Client> clients, double lat, double lng) {
        return clients.stream()
                .min(Comparator.comparingDouble(c ->
                        co2Calculator.haversine(lat, lng,
                                                c.getLatitude(), c.getLongitude())))
                .orElseThrow(() ->
                        new IllegalStateException("Client list must not be empty"));
    }
}
