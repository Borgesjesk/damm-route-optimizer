package com.dammroute.service;

import com.dammroute.entity.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calculates CO2 emissions for delivery routes.
 * Baseline = average of 100 random orderings.
 * Optimised = result of route optimizer.
 */
@Service
public class Co2CalculatorService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    // 1 tree absorbs ~21 kg CO2 per year (standard EU estimate)
    private static final double KG_CO2_PER_TREE_PER_YEAR = 21.0;

    @Value("${app.co2.kg-per-km}")
    private double kgCo2PerKm;

    @Value("${app.warehouse.latitude}")
    private double warehouseLat;

    @Value("${app.warehouse.longitude}")
    private double warehouseLng;

    /**
     * Calculates total route distance including return to warehouse.
     */
    public double calculateTotalDistanceKm(List<Client> orderedStops) {
        if (orderedStops == null || orderedStops.isEmpty()) return 0.0;

        double total = 0.0;
        double currentLat = warehouseLat;
        double currentLng = warehouseLng;

        for (Client stop : orderedStops) {
            total += haversine(currentLat, currentLng,
                               stop.getLatitude(), stop.getLongitude());
            currentLat = stop.getLatitude();
            currentLng = stop.getLongitude();
        }

        // Return to warehouse
        total += haversine(currentLat, currentLng, warehouseLat, warehouseLng);
        return Math.round(total * 100.0) / 100.0;
    }

    public double calculateCo2Kg(double distanceKm) {
        return Math.round(distanceKm * kgCo2PerKm * 100.0) / 100.0;
    }

    /**
     * Baseline: average CO2 of 100 random stop orderings.
     * This proves our optimizer actually saves emissions.
     */
    public double calculateBaselineCo2Kg(List<Client> clients) {
        if (clients == null || clients.isEmpty()) return 0.0;

        List<Client> mutable = new ArrayList<>(clients);
        double totalDistance = 0.0;
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            Collections.shuffle(mutable);
            totalDistance += calculateTotalDistanceKm(mutable);
        }

        double avgDistance = totalDistance / iterations;
        return calculateCo2Kg(avgDistance);
    }

    public double calculateSavedPercent(double baselineCo2, double optimisedCo2) {
        if (baselineCo2 <= 0) return 0.0;
        double saved = ((baselineCo2 - optimisedCo2) / baselineCo2) * 100.0;
        return Math.round(saved * 10.0) / 10.0;
    }

    public double calculateTreesEquivalent(double co2SavedKg) {
        return Math.round((co2SavedKg / KG_CO2_PER_TREE_PER_YEAR) * 100.0) / 100.0;
    }

    /**
     * Haversine formula — distance between two lat/lng points in km.
     */
    public double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
