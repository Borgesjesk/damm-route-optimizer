package com.dammroute.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "route_stops")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private int stopOrder;

    // PENDING / DELIVERED / FAILED
    @Column(nullable = false)
    private String status = "PENDING";

    private String estimatedArrival;

    @Column(length = 300)
    private String parkingInstruction;
}
