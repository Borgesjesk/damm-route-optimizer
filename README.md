# 🚛 DammRoute — Smart Logistics Platform

> **InterhackBCN 2026 · Damm Challenge**
> Parking-first route optimization for beer distribution in Catalonia

![DammRoute Demo](docs/demo-map.png)

---

## 🎯 The Problem

Damm's distribution trucks waste time circling for parking in narrow Catalan streets. Drivers don't know where to park, which deliveries to group, or how to minimize CO₂ emissions across their route.

## 💡 Our Solution

**DammRoute** optimizes delivery routes by **parking zones first** — not individual clients. The algorithm:

1. **Groups clients by nearest loading bay** — clients sharing a parking zone are clustered together
2. **Routes between parking nodes** using greedy nearest-neighbor with time window awareness
3. **The driver parks once per zone** and delivers to all clients on foot
4. **Tracks CO₂ savings** comparing optimized vs. naive routing

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    FRONTEND                          │
│         React · Leaflet · Dark Theme                 │
│   Map + Parking Markers + Delivery Tracking          │
├─────────────────────────────────────────────────────┤
│                   REST API                           │
│              Spring Boot 3.3.5                       │
│     /api/clients · /api/carriers · /api/routes       │
├─────────────────────────────────────────────────────┤
│               SERVICES                               │
│  RouteOptimizerService (parking-first algorithm)     │
│  FraudSentinel (carrier trust scoring)               │
│  Co2CalculatorService (haversine + emissions)        │
│  WarehouseService (loading sheets + damage reports)  │
├─────────────────────────────────────────────────────┤
│         PYTHON OPTIMIZER (2-Opt + Clustering)        │
│  Google Maps Distance Matrix · 1000 candidate routes │
├─────────────────────────────────────────────────────┤
│              H2 IN-MEMORY DB                         │
│  Clients · Carriers · Routes · Stops · Alerts        │
└─────────────────────────────────────────────────────┘
```

---

## 🔑 Key Features

### 🅿️ Parking-First Route Optimization
Clients are grouped by their nearest loading bay. The truck navigates between parking zones, not individual addresses. The driver walks to deliver within each zone.

### 🧮 2-Opt Mathematical Optimizer
A Python-based optimizer that generates 1,000 candidate routes per truck cluster and evaluates real-time cost including customer availability, truck loading constraints, and warehouse efficiency. See [Algorithm Details](#-algorithm-2-opt-mathematical-optimizer) below.

### 🛡️ FraudSentinel — Carrier Trust Scoring
Each carrier has a trust score computed from document verification, identity flags, and dispute history. Carriers below threshold 50 are **automatically blocked** from new routes (HTTP 403).

### 🌱 CO₂ Calculator
Every route compares optimized distance vs. naive routing and calculates CO₂ savings in kg, percentage, and tree equivalents.

### 📦 Warehouse Operations — LIFO Loading
Loading sheets with LIFO (Last In, First Out) truck zone allocation: BACK → MIDDLE → FRONT. Each stop includes delivery volume, return volume for empty crates, and parking instructions. Delivery confirmation, damage reports, and incident alerts — all tracked per stop.

### 🗺️ Live Dashboard
Dark-themed React dashboard with Leaflet map showing:
- 🔴 Warehouse (DDI Mollet del Vallès)
- 🟡 Pending delivery stops
- 🟢 Delivered stops
- 🔵 Parking zone markers with client lists
- 🔴 Optimized route line

---

## 🧮 Algorithm: 2-Opt Mathematical Optimizer

### Input Data
- Customer names and locations
- Full order per customer: items and quantities
- Distance matrix from Google Maps API

### How it Works

**Step 1 — Clustering:** Groups customers into clusters that can fill a truck. These groups are selected to minimize the distance between them using the Google Maps Distance Matrix API for real driving distances.

**Step 2 — Route Generation:** For each group of customers, generates **1,000 candidate routes** using 2-Opt optimization. These routes minimize the total distance traveled. At this stage, customer availability and truck distribution are not yet considered.

**Step 3 — Real-Time Cost Evaluation:** For each candidate route, calculates the real-time cost. It takes into account customer availability windows, truck loading/distribution constraints, and warehouse efficiency.

### Output
- Best route for each truck: `WAREHOUSE → Shop 1 → Shop 2 → ...`
- Distribution of items on each pallet and within the truck

### Cluster Visualization

#### Truck 1 — 19 Deliveries · 106 min estimated
![Truck 1 Route](docs/graph1.png)

#### Truck 2 — 12 Deliveries · 114 min estimated
![Truck 2 Route](docs/graph2.png)

#### Truck 3 — 19 Deliveries · 125 min estimated
![Truck 3 Route](docs/graph3.png)

### Java Fallback: Parking-First Greedy

When the Python optimizer is unavailable, the Java backend activates a parking-first greedy algorithm automatically:

```
1. INPUT: List of clients with coordinates + nearestLoadingBay

