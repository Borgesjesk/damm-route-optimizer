package com.dammroute.dto;

import jakarta.validation.constraints.*;

public record CarrierDTO(
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    String name,

    @NotBlank(message = "License number is required")
    @Size(max = 20)
    String licenseNumber,

    boolean documentVerified,

    @Min(0)
    int disputeCount
) {}
