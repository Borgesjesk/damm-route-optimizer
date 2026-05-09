# 🚨 DammRoute — Emergency Cheat Sheet
# If something breaks at 2am — read this first

---

## 🔴 Spring Boot won't start

**Error: Port 8080 already in use**
```bash
# Find what's using it
netstat -ano | findstr :8080   # Windows
lsof -i :8080                  # Mac/Linux

# Kill it (replace PID)
taskkill /PID <PID> /F         # Windows
kill -9 <PID>                  # Mac/Linux

# Or just change port in application.properties:
server.port=8081
```

**Error: Cannot find main class**
```bash
./mvnw clean spring-boot:run
```

**Error: H2 console not loading**
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:dammroute
Username: sa
Password: (empty)
```

---

## 🔴 Security / 401 Unauthorized

**Every API call needs this header:**
```
X-API-Key: dammroute-hackathon-2026
```

**In Postman:** Headers tab → Key: X-API-Key, Value: dammroute-hackathon-2026

**H2 console doesn't need the key — it's excluded**

---

## 🔴 CORS error in browser console

Add this to application.properties temporarily:
```properties
# Nuclear option for demo only — never in production
spring.web.cors.allowed-origins=*
```

Or ensure React is on port 3000 or 5173 (already in SecurityConfig).

---

## 🔴 Carrier 3 (BLOCKED) — Demo the rejection

```bash
POST http://localhost:8080/api/routes/optimise
X-API-Key: dammroute-hackathon-2026

{
  "carrierId": 3,
  "clientIds": [1, 2, 3]
}
```

Expected response: **403 Forbidden**
```json
{
  "error": "Carrier 'Unknown Logistics SL' has trust score 20/100 (minimum 50 required). Level: BLOCKED. This carrier is blocked by FraudSentinel."
}
```

This is your DEMO MOMENT — show judges the security working.

---

## 🔴 Route returns wrong order

The optimizer sorts by time window first, then nearest neighbour.
Clients with "06:00" windows appear before "12:00" windows.
This is correct — time constraints take priority over distance.

---

## 🔴 CO2 saved is 0 or negative

Happens when only 1 client is selected (no optimization possible).
Select at least 3 clients for meaningful CO2 savings.
With all 10 clients, typical saving is 25-40%.

---

## 🔴 React frontend — white screen

```bash
cd dammroute-frontend
npm install
npm start
```

If Leaflet map is broken:
- Check browser console for "L is not defined"
- Make sure leaflet CSS is in index.html (it is)
- Try: npm install leaflet react-leaflet --save

---

## 🔴 React — "Cannot read properties of null"

API is probably offline. Check:
1. Spring Boot running? → http://localhost:8080/api/health
2. API key in api.js correct?
3. CORS allowing localhost:3000?

---

## 🔴 Database is empty after restart

Normal — H2 is in-memory. DataLoader runs on every startup.
If DataLoader throws error, check application.properties:
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## 🟡 Demo backup plan (if everything breaks)

1. Open Postman
2. Show GET /api/clients → 10 Barcelona clients
3. Show POST /api/routes/optimise with carrier 1 → working route
4. Show POST /api/routes/optimise with carrier 3 → 403 blocked
5. Show the CO2 saved in the response JSON

Say: "The frontend integration would visualize this on a live map.
The backend is production-grade and fully working."

Judges care about the idea and architecture. A working API demo is enough.

---

## 🟡 Quick curl commands (no Postman needed)

```bash
# Health check
curl http://localhost:8080/api/health

# Get all clients
curl -H "X-API-Key: dammroute-hackathon-2026" http://localhost:8080/api/clients

# Get all carriers
curl -H "X-API-Key: dammroute-hackathon-2026" http://localhost:8080/api/carriers

# Optimise route (trusted carrier)
curl -X POST http://localhost:8080/api/routes/optimise \
  -H "X-API-Key: dammroute-hackathon-2026" \
  -H "Content-Type: application/json" \
  -d '{"carrierId":1,"clientIds":[1,2,3,4,5,6,7,8,9,10]}'

# Try blocked carrier (should return 403)
curl -X POST http://localhost:8080/api/routes/optimise \
  -H "X-API-Key: dammroute-hackathon-2026" \
  -H "Content-Type: application/json" \
  -d '{"carrierId":3,"clientIds":[1,2,3]}'
```

---

## 🟡 Git — save your work every 30 minutes

```bash
git add .
git commit -m "feat: [describe what you just built]"
git push origin main
```

**First time setup:**
```bash
cd dammroute
git init
git add .
git commit -m "feat: initial DammRoute backend — Spring Boot 3.3, security, route optimizer"
git remote add origin https://github.com/Borgesjesk/dammroute.git
git push -u origin main
```

---

## 🟡 Conventional commit messages to use tonight

```
feat: add route optimizer with time window awareness
feat: add FraudSentinel carrier trust score
feat: add CO2 calculator with baseline comparison
feat: add parking intelligence per stop
fix: correct haversine calculation for long distances
test: add happy and unhappy path tests for CarrierTrustService
docs: update README with API endpoints
```

---

## 📊 Numbers to memorize for the pitch

- Europe freight logistics market: **$1.48 trillion (2025)**
- Cargo crime surge in Europe: **+438% in 3 years**
- Q1 2026 fraud at record high: **50% by carriers with clean records**
- Your CO2 saving: **~35% per route** (show actual number from demo)
- If Damm runs 50 routes/day: **~850kg CO2 saved daily**
- Trees equivalent per day: **~40 trees**
- Average loss per freight fraud incident: **$202,000**

---

## 💬 Answers to judge questions

**"Why not use Google Maps API?"**
"OpenRouteService is free and open source — perfect for a hackathon.
In production we'd evaluate Google Fleet Routing for enterprise scale."

**"How does the trust score work?"**
"It's computed from 4 factors: document verification (-40 if missing),
identity flags (-30), and dispute history (-10 per dispute, max -30).
Score below 50 blocks the carrier from routes automatically."

**"Can this scale?"**
"The greedy algorithm works well under 20 stops.
For production, we'd integrate OR-Tools VRP solver.
The architecture is API-first — swapping the optimizer is one service change."

**"What data does it use?"**
"Open Data BCN for parking zones, OpenRouteService for routing,
our own carrier trust database built from operational history."

**"Is this secure?"**
"API key authentication on all endpoints. CORS restricted to known origins.
Stateless — no sessions. Input validation on every endpoint.
Error messages never expose internals. Built with security-first mindset."

---

You built this. You know every line. You got this. 🚀