2. CLUSTER: Group clients by shared parking zone (nearestLoadingBay)
   Example: 5 clients on C/ Gran → 1 parking cluster

3. ORDER CLUSTERS: Greedy nearest-neighbor between cluster centers
   Starting from DDI Mollet warehouse → nearest cluster → next → ...

4. WITHIN CLUSTER: Sort by delivery time window
   Earliest window first within each parking zone

5. OUTPUT: Ordered list of stops, grouped by parking zone
   Driver parks once per zone, delivers on foot
```

---

## 📸 Screenshots

### Route Map with Parking Zones
![Route Map](docs/demo-map.png)

### API Response — Postman
![Postman](docs/demo-postman.png)

### H2 Database Schema
![H2 Console](docs/demo-h2.png)

---

## 🛠️ Tech Stack

| Layer      | Technology                                      |
|------------|------------------------------------------------|
| Backend    | Java 21 · Spring Boot 3.3.5 · Spring Security |
| Optimizer  | Python · NetworkX · 2-Opt · Google Maps API    |
| Database   | H2 (in-memory) · JPA / Hibernate              |
| Frontend   | React · Leaflet · CARTO dark tiles            |
| API        | REST · JSON · API Key authentication           |
| Build      | Maven · npm                                    |

---

## 🚀 Quick Start

### Backend
```bash
cd dammroute
./mvnw spring-boot:run
# or in IntelliJ: Run DammRouteApplication.java
```

### Frontend
```bash
cd frontend
npm install
npm start
```

- **Backend:** http://localhost:8080
- **Frontend:** http://localhost:3000
- **H2 Console:** http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:dammroute`, user: `sa`)

---

## 📡 API Endpoints

| Method | Endpoint                                  | Description                    |
|--------|------------------------------------------|--------------------------------|
| GET    | `/api/health`                            | Health check                   |
| GET    | `/api/clients`                           | List all delivery points       |
| GET    | `/api/carriers`                          | List all carriers              |
| POST   | `/api/routes/optimise`                   | Generate optimized route       |
| GET    | `/api/routes/{id}`                       | Get route details              |
| GET    | `/api/routes/{id}/loading-plan`          | Truck loading sheet (LIFO)     |
| GET    | `/api/routes/{id}/warehouse-sheet`       | Warehouse pick list            |
| POST   | `/api/routes/{id}/stops/{stopId}/confirm-delivery` | Confirm delivery   |
| POST   | `/api/routes/{id}/stops/{stopId}/damage` | Report damage                  |
| GET    | `/api/dashboard/stats`                   | Dashboard statistics           |

---

## 👥 Team

| Name | Role |
|------|------|
| **Jess Borges** | Backend Development · Route Optimizer · FraudSentinel · Architecture |
| **Eduardo Moraga** | Mathematical Optimization · 2-Opt Algorithm · Google Maps Integration |
| **Tomàs Corcho** | Data Analysis · Clustering · Route Cost Evaluation |

---

## 📊 Impact Numbers

- **Parking events reduced:** ~60% fewer stops (cluster vs. individual)
- **CO₂ savings:** ~4-35% per route vs. naive routing
- **Route distance:** 141.75 km optimized for 14 delivery points
- **Candidate routes evaluated:** 1,000 per truck cluster
- **Fraud prevention:** Automated carrier blocking via trust scoring

---

## 📄 License

Built for InterhackBCN 2026 — Damm Challenge.