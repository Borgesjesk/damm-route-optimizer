package com.dammroute.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_routes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;

    // PENDING / ACTIVE / COMPLETED
    @Column(nullable = false)
    private String status = "PENDING";

    private double totalDistanceKm;
    private double baselineCo2Kg;       // unoptimised
    private double optimisedCo2Kg;      // after optimisation
    private double co2SavedPercent;
    private double treesEquivalent;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime departureTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "route_id")
    @Builder.Default
    private List<RouteStop> stops = new ArrayList<>();
}
