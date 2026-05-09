package com.dammroute.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RouteRequestDTO(
    @NotNull(message = "Carrier ID is required")
    Long carrierId,

    @NotEmpty(message = "At least one client stop is required")
    List<Long> clientIds
) {}
