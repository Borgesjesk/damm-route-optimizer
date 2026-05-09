import { useState, useEffect, useCallback } from 'react';
import { RouteMap } from './components/RouteMap';
import { StatCard } from './components/StatCard';
import { CarrierCard } from './components/CarrierCard';
import { api } from './api';

// ── STYLES ─────────────────────────────────────────────────────────────
const S = {
  app: {
    display: 'grid',
    gridTemplateRows: 'auto 1fr',
    height: '100vh',
    background: '#0a0a0a',
    color: '#f5f0e8',
    fontFamily: '"Courier New", monospace',
  },
  header: {
    background: '#111',
    borderBottom: '1px solid #222',
    padding: '0 24px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    height: 56,
  },
  logo: {
    fontFamily: '"Courier New", monospace',
    fontSize: 20,
    fontWeight: 700,
    letterSpacing: -1,
  },
  body: {
    display: 'grid',
    gridTemplateColumns: '340px 1fr',
    overflow: 'hidden',
  },
  sidebar: {
    background: '#111',
    borderRight: '1px solid #1a1a1a',
    overflowY: 'auto',
    display: 'flex',
    flexDirection: 'column',
    gap: 0,
  },
  section: {
    borderBottom: '1px solid #1a1a1a',
    padding: '16px',
  },
  sectionTitle: {
    fontSize: 10,
    letterSpacing: 3,
    textTransform: 'uppercase',
    color: '#f5c842',
    marginBottom: 12,
  },
  mapWrapper: {
    position: 'relative',
    display: 'flex',
    flexDirection: 'column',
  },
  statsBar: {
    display: 'grid',
    gridTemplateColumns: 'repeat(4, 1fr)',
    gap: 1,
    background: '#0a0a0a',
  },
  btn: (variant = 'primary', disabled = false) => ({
    padding: '10px 16px',
    background: disabled ? '#1a1a1a' : variant === 'primary' ? '#e2001a' : '#1a1a2a',
    color: disabled ? '#555' : '#fff',
    border: 'none',
    cursor: disabled ? 'not-allowed' : 'pointer',
    fontSize: 12,
    fontFamily: '"Courier New", monospace',
    fontWeight: 700,
    letterSpacing: 1,
    textTransform: 'uppercase',
    width: '100%',
    transition: 'background 0.2s',
  }),
  alert: (type = 'error') => ({
    padding: '10px 14px',
    background: type === 'error' ? '#1a0000' : '#001a00',
    borderLeft: `3px solid ${type === 'error' ? '#e63329' : '#1db954'}`,
    fontSize: 12,
    color: type === 'error' ? '#ff9a9a' : '#9af0b8',
    margin: '8px 0',
  }),
  stopRow: (status) => ({
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '8px 12px',
    background: '#1a1a1a',
    marginBottom: 2,
    fontSize: 12,
    borderLeft: `2px solid ${
      status === 'DELIVERED' ? '#1db954' :
      status === 'FAILED'    ? '#e63329' : '#f5c842'
    }`,
  }),
};

