package com.dammroute.service;

import com.dammroute.dto.CommercialAlertDTO;
import com.dammroute.dto.SustainabilityBadgeDTO;
import com.dammroute.entity.*;
import com.dammroute.exception.ResourceNotFoundException;
import com.dammroute.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CommercialIntelligenceService
 *
 * Turns every delivery into a commercial intelligence moment.
 *
 * Two features:
 * 1. Smart Commercial Alerts — triggered on stop completion
 *    Detects: churn risk, upsell opportunity, competitor activity
 *    Routes to: sales rep, telesales, or marketing automation
 *
 * 2. Sustainability Badge — generated after route completion
 *    Shows CO₂ saved, trees equivalent, shareable social media message
 *    Badge levels: BRONZE / SILVER / GOLD / PLATINUM
 *
 * Connects DammRoute logistics to Damm's commercial strategy.
 * Same data, double value.
 */
@Service
public class CommercialIntelligenceService {

    private final CommercialAlertRepository alertRepository;
    private final ClientRepository clientRepository;
    private final DeliveryRouteRepository routeRepository;
    private final Co2CalculatorService co2Calculator;

    public CommercialIntelligenceService(
            CommercialAlertRepository alertRepository,
            ClientRepository clientRepository,
            DeliveryRouteRepository routeRepository,
            Co2CalculatorService co2Calculator) {
        this.alertRepository    = alertRepository;
        this.clientRepository   = clientRepository;
        this.routeRepository    = routeRepository;
        this.co2Calculator      = co2Calculator;
    }

    // ── FEATURE 1: Commercial Alerts ──────────────────────────────────

