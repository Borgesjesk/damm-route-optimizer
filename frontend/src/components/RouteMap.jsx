import { useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';

const WAREHOUSE = [41.5432, 2.2089];
const TILE_URL = 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png';
const TILE_ATTR = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>';

const warehouseIcon = L.divIcon({
  className: '',
  html: `<div style="width:42px;height:42px;border-radius:8px;background:#e2001a;display:flex;align-items:center;justify-content:center;font-size:22px;border:2px solid rgba(255,255,255,0.25);box-shadow:0 0 24px #e2001a70,0 4px 20px rgba(0,0,0,0.9);position:relative;">🏭<div style="position:absolute;bottom:-2px;left:50%;transform:translateX(-50%);width:0;height:0;border-left:6px solid transparent;border-right:6px solid transparent;border-top:7px solid #e2001a;"></div></div>`,
  iconSize: [42, 49], iconAnchor: [21, 49], popupAnchor: [0, -52],
});

function makeParkingIcon() {
  return L.divIcon({
    className: '',
    html: `<div style="width:36px;height:36px;border-radius:6px;background:#2563eb;color:#fff;display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:900;font-family:'Courier New',monospace;border:2px solid rgba(255,255,255,0.35);box-shadow:0 0 18px #2563eb60,0 3px 12px rgba(0,0,0,0.7);position:relative;">🅿<div style="position:absolute;bottom:-2px;left:50%;transform:translateX(-50%);width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-top:6px solid #2563eb;"></div></div>`,
    iconSize: [36, 42], iconAnchor: [18, 42], popupAnchor: [0, -46],
  });
}

function makeStopIcon(num, delivered) {
  const bg = delivered ? '#1db954' : '#f5c842';
  const shadow = delivered ? '#1db95460' : '#f5c84260';
  return L.divIcon({
    className: '',
    html: `<div style="width:32px;height:32px;border-radius:50%;background:${bg};color:#000;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:800;font-family:'Courier New',monospace;border:2px solid rgba(255,255,255,0.3);box-shadow:0 0 14px ${shadow},0 3px 10px rgba(0,0,0,0.7);position:relative;">${num}<div style="position:absolute;bottom:-2px;left:50%;transform:translateX(-50%);width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-top:6px solid ${bg};"></div></div>`,
    iconSize: [32, 38], iconAnchor: [16, 38], popupAnchor: [0, -42],
  });
}

function MapBoundsController({ stops }) {
  const map = useMap();
  useEffect(() => {
    if (!stops || stops.length === 0) return;
    const valid = stops.filter((s) => s.lat != null && s.lng != null);
    if (!valid.length) return;
    const points = [WAREHOUSE, ...valid.map((s) => [s.lat, s.lng])];
    try { map.fitBounds(L.latLngBounds(points), { padding: [64, 64], maxZoom: 15, animate: true, duration: 0.9 }); } catch (_) {}
  }, [stops, map]);
  return null;
}

const RouteMap = ({ stops = [], clients = [] }) => {
  const clientById = useMemo(() => Object.fromEntries(clients.map((c) => [c.id, c])), [clients]);

  const validStops = useMemo(() => stops.filter((s) => s.lat != null && s.lng != null), [stops]);

  const routePositions = useMemo(() => {
    if (!validStops.length) return [];
    return [WAREHOUSE, ...validStops.map((s) => [s.lat, s.lng]), WAREHOUSE];
  }, [validStops]);

  // Group stops by parking instruction to create parking markers
  const parkingSpots = useMemo(() => {
    const clusters = {};
    validStops.forEach((stop) => {
      const key = stop.parkingInstruction ?? stop.parkingNotes ?? null;
      if (!key) return;
      // Extract just the loading bay name (before the | Difficulty part)
      const bayName = key.split('|')[0].replace('Loading bay:', '').trim();
      if (!clusters[bayName]) {
        clusters[bayName] = { name: bayName, fullInstruction: key, stops: [], lats: [], lngs: [] };
      }
      clusters[bayName].stops.push(stop);
      clusters[bayName].lats.push(stop.lat);
      clusters[bayName].lngs.push(stop.lng);
    });
    // Compute center of each cluster and offset slightly so parking icon doesn't overlap stops
    return Object.values(clusters).map((c) => ({
      name: c.name,
      fullInstruction: c.fullInstruction,
      clientNames: c.stops.map(s => s.clientName ?? 'Client'),
      lat: c.lats.reduce((a, b) => a + b, 0) / c.lats.length + 0.0003,
      lng: c.lngs.reduce((a, b) => a + b, 0) / c.lngs.length - 0.0005,
      count: c.stops.length,
    }));
  }, [validStops]);

  return (
    <MapContainer center={WAREHOUSE} zoom={13} style={{ width: '100%', height: '100%' }} zoomControl={true} attributionControl={true}>
      <TileLayer url={TILE_URL} attribution={TILE_ATTR} />
      <MapBoundsController stops={validStops} />

      <Marker position={WAREHOUSE} icon={warehouseIcon}>
        <Popup>
          <strong>DDI Mollet del Vallès</strong><br />
          Carrer de la Indústria<br />
          08100 Mollet del Vallès<br />
          <span style={{ color: '#e2001a', fontWeight: '700' }}>● Damm Distribution Hub</span>
        </Popup>
      </Marker>

      {routePositions.length > 1 && (
        <Polyline positions={routePositions} pathOptions={{ color: '#e2001a', weight: 2.5, opacity: 0.75, dashArray: '10, 7' }} />
      )}

      {/* PARKING SPOT MARKERS */}
      {parkingSpots.map((spot, i) => (
        <Marker key={`park-${i}`} position={[spot.lat, spot.lng]} icon={makeParkingIcon()}>
          <Popup>
            <strong style={{ color: '#2563eb' }}>🅿 PARKING ZONE</strong><br />
            <span style={{ fontSize: '12px' }}>{spot.name}</span><br />
            <span style={{ color: '#888', fontSize: '11px' }}>{spot.count} deliveries from here:</span><br />
            {spot.clientNames.map((n, j) => (
              <span key={j} style={{ fontSize: '11px', color: '#ccc' }}>• {n}<br /></span>
            ))}
          </Popup>
        </Marker>
      ))}

      {/* STOP MARKERS */}
      {validStops.map((stop, i) => {
        const client = clientById[stop.clientId] ?? {};
        const delivered = stop.status === 'DELIVERED';
        const clientName = client.name ?? stop.clientName ?? `Stop ${i + 1}`;
        const address = client.address ?? stop.address ?? '';
        const timeWindow = stop.timeWindow ?? client.timeWindow ?? '';
        const parking = stop.parkingInstruction ?? stop.parkingNotes ?? client.parkingNotes ?? '';

        return (
          <Marker key={stop.id ?? i} position={[stop.lat, stop.lng]} icon={makeStopIcon(i + 1, delivered)}>
            <Popup>
              <strong>{clientName}</strong><br />
              {address && <>{address}<br /></>}
              {timeWindow && <><span style={{ color: '#f5c842' }}>⏱ {timeWindow}</span><br /></>}
              {parking && <><span style={{ color: '#2563eb' }}>🅿 {parking}</span><br /></>}
              <span style={{ color: delivered ? '#1db954' : '#f5c842', fontWeight: '700', marginTop: '4px', display: 'inline-block' }}>{delivered ? '✓ DELIVERED' : '⏳ PENDING'}</span>
            </Popup>
          </Marker>
        );
      })}
    </MapContainer>
  );
};

export default RouteMap;