// ── APP ────────────────────────────────────────────────────────────────
export default function App() {
  const [clients,         setClients]         = useState([]);
  const [carriers,        setCarriers]         = useState([]);
  const [dashboard,       setDashboard]        = useState(null);
  const [selectedCarrier, setSelectedCarrier]  = useState(null);
  const [selectedClients, setSelectedClients]  = useState([]);
  const [currentRoute,    setCurrentRoute]     = useState(null);
  const [loading,         setLoading]          = useState(false);
  const [error,           setError]            = useState('');
  const [success,         setSuccess]          = useState('');
  const [apiStatus,       setApiStatus]        = useState('checking');

  // ── Load initial data ──────────────────────────────────────────────
  const loadAll = useCallback(async () => {
    try {
      await api.health();
      setApiStatus('online');
      const [c, car, dash] = await Promise.all([
        api.getClients(),
        api.getCarriers(),
        api.getDashboard(),
      ]);
      setClients(c);
      setCarriers(car);
      setDashboard(dash);
    } catch (e) {
      setApiStatus('offline');
      setError('Cannot connect to DammRoute API. Make sure Spring Boot is running on :8080');
    }
  }, []);

  useEffect(() => { loadAll(); }, [loadAll]);

  // ── Toggle client selection ────────────────────────────────────────
  const toggleClient = (client) => {
    setSelectedClients(prev =>
      prev.find(c => c.id === client.id)
        ? prev.filter(c => c.id !== client.id)
        : [...prev, client]
    );
  };

  const selectAllClients = () => {
    setSelectedClients(selectedClients.length === clients.length ? [] : [...clients]);
  };

  // ── Optimise route ─────────────────────────────────────────────────
  const optimiseRoute = async () => {
    if (!selectedCarrier) { setError('Select a carrier first'); return; }
    if (selectedClients.length === 0) { setError('Select at least one client'); return; }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const route = await api.optimiseRoute(
        selectedCarrier.id,
        selectedClients.map(c => c.id)
      );
      setCurrentRoute(route);
      setSuccess(
        `✅ Route optimised! CO₂ saved: ${route.co2SavedPercent}% ` +
        `(${(route.baselineCo2Kg - route.optimisedCo2Kg).toFixed(1)}kg) ` +
        `≈ ${route.treesEquivalent} trees`
      );
      await loadAll(); // refresh dashboard
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  // ── Mark stop delivered ────────────────────────────────────────────
  const completeStop = async (stopId) => {
    if (!currentRoute) return;
    try {
      const updated = await api.completeStop(currentRoute.routeId, stopId);
      setCurrentRoute(updated);
      await loadAll();
    } catch (e) {
      setError(e.message);
    }
  };

  // ── CO2 bar visual ─────────────────────────────────────────────────
  const Co2Bar = ({ baseline, optimised, savedPct }) => (
    <div style={{ marginTop: 8 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, color: '#888', marginBottom: 4 }}>
        <span>Unoptimised: {baseline?.toFixed(1)}kg CO₂</span>
        <span style={{ color: '#1db954' }}>Saved: {savedPct}%</span>
      </div>
      <div style={{ height: 8, background: '#0a0a0a', position: 'relative' }}>
        <div style={{ height: '100%', width: '100%', background: '#e63329', opacity: 0.3 }} />
        <div style={{
          height: '100%',
          width: `${100 - savedPct}%`,
          background: '#1db954',
          position: 'absolute', top: 0, left: 0,
        }} />
      </div>
      <div style={{ fontSize: 10, color: '#1db954', marginTop: 4 }}>
        Optimised: {optimised?.toFixed(1)}kg CO₂
      </div>
    </div>
  );

  // ── RENDER ─────────────────────────────────────────────────────────
  return (
    <div style={S.app}>

      {/* HEADER */}
      <header style={S.header}>
        <div style={S.logo}>
          <span style={{ color: '#e2001a' }}>Damm</span>Route
          <span style={{ fontSize: 11, color: '#555', marginLeft: 12, fontWeight: 400 }}>
            Smart Delivery Barcelona 2030
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span style={{
            fontSize: 10,
            padding: '4px 10px',
            background: apiStatus === 'online' ? '#001a00' : '#1a0000',
            color: apiStatus === 'online' ? '#1db954' : '#e63329',
            letterSpacing: 1,
          }}>
            ● API {apiStatus.toUpperCase()}
          </span>
          <span style={{ fontSize: 10, color: '#555' }}>🛡️ FraudSentinel Active</span>
        </div>
      </header>

      <div style={S.body}>

        {/* SIDEBAR */}
        <aside style={S.sidebar}>

          {/* STEP 1 — CARRIERS */}
          <div style={S.section}>
            <div style={S.sectionTitle}>Step 1 — Select Carrier</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              {carriers.map(carrier => (
                <CarrierCard
                  key={carrier.id}
                  carrier={carrier}
                  selected={selectedCarrier?.id === carrier.id}
                  onSelect={setSelectedCarrier}
                />
              ))}
            </div>
          </div>

          {/* STEP 2 — CLIENTS */}
          <div style={S.section}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <div style={S.sectionTitle}>Step 2 — Select Stops</div>
              <button
                style={{ ...S.btn('secondary'), width: 'auto', padding: '4px 10px', fontSize: 10 }}
                onClick={selectAllClients}
              >
                {selectedClients.length === clients.length ? 'Deselect All' : 'Select All'}
              </button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              {clients.map(client => {
                const isSelected = selectedClients.find(c => c.id === client.id);
                return (
                  <div
                    key={client.id}
                    onClick={() => toggleClient(client)}
                    style={{
                      padding: '8px 12px',
                      background: isSelected ? '#1a1a00' : '#1a1a1a',
                      border: `1px solid ${isSelected ? '#f5c842' : '#222'}`,
                      cursor: 'pointer',
                      fontSize: 12,
                      transition: 'all 0.15s',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: isSelected ? '#f5c842' : '#bbb' }}>
                        {isSelected ? '☑' : '☐'} {client.name}
                      </span>
                      <span style={{
                        fontSize: 10,
                        color: client.parkingDifficulty === 'HIGH' ? '#e63329' :
                               client.parkingDifficulty === 'MEDIUM' ? '#f5c842' : '#1db954',
                      }}>
                        🅿️ {client.parkingDifficulty}
                      </span>
                    </div>
                    <div style={{ fontSize: 10, color: '#555', marginTop: 2 }}>
                      ⏰ {client.deliveryWindowStart}–{client.deliveryWindowEnd}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* STEP 3 — OPTIMISE */}
          <div style={S.section}>
            <div style={S.sectionTitle}>Step 3 — Optimise</div>
            {error && <div style={S.alert('error')}>{error}</div>}
            {success && <div style={S.alert('success')}>{success}</div>}
            <button
              style={S.btn('primary', loading || !selectedCarrier || selectedClients.length === 0)}
              onClick={optimiseRoute}
              disabled={loading || !selectedCarrier || selectedClients.length === 0}
            >
              {loading ? '⏳ Calculating...' : '🚀 Generate Optimised Route'}
            </button>
            {currentRoute && (
              <Co2Bar
                baseline={currentRoute.baselineCo2Kg}
                optimised={currentRoute.optimisedCo2Kg}
                savedPct={currentRoute.co2SavedPercent}
              />
            )}
          </div>

          {/* ROUTE STOPS */}
          {currentRoute && (
            <div style={S.section}>
              <div style={S.sectionTitle}>
                Route #{currentRoute.routeId} — {currentRoute.carrierName}
              </div>
              {currentRoute.stops
                .sort((a, b) => a.stopOrder - b.stopOrder)
                .map(stop => (
                  <div key={stop.stopId} style={S.stopRow(stop.status)}>
                    <span style={{ color: '#f5c842', fontWeight: 700, minWidth: 20 }}>
                      {stop.stopOrder}
                    </span>
                    <div style={{ flex: 1 }}>
                      <div style={{ color: '#ddd' }}>{stop.clientName}</div>
                      <div style={{ fontSize: 10, color: '#555' }}>
                        ETA {stop.estimatedArrival} · {stop.parkingDifficulty}
                      </div>
                    </div>
                    {stop.status === 'PENDING' && (
                      <button
                        onClick={() => completeStop(stop.stopId)}
                        style={{
                          background: '#001a00',
                          color: '#1db954',
                          border: '1px solid #1db954',
                          padding: '3px 8px',
                          fontSize: 10,
                          cursor: 'pointer',
                          fontFamily: 'inherit',
                        }}
                      >
                        ✓
                      </button>
                    )}
                    {stop.status === 'DELIVERED' && (
                      <span style={{ color: '#1db954', fontSize: 12 }}>✓</span>
                    )}
                  </div>
                ))}
            </div>
          )}
        </aside>

        {/* MAP + STATS */}
        <main style={S.mapWrapper}>
          {/* Stats bar */}
          <div style={S.statsBar}>
            <StatCard
              label="Active Routes"
              value={dashboard?.activeRoutes ?? '—'}
              icon="🚚"
              color="#f5c842"
            />
            <StatCard
              label="Deliveries Done"
              value={dashboard?.completedDeliveries ?? '—'}
              icon="✅"
              color="#1db954"
            />
            <StatCard
              label="CO₂ Saved"
              value={currentRoute?.co2SavedPercent ?? '—'}
              unit="%"
              icon="🌿"
              color="#1db954"
            />
            <StatCard
              label="Trees Equivalent"
              value={currentRoute?.treesEquivalent ?? '—'}
              icon="🌳"
              color="#1db954"
            />
          </div>

          {/* Map */}
          <div style={{ flex: 1 }}>
            <RouteMap
              clients={clients}
              route={currentRoute ? {
                stops: currentRoute.stops.map(s => ({
                  ...s,
                  latitude:  clients.find(c => c.name === s.clientName)?.latitude,
                  longitude: clients.find(c => c.name === s.clientName)?.longitude,
                }))
              } : null}
            />
          </div>
        </main>
      </div>
    </div>
  );
}