    /**
     * Called automatically when a delivery stop is completed.
     * Analyses the client and generates commercial alerts if needed.
     */
    @Transactional
    public List<CommercialAlertDTO> generateAlertsForClient(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID must not be null");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        List<CommercialAlert> alerts = new ArrayList<>();
        alerts.addAll(analyseClientType(client));
        alertRepository.saveAll(alerts);

        return alerts.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Get all open commercial alerts — for the alerts dashboard.
     */
    @Transactional(readOnly = true)
    public List<CommercialAlertDTO> getOpenAlerts() {
        return alertRepository.findByStatusOrderByPriorityAsc("OPEN")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── FEATURE 2: Sustainability Badge ──────────────────────────────

    /**
     * Generate sustainability badge after route completion.
     * Ready to share on social media.
     */
    @Transactional(readOnly = true)
    public SustainabilityBadgeDTO generateSustainabilityBadge(Long routeId) {
        if (routeId == null) {
            throw new IllegalArgumentException("Route ID must not be null");
        }

        DeliveryRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", routeId));

        double co2Saved    = route.getBaselineCo2Kg() - route.getOptimisedCo2Kg();
        double savedPct    = route.getCo2SavedPercent();
        double trees       = route.getTreesEquivalent();
        double distSaved   = estimateDistanceSaved(co2Saved);
        String level       = determineBadgeLevel(savedPct);
        String message     = buildShareMessage(route, co2Saved, trees, level);
        String hashtags    = "#DammRoute #BarcelonaSostenible #BCNClima2030 "
                           + "#Damm #GreenLogistics #SmartTruck";

        return new SustainabilityBadgeDTO(
                routeId,
                route.getCarrier().getName(),
                route.getStops().size(),
                Math.round(co2Saved * 100.0) / 100.0,
                savedPct,
                trees,
                route.getTotalDistanceKm(),
                Math.round(distSaved * 100.0) / 100.0,
                level,
                message,
                hashtags
        );
    }

    // ── Private helpers ───────────────────────────────────────────────

    /**
     * Generate alerts based on client type and name patterns.
     * In production: uses real purchase history from Damm's WMS.
     * For demo: uses client type embedded in name to simulate real data.
     */
    private List<CommercialAlert> analyseClientType(Client client) {
        List<CommercialAlert> alerts = new ArrayList<>();
        String name = client.getName().toLowerCase();

        // B2B Hostelería — detect churn risk and upsell
        if (name.contains("bar") || name.contains("tapas") ||
            name.contains("cervecería") || name.contains("club")) {

            if (name.contains("club") || name.contains("nocturn")) {
                // Clubs: upsell premium products
                alerts.add(buildAlert(client,
                    "UPSELL",
                    "HIGH",
                    "Club client — high volume weekend demand detected",
                    "Propose Estrella Damm Premium package. Weekend events upcoming.",
                    "SALES_REP",
                    850.0, 0.72, 3));
            } else {
                // Regular bars: check competitor activity
                alerts.add(buildAlert(client,
                    "COMPETITOR",
                    "MEDIUM",
                    "Purchase frequency DOWN 20% vs last month",
                    "Schedule sales rep visit. Competitor Heineken active in zone.",
                    "SALES_REP",
                    420.0, 0.65, 7));
            }
        }

        // B2B Hotel — large volume, loyalty focus
        if (name.contains("hotel") || name.contains("hilton") ||
            name.contains("marriott") || name.contains("w barcelona")) {

            alerts.add(buildAlert(client,
                "LOYALTY_LOW",
                "HIGH",
                "International hotel chain — contract renewal due in 30 days",
                "Schedule key account meeting. Prepare volume discount proposal.",
                "SALES_REP",
                3200.0, 0.85, 14));
        }

        // B2B Retail — reorder prediction
        if (name.contains("mercadona") || name.contains("caprabo") ||
            name.contains("supermercat")) {

            alerts.add(buildAlert(client,
                "REORDER",
                "MEDIUM",
                "Stock rotation analysis: reorder point reached for Estrella 33cl",
                "Auto-trigger reorder proposal via telesales. Estimated need: 200 units.",
                "TELESALES",
                680.0, 0.90, 2));
        }

        // B2B International — large accounts
        if (name.contains("internacional") || name.contains("aeroport") ||
            name.contains("hard rock") || name.contains("palau") ||
            name.contains("camp nou")) {

            alerts.add(buildAlert(client,
                "UPSELL",
                "HIGH",
                "High-traffic venue — seasonal event demand spike detected",
                "Prepare special event pricing proposal. Contact venue manager.",
                "SALES_REP",
                5500.0, 0.78, 5));
        }

        // Default: generic churn detection for any client
        if (alerts.isEmpty()) {
            alerts.add(buildAlert(client,
                "CHURN_RISK",
                "LOW",
                "Purchase pattern analysis: client behaviour within normal range",
                "Monitor next 2 deliveries. No action needed today.",
                "MARKETING_AUTO",
                150.0, 0.40, 30));
        }

        return alerts;
    }

    private CommercialAlert buildAlert(Client client, String type,
            String priority, String message, String recommendation,
            String channel, double revenue, double probability, int urgencyDays) {
        return CommercialAlert.builder()
                .client(client)
                .alertType(type)
                .priority(priority)
                .message(message)
                .recommendation(recommendation)
                .assignedChannel(channel)
                .expectedRevenueEur(revenue)
                .conversionProbability(probability)
                .urgencyDays(urgencyDays)
                .createdAt(LocalDateTime.now())
                .status("OPEN")
                .build();
    }

    private String determineBadgeLevel(double savedPct) {
        if (savedPct >= 40) return "PLATINUM";
        if (savedPct >= 30) return "GOLD";
        if (savedPct >= 20) return "SILVER";
        return "BRONZE";
    }

    private double estimateDistanceSaved(double co2SavedKg) {
        // 0.27 kg CO2 per km — reverse calculation
        return co2SavedKg / 0.27;
    }

    private String buildShareMessage(DeliveryRoute route,
            double co2Saved, double trees, String level) {
        return String.format(
            "🍺 DammRoute completed %d deliveries across Barcelona today "
            + "saving %.1f kg of CO₂ — equivalent to %.1f trees. "
            + "%s badge earned. "
            + "Smart logistics for a greener Barcelona 2030. "
            + "Carrier: %s ✅",
            route.getStops().size(),
            co2Saved,
            trees,
            level,
            route.getCarrier().getName()
        );
    }

    private CommercialAlertDTO toDTO(CommercialAlert alert) {
        return new CommercialAlertDTO(
                alert.getId(),
                alert.getClient().getName(),
                alert.getClient().getAddress(),
                alert.getAlertType(),
                alert.getPriority(),
                alert.getMessage(),
                alert.getRecommendation(),
                alert.getAssignedChannel(),
                alert.getExpectedRevenueEur(),
                alert.getConversionProbability(),
                alert.getUrgencyDays(),
                alert.getStatus()
        );
    }
}
