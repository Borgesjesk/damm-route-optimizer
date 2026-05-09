package com.dammroute.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "delivery_routes")
public class DeliveryRoute {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;
    @Column(nullable = false) private String status = "PENDING";
    private double totalDistanceKm;
    private double baselineCo2Kg;
    private double optimisedCo2Kg;
    private double co2SavedPercent;
    private double treesEquivalent;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime departureTime;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "route_id")
    private List<RouteStop> stops = new ArrayList<>();
    public DeliveryRoute() {}
    public Long getId() { return id; }
    public Carrier getCarrier() { return carrier; }
    public String getStatus() { return status; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public double getBaselineCo2Kg() { return baselineCo2Kg; }
    public double getOptimisedCo2Kg() { return optimisedCo2Kg; }
    public double getCo2SavedPercent() { return co2SavedPercent; }
    public double getTreesEquivalent() { return treesEquivalent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public List<RouteStop> getStops() { return stops; }
    public void setId(Long id) { this.id = id; }
    public void setCarrier(Carrier carrier) { this.carrier = carrier; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalDistanceKm(double v) { this.totalDistanceKm = v; }
    public void setBaselineCo2Kg(double v) { this.baselineCo2Kg = v; }
    public void setOptimisedCo2Kg(double v) { this.optimisedCo2Kg = v; }
    public void setCo2SavedPercent(double v) { this.co2SavedPercent = v; }
    public void setTreesEquivalent(double v) { this.treesEquivalent = v; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public void setDepartureTime(LocalDateTime v) { this.departureTime = v; }
    public void setStops(List<RouteStop> stops) { this.stops = stops; }
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final DeliveryRoute r = new DeliveryRoute();
        public Builder carrier(Carrier v) { r.carrier = v; return this; }
        public Builder status(String v) { r.status = v; return this; }
        public Builder totalDistanceKm(double v) { r.totalDistanceKm = v; return this; }
        public Builder baselineCo2Kg(double v) { r.baselineCo2Kg = v; return this; }
        public Builder optimisedCo2Kg(double v) { r.optimisedCo2Kg = v; return this; }
        public Builder co2SavedPercent(double v) { r.co2SavedPercent = v; return this; }
        public Builder treesEquivalent(double v) { r.treesEquivalent = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
        public Builder departureTime(LocalDateTime v) { r.departureTime = v; return this; }
        public Builder stops(List<RouteStop> v) { r.stops = v; return this; }
        public DeliveryRoute build() { return r; }
    }
}
