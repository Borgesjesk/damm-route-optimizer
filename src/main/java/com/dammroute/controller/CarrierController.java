package com.dammroute.controller;

import com.dammroute.dto.CarrierDTO;
import com.dammroute.entity.Carrier;
import com.dammroute.service.CarrierTrustService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class CarrierController {

    private final CarrierTrustService carrierTrustService;

    public CarrierController(CarrierTrustService carrierTrustService) {
        this.carrierTrustService = carrierTrustService;
    }

    @PostMapping
    public ResponseEntity<Carrier> createCarrier(@Valid @RequestBody CarrierDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carrierTrustService.createCarrier(dto));
    }

    @GetMapping
    public ResponseEntity<List<Carrier>> getAllCarriers() {
        return ResponseEntity.ok(carrierTrustService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carrier> getCarrier(@PathVariable Long id) {
        return ResponseEntity.ok(carrierTrustService.getById(id));
    }
}
