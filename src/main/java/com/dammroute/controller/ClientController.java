package com.dammroute.controller;

import com.dammroute.dto.ClientDTO;
import com.dammroute.entity.Client;
import com.dammroute.exception.ResourceNotFoundException;
import com.dammroute.repository.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientRepository.findByActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(
            clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id))
        );
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@Valid @RequestBody ClientDTO dto) {
        Client client = Client.builder()
                .name(dto.name().strip())
                .address(dto.address().strip())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .deliveryWindowStart(dto.deliveryWindowStart())
                .deliveryWindowEnd(dto.deliveryWindowEnd())
                .parkingDifficulty("MEDIUM")
                .nearestLoadingBay(dto.nearestLoadingBay())
                .active(true)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientRepository.save(client));
    }
}
