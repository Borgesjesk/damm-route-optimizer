package com.dammroute.config;

import com.dammroute.entity.Carrier;
import com.dammroute.entity.Client;
import com.dammroute.repository.CarrierRepository;
import com.dammroute.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * REAL DAMM DDI DATA — Route DR0027
 * Source: Hoja de Ruta 08.05.2026
 * Driver: FRAN ROMERO (850004)
 * Warehouse: DDI Mollet del Vallès
 *
 * 3 zones: Sant Julià de Vilatorta → Calldetenes → Folgueroles
 * Payment: NO CONTADO (cash) + NO CREDITO (credit)
 * Total route value: €7,832.38
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final CarrierRepository carrierRepository;

    public DataLoader(ClientRepository clientRepository,
                      CarrierRepository carrierRepository) {
        this.clientRepository = clientRepository;
        this.carrierRepository = carrierRepository;
    }

    @Override
    public void run(String... args) {
        loadClients();
        loadCarriers();
    }

    private void loadClients() {
        List<Client> clients = List.of(

            // ── ZONA 1: SANT JULIÀ DE VILATORTA ──────────────────────
            // Town center: 41.9246, 2.3211

            Client.builder()
                .name("BAR PAVELLO ST JULIA")
                .address("Avenida Sant Llorenç S/N, Sant Julià de Vilatorta")
                .latitude(41.9260).longitude(2.3190)
                .deliveryWindowStart("08:00").deliveryWindowEnd("12:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("Av. Sant Llorenç — aparcament públic")
                .active(true).build(),

            Client.builder()
                .name("BAR EL TUPÍ")
                .address("Avinguda Nostra Senyora de Montserrat, Sant Julià de Vilatorta")
                .latitude(41.9248).longitude(2.3205)
                .deliveryWindowStart("08:00").deliveryWindowEnd("12:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("Avinguda principal — zona càrrega")
                .active(true).build(),

            Client.builder()
                .name("MAS L'ALBAREDA")
                .address("Avinguda de Sant Llorenç 68, Sant Julià de Vilatorta")
                .latitude(41.9255).longitude(2.3175)
                .deliveryWindowStart("09:00").deliveryWindowEnd("13:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("Av. Sant Llorenç 68 — entrada posterior")
                .active(true).build(),

            Client.builder()
                .name("BAR NURIA ST. JULIA")
                .address("Calle Nuria 27, Sant Julià de Vilatorta")
                .latitude(41.9242).longitude(2.3220)
                .deliveryWindowStart("08:00").deliveryWindowEnd("11:00")
                .parkingDifficulty("MEDIUM")
                .nearestLoadingBay("C/ Nuria — zona càrrega davant")
                .active(true).build(),

            Client.builder()
                .name("CAL TEIXIDOR")
                .address("Carrer de Puig-l'agulla S/N, Sant Julià de Vilatorta")
                .latitude(41.9235).longitude(2.3198)
                .deliveryWindowStart("09:00").deliveryWindowEnd("13:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("C/ Puig-l'agulla — accés lliure")
                .active(true).build(),

            // ── ZONA 2: CALLDETENES ───────────────────────────────────
            // Town center: 41.9136, 2.3089

            Client.builder()
                .name("RESTAURANT EL ROSER")
                .address("Avinguda Pau Casals 22, Calldetenes")
                .latitude(41.9140).longitude(2.3095)
                .deliveryWindowStart("08:00").deliveryWindowEnd("11:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("Av. Pau Casals — zona càrrega restaurant")
                .active(true).build(),

            Client.builder()
                .name("CELLER CALLDETENES")
                .address("Calle Gran 1, Calldetenes")
                .latitude(41.9132).longitude(2.3082)
                .deliveryWindowStart("09:00").deliveryWindowEnd("13:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("C/ Gran 1 — davant celler")
                .active(true).build(),

            Client.builder()
                .name("SUKIPA")
                .address("Calle Gran 9, Calldetenes")
                .latitude(41.9134).longitude(2.3085)
                .deliveryWindowStart("09:00").deliveryWindowEnd("13:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("C/ Gran — zona càrrega compartida")
                .active(true).build(),

            Client.builder()
                .name("BAR DE LA BENZINERA")
                .address("Carretera N-141 D S/N, Calldetenes")
                .latitude(41.9145).longitude(2.3110)
                .deliveryWindowStart("07:00").deliveryWindowEnd("10:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("N-141 — aparcament gasolinera")
                .active(true).build(),

            Client.builder()
                .name("CA LA NENA")
                .address("Carrer Gran 20, Calldetenes")
                .latitude(41.9136).longitude(2.3088)
                .deliveryWindowStart("09:00").deliveryWindowEnd("13:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("C/ Gran 20 — entrada principal")
                .active(true).build(),

            // ── ZONA 3: FOLGUEROLES ───────────────────────────────────
            // Town center: 41.9089, 2.3012

            Client.builder()
                .name("BAR KARNAK")
                .address("Calle Major 12, Folgueroles")
                .latitude(41.9092).longitude(2.3018)
                .deliveryWindowStart("08:00").deliveryWindowEnd("12:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("C/ Major — zona càrrega centre")
                .active(true).build(),

            Client.builder()
                .name("L'ESPAI RESTAURANT")
                .address("Calle Rec d'Acumulada 9, Folgueroles")
                .latitude(41.9085).longitude(2.3008)
                .deliveryWindowStart("09:00").deliveryWindowEnd("13:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("C/ Rec d'Acumulada — entrada cuina")
                .active(true).build(),

            Client.builder()
                .name("LA COCA DE FOLGUEROLES")
                .address("Carrer Camí Vell de Vic 16, Folgueroles")
                .latitude(41.9080).longitude(2.3005)
                .deliveryWindowStart("09:00").deliveryWindowEnd("13:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("Camí Vell de Vic — accés lliure")
                .active(true).build(),

            Client.builder()
                .name("CAL CISTELLER")
                .address("Plaça Mossèn Cinto Verdaguer 3, Folgueroles")
                .latitude(41.9090).longitude(2.3015)
                .deliveryWindowStart("08:00").deliveryWindowEnd("12:00")
                .parkingDifficulty("LOW")
                .nearestLoadingBay("Plaça Verdaguer — zona càrrega plaça")
                .active(true).build()
        );

        clientRepository.saveAll(clients);
    }

    private void loadCarriers() {
        List<Carrier> carriers = List.of(
            // REAL DDI carrier — TRUSTED
            Carrier.builder()
                .name("FRAN ROMERO — DDI 850004")
                .licenseNumber("7524KXX")
                .trustScore(95).trustLevel("TRUSTED")
                .documentVerified(true).disputeCount(0)
                .identityFlagged(false).active(true).build(),

            // WARNING carrier
            Carrier.builder()
                .name("Rutes Express Osona SL")
                .licenseNumber("B7654321")
                .trustScore(62).trustLevel("WARNING")
                .documentVerified(true).disputeCount(2)
                .identityFlagged(false).active(true).build(),

            // BLOCKED — FraudSentinel demo
            Carrier.builder()
                .name("Unknown Transport SL")
                .licenseNumber("B0000000")
                .trustScore(18).trustLevel("BLOCKED")
                .documentVerified(false).disputeCount(5)
                .identityFlagged(true).active(true).build()
        );

        carrierRepository.saveAll(carriers);
    }
}
