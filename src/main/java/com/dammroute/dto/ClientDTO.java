package com.dammroute.dto;

import jakarta.validation.constraints.*;

public record ClientDTO(
    @NotBlank String name,
    @NotBlank String address,
    double latitude,
    double longitude,
    @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$") String deliveryWindowStart,
    @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$") String deliveryWindowEnd,
    String nearestLoadingBay
) {}
