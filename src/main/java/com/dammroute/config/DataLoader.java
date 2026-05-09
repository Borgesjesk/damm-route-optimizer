package com.dammroute.config;
import com.dammroute.entity.Carrier;
import com.dammroute.entity.Client;
import com.dammroute.repository.CarrierRepository;
import com.dammroute.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
/**
 * Realistic Damm DDI client network.
 *
 * Client types (matching real Damm distribution):
 * - B2B Hostelería: bars, restaurants, clubs (main channel)
 * - B2B Hotel: strict windows, large volume
 * - B2B Retail: supermarkets, convenience stores
 * - B2B International: hotels chains, airport, stadium
 * - B2C: not in DDI scope — excluded
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
// ── B2B HOSTELERÍA — Bars & Restaurants ──────────────────
                Client.builder()

                        .name("Bar El Born [B2B · Bar]")
                        .address("Carrer del Comerç 24, Barcelona")
                        .latitude(41.3851).longitude(2.1827)
                        .deliveryWindowStart("06:00").deliveryWindowEnd("09:00")
                        .parkingDifficulty("HIGH")
                        .nearestLoadingBay("Zona càrrega C/Princesa")
                        .active(true).build(),
                Client.builder()
                        .name("Cervecería Barceloneta [B2B · Bar]")
                        .address("Passeig Joan de Borbó 22, Barcelona")
                        .latitude(41.3780).longitude(2.1877)
                        .deliveryWindowStart("07:00").deliveryWindowEnd("10:00")
                        .parkingDifficulty("HIGH")
                        .nearestLoadingBay("Passeig Marítim càrrega")
                        .active(true).build(),
                Client.builder()
                        .name("Restaurant Eixample Central [B2B · Restaurant]")
                        .address("Carrer d'Enric Granados 56, Barcelona")
                        .latitude(41.3907).longitude(2.1601)
                        .deliveryWindowStart("08:00").deliveryWindowEnd("11:00")
                        .parkingDifficulty("LOW")
                        .nearestLoadingBay("Zona càrrega C/Aragó")
                        .active(true).build(),
                Client.builder()
                        .name("Tapas Bar Poble Sec [B2B · Bar]")
                        .address("Carrer de Blai 35, Barcelona")
                        .latitude(41.3741).longitude(2.1605)
                        .deliveryWindowStart("08:00").deliveryWindowEnd("11:00")
                        .parkingDifficulty("MEDIUM")
                        .nearestLoadingBay("Zona càrrega Av. Paral·lel")
                        .active(true).build(),
                Client.builder()
                        .name("Club Nocturn Gràcia [B2B · Club]")
                        .address("Carrer de Verdi 45, Barcelona")
                        .latitude(41.4036).longitude(2.1579)
                        .deliveryWindowStart("09:00").deliveryWindowEnd("12:00")
                        .parkingDifficulty("MEDIUM")
                        .nearestLoadingBay("Zona càrrega C/Verdi")
                        .active(true).build(),
// ── B2B HOTEL — Strict windows, large volume ─────────────
                Client.builder()
                        .name("Hotel Arts Barcelona [B2B · Hotel ★★★★★]")

                        .address("Carrer de la Marina 19, Barcelona")
                        .latitude(41.3870).longitude(2.1963)
                        .deliveryWindowStart("06:00").deliveryWindowEnd("08:00")
                        .parkingDifficulty("HIGH")
                        .nearestLoadingBay("Moll de Gregal — dock B")
                        .active(true).build(),
                Client.builder()
                        .name("Hilton Diagonal Mar [B2B · Hotel ★★★★★]")
                        .address("Passeig del Taulat 262, Barcelona")
                        .latitude(41.4072).longitude(2.2177)
                        .deliveryWindowStart("06:00").deliveryWindowEnd("09:00")
                        .parkingDifficulty("LOW")
                        .nearestLoadingBay("Dock càrrega Hilton posterior")
                        .active(true).build(),
// ── B2B RETAIL — Supermarkets & Convenience ─────────────
                Client.builder()
                        .name("Mercadona Eixample [B2B · Supermercat]")
                        .address("Carrer de Muntaner 80, Barcelona")
                        .latitude(41.3880).longitude(2.1530)
                        .deliveryWindowStart("07:00").deliveryWindowEnd("09:00")
                        .parkingDifficulty("MEDIUM")
                        .nearestLoadingBay("Moll posterior C/Rosselló")
                        .active(true).build(),
                Client.builder()
                        .name("Caprabo Gràcia [B2B · Supermercat]")
                        .address("Carrer Gran de Gràcia 18, Barcelona")
                        .latitude(41.3980).longitude(2.1580)
                        .deliveryWindowStart("08:00").deliveryWindowEnd("10:00")
                        .parkingDifficulty("MEDIUM")
                        .nearestLoadingBay("Zona càrrega C/Gran de Gràcia")
                        .active(true).build(),
// ── B2B INTERNATIONAL — Chains, Airport, Stadium ─────────
                Client.builder()
                        .name("Camp Nou Sky Lounge [B2B · Internacional]")
                        .address("Carrer d'Arístides Maillol 12, Barcelona")
                        .latitude(41.3809).longitude(2.1228)
                        .deliveryWindowStart("10:00").deliveryWindowEnd("14:00")
                        .parkingDifficulty("MEDIUM")
                        .nearestLoadingBay("Aparcament Camp Nou Nord — porta C")
                        .active(true).build(),
                Client.builder()
                        .name("Terminal T1 Catering [B2B · Internacional · Aeroport]")

                        .address("Aeroport del Prat, Terminal T1, Barcelona")
                        .latitude(41.2971).longitude(2.0785)
                        .deliveryWindowStart("04:00").deliveryWindowEnd("07:00")
                        .parkingDifficulty("LOW")
                        .nearestLoadingBay("Zona logística T1 — accés C-31")
                        .active(true).build(),
                Client.builder()
                        .name("Marriott Diagonal [B2B · Hotel Internacional ★★★★★]")
                        .address("Avinguda Diagonal 589, Barcelona")
                        .latitude(41.3910).longitude(2.1390)
                        .deliveryWindowStart("06:00").deliveryWindowEnd("09:00")
                        .parkingDifficulty("LOW")
                        .nearestLoadingBay("Moll càrrega posterior Marriott")
                        .active(true).build(),
                Client.builder()
                        .name("Hard Rock Café Barcelona [B2B · Internacional]")
                        .address("Plaça de Catalunya 21, Barcelona")
                        .latitude(41.3869).longitude(2.1699)
                        .deliveryWindowStart("09:00").deliveryWindowEnd("12:00")
                        .parkingDifficulty("HIGH")
                        .nearestLoadingBay("Zona càrrega C/Bergara")
                        .active(true).build(),
                Client.builder()
                        .name("Palau Sant Jordi Events [B2B · Venue Internacional]")
                        .address("Passeig Olímpic 5, Barcelona")
                        .latitude(41.3645).longitude(2.1528)
                        .deliveryWindowStart("08:00").deliveryWindowEnd("12:00")
                        .parkingDifficulty("LOW")
                        .nearestLoadingBay("Accés logístic Anella Olímpica")
                        .active(true).build(),
                Client.builder()
                        .name("W Barcelona Hotel [B2B · Hotel Internacional ★★★★★]")
                        .address("Plaça de la Rosa dels Vents 1, Barcelona")
                        .latitude(41.3713).longitude(2.1876)
                        .deliveryWindowStart("06:00").deliveryWindowEnd("09:00")
                        .parkingDifficulty("HIGH")
                        .nearestLoadingBay("Moll W Hotel — accés Port Olímpic")
                        .active(true).build()
        );
        clientRepository.saveAll(clients);
    }

    private void loadCarriers() {
        List<Carrier> carriers = List.of(
// TRUSTED — perfect record, verified documents
                Carrier.builder()
                        .name("TransBarcelona DDI SL")
                        .licenseNumber("B1234567")
                        .trustScore(92).trustLevel("TRUSTED")
                        .documentVerified(true).disputeCount(0)
                        .identityFlagged(false).active(true).build(),
// WARNING — some disputes, still usable
                Carrier.builder()
                        .name("Rutes Express Catalunya SL")
                        .licenseNumber("B7654321")
                        .trustScore(60).trustLevel("WARNING")
                        .documentVerified(true).disputeCount(2)
                        .identityFlagged(false).active(true).build(),
// BLOCKED — FraudSentinel flagged — demo the rejection
                Carrier.builder()
                        .name("Unknown Logistics SL")
                        .licenseNumber("B0000000")
                        .trustScore(20).trustLevel("BLOCKED")
                        .documentVerified(false).disputeCount(5)
                        .identityFlagged(true).active(true).build()
        );
        carrierRepository.saveAll(carriers);
    }
}