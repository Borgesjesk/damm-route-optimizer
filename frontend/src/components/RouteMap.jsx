import { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';

// Fix Leaflet default icon issue with webpack
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl:       'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl:     'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Custom marker icons
const makeIcon = (color, label) => L.divIcon({
  className: '',
  html: `
    <div style="
      background:${color};
      color:white;
      width:28px;height:28px;
      border-radius:50% 50% 50% 0;
      transform:rotate(-45deg);
      border:2px solid white;
      display:flex;align-items:center;justify-content:center;
      font-size:10px;font-weight:700;
    ">
      <span style="transform:rotate(45deg)">${label}</span>
    </div>`,
  iconSize: [28, 28],
  iconAnchor: [14, 28],
});

const warehouseIcon = L.divIcon({
  className: '',
  html: `<div style="font-size:28px;filter:drop-shadow(0 2px 4px rgba(0,0,0,0.5))">🏭</div>`,
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});

// DAMM warehouse
const WAREHOUSE = [41.3167, 2.0833];

function FitBounds({ positions }) {
  const map = useMap();
  useEffect(() => {
    if (positions && positions.length > 1) {
      map.fitBounds(positions, { padding: [40, 40] });
    }
  }, [positions, map]);
  return null;
}

export function RouteMap({ clients, route }) {
  const stopColors = { PENDING: '#f5c842', DELIVERED: '#1db954', FAILED: '#e63329' };

  // Build polyline: warehouse → stops in order → warehouse
  const routePositions = route
    ? [
        WAREHOUSE,
        ...route.stops
          .sort((a, b) => a.stopOrder - b.stopOrder)
          .map(s => [s.latitude, s.longitude]),
        WAREHOUSE,
      ]
    : [];

  const allPositions = clients.length > 0
    ? [[...clients.map(c => [c.latitude, c.longitude]), WAREHOUSE]]
    : null;

  return (
    <MapContainer
      center={[41.3851, 2.1734]}
      zoom={12}
      style={{ height: '100%', width: '100%', minHeight: 500 }}
    >
      <TileLayer
        url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        attribution='© OpenStreetMap © CARTO'
      />

      {allPositions && <FitBounds positions={allPositions[0]} />}

      {/* Warehouse marker */}
      <Marker position={WAREHOUSE} icon={warehouseIcon}>
        <Popup>
          <strong>Damm Warehouse</strong><br/>
          El Prat de Llobregat<br/>
          <span style={{ color: '#e2001a' }}>🍺 Route origin</span>
        </Popup>
      </Marker>

      {/* Client markers — show route order if route exists */}
      {clients.map((client, idx) => {
        const stop = route?.stops?.find(s => s.clientName === client.name);
        const color = stop ? stopColors[stop.status] : '#f5c842';
        const label = stop ? stop.stopOrder : idx + 1;

        return (
          <Marker
            key={client.id}
            position={[client.latitude, client.longitude]}
            icon={makeIcon(color, label)}
          >
            <Popup>
              <div style={{ minWidth: 200 }}>
                <strong>{client.name}</strong><br/>
                <span style={{ fontSize: 12, color: '#666' }}>{client.address}</span><br/><br/>
                <span>⏰ {client.deliveryWindowStart} – {client.deliveryWindowEnd}</span><br/>
                <span>🅿️ {client.parkingDifficulty} | {client.nearestLoadingBay || 'Check signage'}</span>
                {stop && (
                  <>
                    <br/><span>🕐 ETA: {stop.estimatedArrival}</span>
                    <br/><span style={{
                      color,
                      fontWeight: 700,
                    }}>● {stop.status}</span>
                  </>
                )}
              </div>
            </Popup>
          </Marker>
        );
      })}

      {/* Optimised route polyline */}
      {routePositions.length > 1 && (
        <Polyline
          positions={routePositions}
          color="#e2001a"
          weight={3}
          opacity={0.8}
          dashArray="8,4"
        />
      )}
    </MapContainer>
  );
}
