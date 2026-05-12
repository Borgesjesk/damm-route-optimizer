import { useState, useEffect, useCallback } from 'react';
import * as api from './api';
import RouteMap from './components/RouteMap';
import CarrierCard from './components/CarrierCard';
import TruckLoadingPlan from './components/TruckLoadingPlan';
import StatCard from './components/StatCard';

const SectionLabel = ({ children, right }) => (
  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '8px', color: '#333', letterSpacing: '2.5px', textTransform: 'uppercase', marginTop: '18px', marginBottom: '10px', paddingBottom: '7px', borderBottom: '1px solid #161616' }}>
    <span>{children}</span>
    {right}
  </div>
);

const ClientRow = ({ client, checked, onToggle }) => (
  <label style={{ display: 'flex', alignItems: 'flex-start', gap: '9px', padding: '8px 10px', background: checked ? '#141414' : '#0f0f0f', border: `1px solid ${checked ? '#e2001a33' : '#161616'}`, borderRadius: '6px', cursor: 'pointer', marginBottom: '4px', transition: 'border-color 0.1s, background 0.1s' }}>
    <div style={{ width: '14px', height: '14px', borderRadius: '3px', border: `1.5px solid ${checked ? '#e2001a' : '#2a2a2a'}`, background: checked ? '#e2001a' : 'transparent', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, marginTop: '1px', transition: 'background 0.1s, border-color 0.1s' }}>
      {checked && <span style={{ color: '#fff', fontSize: '9px', lineHeight: 1, fontWeight: '800' }}>✓</span>}
    </div>
    <input type="checkbox" checked={checked} onChange={onToggle} style={{ display: 'none' }} />
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontSize: '11px', fontWeight: '600', color: checked ? '#ddd' : '#777', marginBottom: '2px', fontFamily: "'Syne', sans-serif", transition: 'color 0.1s' }}>{client.name}</div>
      <div style={{ fontSize: '9px', color: '#2e2e2e', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{client.address}</div>
      {client.timeWindow && <div style={{ fontSize: '8px', color: '#2a2a2a', marginTop: '2px' }}>⏱ {client.timeWindow}</div>}
    </div>
  </label>
);

const StopRow = ({ stop, index, clientById, onConfirm, onDamage }) => {
  const [busy, setBusy] = useState(false);
  const client = clientById[stop.clientId] ?? {};
  const delivered = stop.status === 'DELIVERED';
  const name = client.name ?? stop.clientName ?? `Stop ${index + 1}`;
  const address = client.address ?? stop.address ?? '';
  const tw = stop.timeWindow ?? client.timeWindow ?? '';
  const sid = stop.stopId ?? stop.id;

  const handleConfirm = async () => {
    setBusy(true);
    await onConfirm(sid);
    setBusy(false);
  };

  return (
    <div style={{ background: delivered ? '#0b150e' : '#111', border: `1px solid ${delivered ? '#1db95428' : '#1a1a1a'}`, borderRadius: '6px', padding: '9px 10px', marginBottom: '5px' }}>
      <div style={{ display: 'flex', gap: '9px', alignItems: 'flex-start', marginBottom: delivered ? 0 : '8px' }}>
        <div style={{ width: '24px', height: '24px', borderRadius: '50%', background: delivered ? '#1db954' : '#f5c842', color: '#000', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', fontWeight: '800', flexShrink: 0 }}>{index + 1}</div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: '11px', fontWeight: '700', color: delivered ? '#1db954' : '#ccc', fontFamily: "'Syne', sans-serif", marginBottom: '2px' }}>{name}</div>
          {address && <div style={{ fontSize: '9px', color: '#333', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{address}</div>}
          {tw && <div style={{ fontSize: '8px', color: '#2a2a2a', marginTop: '1px' }}>⏱ {tw}</div>}
        </div>
        {delivered && <div style={{ fontSize: '9px', color: '#1db954', fontWeight: '800', flexShrink: 0 }}>✓</div>}
      </div>
      {!delivered && (
        <div style={{ display: 'flex', gap: '5px' }}>
          <button onClick={handleConfirm} disabled={busy} style={{ flex: 1, padding: '5px 0', background: busy ? '#0b150e' : '#1db95415', border: '1px solid #1db95435', borderRadius: '4px', color: '#1db954', fontSize: '8px', cursor: busy ? 'default' : 'pointer', fontFamily: 'monospace', letterSpacing: '1px', fontWeight: '700', transition: 'background 0.15s' }}>{busy ? '···' : '✓ CONFIRM'}</button>
          <button onClick={() => onDamage(sid)} style={{ padding: '5px 9px', background: '#e2001a0a', border: '1px solid #e2001a25', borderRadius: '4px', color: '#e2001a', fontSize: '8px', cursor: 'pointer', fontFamily: 'monospace', letterSpacing: '1px', transition: 'background 0.15s' }}>⚠ DMG</button>
        </div>
      )}
    </div>
  );
};

export default function App() {
  const [apiStatus, setApiStatus] = useState('checking');
  const [carriers, setCarriers] = useState([]);
  const [clients, setClients] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedCarrier, setSelectedCarrier] = useState(null);
  const [selectedClients, setSelectedClients] = useState([]);
  const [route, setRoute] = useState(null);
  const [loadingPlan, setLoadingPlan] = useState(null);
  const [warehouseSheet, setWarehouseSheet] = useState(null);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState(null);
  const [fraudBlocked, setFraudBlocked] = useState(false);
  const [co2Banner, setCo2Banner] = useState(null);

  useEffect(() => {
    api.checkHealth().then((r) => setApiStatus(r.ok ? 'online' : 'offline')).catch(() => setApiStatus('offline'));
        Promise.all([api.fetchCarriers().catch(() => []), api.fetchClients().catch(() => []), api.fetchDashboardStats().catch(() => null)]).then(async ([carr, cli, st]) => {
          setCarriers(carr); setClients(cli); setStats(st); setLoading(false);
          // Auto-generate route on load
          const trustedCarrier = carr.find(c => c.trustLevel === 'TRUSTED') ?? carr[0];
          const allClientIds = cli.map(c => c.id);
          if (trustedCarrier && allClientIds.length > 0) {
            setSelectedCarrier(trustedCarrier);
            setSelectedClients(allClientIds);
            try {
              const res = await api.optimiseRoute(trustedCarrier.id, allClientIds);
              if (res.ok) {
                const routeData = await res.json();
                routeData.id = routeData.routeId;
                routeData.stops = routeData.stops.map(s => ({ ...s, id: s.stopId, lat: s.latitude, lng: s.longitude }));
                setRoute(routeData);
                if (routeData.co2SavedPercent != null) { setCo2Banner(`Route optimised — ${routeData.co2SavedPercent}% less CO₂ vs. naive routing`); } else { setCo2Banner('Route successfully optimised.'); }
                const [plan] = await Promise.all([api.fetchLoadingPlan(routeData.id).catch(() => null)]);
                if (plan) { plan.items = (plan.loadingSequence ?? []).map(s => ({ order: s.loadingOrder, clientName: s.clientName, units: s.estimatedVolumeM3 + ' m³', zone: s.truckZone })); }
                setLoadingPlan(plan); setWarehouseSheet(plan ? { instructions: plan.warehouseInstructions ?? [] } : null);
              }
            } catch (e) { console.error('Auto-generate failed:', e); }
            api.fetchDashboardStats().then(setStats).catch(() => {});
          }
        });
      }, []);

  const refreshStats = useCallback(() => { api.fetchDashboardStats().then(setStats).catch(() => {}); }, []);

  const handleCarrierSelect = useCallback((carrier) => { setSelectedCarrier((prev) => (prev?.id === carrier.id ? null : carrier)); setFraudBlocked(false); setError(null); }, []);

  const handleClientToggle = useCallback((id) => { setSelectedClients((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]); }, []);

  const canGenerate = !!selectedCarrier && selectedClients.length > 0;

  const handleGenerate = useCallback(async () => {
    if (!canGenerate || generating) return;
    setGenerating(true); setError(null); setFraudBlocked(false); setCo2Banner(null);
    try {
      const res = await api.optimiseRoute(selectedCarrier.id, selectedClients);
      if (res.status === 403) { setFraudBlocked(true); return; }
      if (!res.ok) { const body = await res.json().catch(() => ({})); setError(body.message ?? `Server error ${res.status}`); return; }

      const routeData = await res.json();
      routeData.id = routeData.routeId;
      routeData.stops = routeData.stops.map(s => ({ ...s, id: s.stopId, lat: s.latitude, lng: s.longitude }));
      setRoute(routeData);

      if (routeData.co2SavedPercent != null) {
        setCo2Banner(`Route optimised — ${routeData.co2SavedPercent}% less CO₂ vs. naive routing`);
      } else {
        setCo2Banner('Route successfully optimised.');
      }

      const [plan, sheet] = await Promise.all([
        api.fetchLoadingPlan(routeData.id).catch(() => null),
        api.fetchWarehouseSheet(routeData.id).catch(() => null),
      ]);
      if (plan) { plan.items = (plan.loadingSequence ?? []).map(s => ({ order: s.loadingOrder, clientName: s.clientName, units: s.estimatedVolumeM3 + ' m³', zone: s.truckZone })); }
          setLoadingPlan(plan); setWarehouseSheet(plan ? { instructions: plan.warehouseInstructions ?? [] } : sheet); refreshStats();
    } catch (err) { setError(err.message); } finally { setGenerating(false); }
  }, [canGenerate, generating, selectedCarrier, selectedClients, refreshStats]);

  const handleConfirmDelivery = useCallback(async (stopId) => {
    if (!route) return;
    try { await api.confirmDelivery(route.id, stopId); const updated = await api.fetchRoute(route.id); updated.id = updated.routeId ?? updated.id; updated.stops = (updated.stops ?? []).map(s => ({ ...s, id: s.stopId ?? s.id, lat: s.latitude ?? s.lat, lng: s.longitude ?? s.lng })); setRoute(updated); refreshStats(); } catch (err) { console.error('confirmDelivery failed:', err); }
  }, [route, refreshStats]);

  const handleDamage = useCallback(async (stopId) => {
    if (!route) return;
    const notes = window.prompt('Describe the damage / incident:');
    if (!notes?.trim()) return;
    try { await api.reportDamage(route.id, stopId, { notes }); } catch (err) { console.error('reportDamage failed:', err); }
  }, [route]);

  const stops = route?.stops ?? [];
  const clientById = Object.fromEntries(clients.map((c) => [c.id, c]));
  const statusColor = { online: '#1db954', offline: '#e2001a', checking: '#f5c842' }[apiStatus];
  const statusPulse = apiStatus === 'online' ? '0 0 8px #1db95460' : 'none';
  const genBtnActive = canGenerate && !generating;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', width: '100vw', overflow: 'hidden', background: '#0a0a0a', color: '#fff', fontFamily: "'Courier New', Courier, monospace" }}>
      <header style={{ height: '54px', background: '#0c0c0c', borderBottom: '1px solid #181818', display: 'flex', alignItems: 'center', padding: '0 20px', justifyContent: 'space-between', flexShrink: 0, zIndex: 1000 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '13px' }}>
          <div style={{ width: '36px', height: '36px', background: '#e2001a', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: "'Syne', sans-serif", fontWeight: '900', fontSize: '18px', color: '#fff', boxShadow: '0 0 20px #e2001a50', flexShrink: 0 }}>D</div>
          <div>
            <div style={{ fontFamily: "'Syne', sans-serif", fontWeight: '800', fontSize: '16px', letterSpacing: '0.5px', color: '#fff' }}>DammRoute</div>
            <div style={{ fontSize: '8px', color: '#333', letterSpacing: '2.5px', textTransform: 'uppercase' }}>Smart Logistics · InterhackBCN 2026</div>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          {route && <div style={{ fontSize: '8px', color: '#555', letterSpacing: '1.5px', fontFamily: 'monospace', background: '#141414', border: '1px solid #222', borderRadius: '4px', padding: '4px 10px' }}>ROUTE {(route.id ?? '').toString().slice(-8).toUpperCase()}</div>}
          <div style={{ display: 'flex', alignItems: 'center', gap: '7px' }}>
            <div style={{ width: '7px', height: '7px', borderRadius: '50%', background: statusColor, boxShadow: statusPulse }} />
            <span style={{ fontSize: '9px', color: statusColor, letterSpacing: '2px', textTransform: 'uppercase', fontWeight: '700' }}>API {apiStatus}</span>
          </div>
        </div>
      </header>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        <aside style={{ width: '340px', flexShrink: 0, background: '#090909', borderRight: '1px solid #151515', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div style={{ flex: 1, overflowY: 'auto', padding: '14px 14px 24px' }}>
            <SectionLabel>Carrier Selection</SectionLabel>
            {loading ? <div style={{ color: '#2a2a2a', fontSize: '11px' }}>Loading carriers…</div> : carriers.length === 0 ? <div style={{ color: '#2a2a2a', fontSize: '11px' }}>No carriers found.</div> : carriers.map((c) => <CarrierCard key={c.id} carrier={c} selected={selectedCarrier?.id === c.id} onSelect={handleCarrierSelect} />)}

            <SectionLabel right={<div style={{ display: 'flex', gap: '10px' }}><button onClick={() => setSelectedClients(clients.map((c) => c.id))} style={{ background: 'none', border: 'none', color: '#333', fontSize: '8px', cursor: 'pointer', letterSpacing: '1px', textTransform: 'uppercase', padding: 0 }}>All</button><button onClick={() => setSelectedClients([])} style={{ background: 'none', border: 'none', color: '#333', fontSize: '8px', cursor: 'pointer', letterSpacing: '1px', textTransform: 'uppercase', padding: 0 }}>None</button></div>}>Delivery Points{clients.length > 0 ? ` (${selectedClients.length}/${clients.length})` : ''}</SectionLabel>
            {loading ? <div style={{ color: '#2a2a2a', fontSize: '11px' }}>Loading clients…</div> : clients.length === 0 ? <div style={{ color: '#2a2a2a', fontSize: '11px' }}>No clients found.</div> : clients.map((c) => <ClientRow key={c.id} client={c} checked={selectedClients.includes(c.id)} onToggle={() => handleClientToggle(c.id)} />)}

            <button onClick={handleGenerate} disabled={!genBtnActive} style={{ width: '100%', marginTop: '14px', padding: '13px', background: genBtnActive ? '#e2001a' : '#141414', color: genBtnActive ? '#fff' : '#2a2a2a', border: `1px solid ${genBtnActive ? '#e2001a' : '#1e1e1e'}`, borderRadius: '7px', fontSize: '12px', fontWeight: '800', fontFamily: "'Syne', sans-serif", letterSpacing: '1.5px', cursor: genBtnActive ? 'pointer' : 'not-allowed', transition: 'all 0.2s ease', boxShadow: genBtnActive ? '0 0 20px #e2001a35' : 'none', textTransform: 'uppercase' }}>{generating ? '⚙ OPTIMISING…' : '⚡ GENERATE ROUTE'}</button>
            {!canGenerate && !generating && <div style={{ fontSize: '8px', color: '#222', textAlign: 'center', marginTop: '7px', letterSpacing: '1px' }}>{!selectedCarrier ? 'Select a carrier first' : 'Select at least one client'}</div>}

            {fraudBlocked && <div style={{ marginTop: '12px', background: '#12060608', border: '1px solid #e2001a35', borderLeft: '3px solid #e2001a', borderRadius: '0 6px 6px 0', padding: '11px 13px' }}><div style={{ fontFamily: "'Syne', sans-serif", fontWeight: '800', fontSize: '11px', color: '#e2001a', marginBottom: '5px', letterSpacing: '0.5px' }}>⚠ FraudSentinel · Carrier Blocked</div><div style={{ fontSize: '10px', color: '#e2001a70', lineHeight: 1.55 }}>This carrier has an active FraudSentinel flag and cannot be assigned new routes. Select a <span style={{ color: '#1db954' }}>TRUSTED</span> carrier.</div></div>}
            {error && <div style={{ marginTop: '12px', background: '#110a0a', border: '1px solid #e2001a20', borderRadius: '6px', padding: '9px 12px', fontSize: '10px', color: '#e2001a80' }}>Error: {error}</div>}
            {co2Banner && <div style={{ marginTop: '12px', background: '#091309', border: '1px solid #1db95430', borderLeft: '3px solid #1db954', borderRadius: '0 6px 6px 0', padding: '9px 13px', fontSize: '11px', color: '#1db954', fontWeight: '600', lineHeight: 1.5 }}>🌱 {co2Banner}</div>}

            {stops.length > 0 && (<><SectionLabel>Route Stops ({stops.length})</SectionLabel>{stops.map((stop, i) => <StopRow key={stop.stopId ?? stop.id ?? i} stop={stop} index={i} clientById={clientById} onConfirm={handleConfirmDelivery} onDamage={handleDamage} />)}</>)}
            {(loadingPlan || warehouseSheet) && <TruckLoadingPlan loadingPlan={loadingPlan} warehouseSheet={warehouseSheet} />}
          </div>
        </aside>

        <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', minWidth: 0 }}>
          <div style={{ display: 'flex', gap: '8px', padding: '10px 12px', background: '#0c0c0c', borderBottom: '1px solid #151515', flexShrink: 0 }}>
            <StatCard label="Active Routes" value={stats?.activeRoutes} accent="#e2001a" icon="🚛" />
            <StatCard label="Deliveries Done" value={stats?.deliveriesDone} accent="#1db954" icon="📦" />
            <StatCard label="CO₂ Saved" value={stats?.co2SavedPercent} unit="%" accent="#f5c842" icon="🌱" />
            <StatCard label="Trees Equivalent" value={stats?.treesEquivalent} accent="#1db954" icon="🌳" />
          </div>
          <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
            <RouteMap stops={stops} clients={clients} />
            {route && <div style={{ position: 'absolute', top: '12px', right: '44px', zIndex: 1000, background: 'rgba(10,10,10,0.88)', backdropFilter: 'blur(10px)', border: '1px solid #222', borderRadius: '7px', padding: '10px 14px', minWidth: '170px' }}><div style={{ fontSize: '8px', color: '#e2001a', fontWeight: '800', letterSpacing: '2px', marginBottom: '6px', textTransform: 'uppercase' }}>● Route Active</div><div style={{ fontSize: '10px', color: '#666', lineHeight: 1.65 }}><div>{stops.length} stops</div><div style={{ color: '#444' }}>{selectedCarrier?.name}</div>{route.totalDistanceKm != null && <div>{route.totalDistanceKm} km</div>}{route.estimatedDuration && <div>~{route.estimatedDuration}</div>}{stops.filter((s) => s.status === 'DELIVERED').length > 0 && <div style={{ color: '#1db954', marginTop: '3px' }}>✓ {stops.filter((s) => s.status === 'DELIVERED').length} delivered</div>}</div></div>}
            <div style={{ position: 'absolute', bottom: '30px', left: '12px', zIndex: 1000, background: 'rgba(10,10,10,0.85)', backdropFilter: 'blur(8px)', border: '1px solid #1a1a1a', borderRadius: '6px', padding: '9px 13px', display: 'flex', flexDirection: 'column', gap: '5px' }}>
              {[{ color: '#e2001a', shape: 'square', label: 'DDI Mollet (Warehouse)' }, { color: '#f5c842', shape: 'circle', label: 'Pending delivery' }, { color: '#1db954', shape: 'circle', label: 'Delivered' }, { color: '#e2001a', shape: 'line', label: 'Optimised route' }].map(({ color, shape, label }) => (
                <div key={label} style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '9px', color: '#444', fontFamily: 'monospace' }}>
                  {shape === 'circle' && <div style={{ width: '9px', height: '9px', borderRadius: '50%', background: color, flexShrink: 0 }} />}
                  {shape === 'square' && <div style={{ width: '9px', height: '9px', borderRadius: '2px', background: color, flexShrink: 0 }} />}
                  {shape === 'line' && <div style={{ width: '18px', height: '2px', background: `repeating-linear-gradient(90deg, ${color} 0, ${color} 5px, transparent 5px, transparent 8px)`, flexShrink: 0 }} />}
                  <span>{label}</span>
                </div>
              ))}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}