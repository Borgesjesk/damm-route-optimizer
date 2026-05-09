# DammRoute 🍺

> Smart last-mile distribution platform for Damm Barcelona.  
> Built at InterhackBCN 2026 in 24 hours.

## What it does

DammRoute optimises Damm's beer delivery routes across Barcelona:
- **Route optimization** — nearest-neighbour algorithm respecting client time windows
- **Parking intelligence** — pre-loads loading bay info for every stop
- **CarrierTrust Score** — FraudSentinel integration blocks unverified carriers
- **CO₂ calculator** — proves emissions savings vs unoptimised baseline

## Tech Stack

- Java 21 + Spring Boot 3.3.5
- Spring Security (API Key authentication)
- Spring Data JPA + H2 (demo database)
- Maven

## Run the project

```bash
./mvnw spring-boot:run
```

API available at: `http://localhost:8080`  
H2 console: `http://localhost:8080/h2-console`

## Authentication

All `/api/**` endpoints require header:
```
X-API-Key: dammroute-hackathon-2026
```

## Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/routes/optimise` | Create optimised route |
| GET | `/api/routes/{id}` | Get route details |
| PUT | `/api/routes/{routeId}/stops/{stopId}/complete` | Mark stop delivered |
| GET | `/api/carriers` | List all carriers with trust scores |
| GET | `/api/clients` | List all Damm clients |
| GET | `/api/dashboard/stats` | Live dashboard stats |
| GET | `/api/health` | Health check (no auth needed) |

## Example Request

```json
POST /api/routes/optimise
X-API-Key: dammroute-hackathon-2026

{
  "carrierId": 1,
  "clientIds": [1, 2, 3, 4, 5]
}
```

## Run Tests

```bash
./mvnw test
```

## Author

Jessica Silva Borges — InterhackBCN 2026  
github.com/Borgesjesk
