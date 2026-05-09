package com.dammroute.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "clients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Client name is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 200)
    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    // "08:00" format
    @NotBlank(message = "Delivery window start is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
             message = "Time must be in HH:mm format")
    private String deliveryWindowStart;

    @NotBlank(message = "Delivery window end is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
             message = "Time must be in HH:mm format")
    private String deliveryWindowEnd;

    // LOW / MEDIUM / HIGH
    @Column(nullable = false)
    private String parkingDifficulty;

    @Size(max = 200)
    private String nearestLoadingBay;

    private boolean active = true;
}
