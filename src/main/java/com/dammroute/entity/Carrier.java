package com.dammroute.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "carriers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Carrier name is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "License number is required")
    @Size(max = 20)
    @Column(nullable = false, unique = true)
    private String licenseNumber;

    // 0–100 — computed from factors below
    @Min(0) @Max(100)
    @Column(nullable = false)
    private int trustScore;

    // TRUSTED / WARNING / BLOCKED
    @Column(nullable = false)
    private String trustLevel;

    @Column(nullable = false)
    private boolean documentVerified;

    @Min(0)
    private int disputeCount;

    // Security finding from FraudSentinel —
    // tracks if carrier has been flagged for identity anomaly
    private boolean identityFlagged;

    private boolean active = true;
}
