package com.dammroute.service;

import com.dammroute.entity.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteOptimizerService {

    private static final Logger log = LoggerFactory.getLogger(RouteOptimizerService.class);

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

        // Try Python optimizer first — math guys algorithm
        try {
            return callPythonOptimizer(clients);
        } catch (Exception e) {
            log.warn("Python optimizer failed, using greedy fallback: {}", e.getMessage());
            return fallbackGreedy(clients);
        }
    }

    private List<Client> callPythonOptimizer(List<Client> clients) throws Exception {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            json.append(String.format("{\"id\":%d,\"lat\":%.6f,\"lng\":%.6f,\"name\":\"%s\"}",
                    c.getId(), c.getLatitude(), c.getLongitude(),
                    c.getName().replace("\"", "\\\"")));
            if (i < clients.size() - 1) json.append(",");
        }
        json.append("]");

        ProcessBuilder pb = new ProcessBuilder("python3", "optimizer.py");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.getOutputStream().write(json.toString().getBytes());
        process.getOutputStream().close();

        String output = new String(process.getInputStream().readAllBytes()).trim();
        process.waitFor();

        String cleaned = output.replace("[", "").replace("]", "").trim();
        if (cleaned.isEmpty()) return fallbackGreedy(clients);

        Map<Long, Client> clientMap = clients.stream()
                .collect(Collectors.toMap(Client::getId, c -> c));

        List<Client> ordered = new ArrayList<>();
        for (String part : cleaned.split(",")) {
            Long id = Long.parseLong(part.trim());
            if (clientMap.containsKey(id)) ordered.add(clientMap.get(id));
        }
        return ordered.isEmpty() ? fallbackGreedy(clients) : ordered;
    }

    private List<Client> fallbackGreedy(List<Client> clients) {
        // Step 1: Group clients by their nearest loading bay (parking node)
        Map<String, List<Client>> parkingClusters = clients.stream()
                .collect(Collectors.groupingBy(c ->
                        c.getNearestLoadingBay() != null ? c.getNearestLoadingBay() : "UNKNOWN_" + c.getId()));

        // Step 2: Order clusters by nearest parking node to warehouse (greedy)
        List<Map.Entry<String, List<Client>>> orderedClusters = new ArrayList<>(parkingClusters.entrySet());
        double lat = warehouseLat, lng = warehouseLng;
        List<Map.Entry<String, List<Client>>> sorted = new ArrayList<>();

        while (!orderedClusters.isEmpty()) {
            final double currentLat = lat, currentLng = lng;
            Map.Entry<String, List<Client>> nearest = orderedClusters.stream()
                    .min(Comparator.comparingDouble(e ->
                            e.getValue().stream()
                                    .mapToDouble(c -> co2Calculator.haversine(currentLat, currentLng, c.getLatitude(), c.getLongitude()))
                                    .min().orElse(Double.MAX_VALUE)))
                    .orElseThrow();
            sorted.add(nearest);
            orderedClusters.remove(nearest);
            // Move to the center of this cluster for next iteration
            lat = nearest.getValue().stream().mapToDouble(Client::getLatitude).average().orElse(lat);
            lng = nearest.getValue().stream().mapToDouble(Client::getLongitude).average().orElse(lng);
        }

        // Step 3: Flatten — within each cluster, sort by delivery window
        List<Client> route = new ArrayList<>();
        for (Map.Entry<String, List<Client>> cluster : sorted) {
            List<Client> group = new ArrayList<>(cluster.getValue());
            group.sort(Comparator.comparing(Client::getDeliveryWindowStart,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            route.addAll(group);
        }

        log.info("Parking-first optimizer: {} clusters from {} clients", sorted.size(), clients.size());
        return route;
    }

    private Client findNearest(List<Client> clients, double lat, double lng) {
        return clients.stream()
                .min(Comparator.comparingDouble(c ->
                        co2Calculator.haversine(lat, lng, c.getLatitude(), c.getLongitude())))
                .orElseThrow();
    }
}
