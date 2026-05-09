// ── API CLIENT ─────────────────────────────────────────────────────────
// All calls go through here. Change API_KEY and BASE_URL in one place.

const BASE_URL = 'http://localhost:8080';
const API_KEY  = 'dammroute-hackathon-2026';

const headers = {
  'Content-Type': 'application/json',
  'X-API-Key': API_KEY,
};

// Generic fetch with error handling
async function apiFetch(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: { ...headers, ...options.headers },
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: 'Unknown error' }));
    throw new Error(err.error || `HTTP ${res.status}`);
  }
  return res.json();
}

export const api = {
  health:       ()           => apiFetch('/api/health'),
  getClients:   ()           => apiFetch('/api/clients'),
  getCarriers:  ()           => apiFetch('/api/carriers'),
  getDashboard: ()           => apiFetch('/api/dashboard/stats'),
  getRoute:     (id)         => apiFetch(`/api/routes/${id}`),
  optimiseRoute: (carrierId, clientIds) =>
    apiFetch('/api/routes/optimise', {
      method: 'POST',
      body: JSON.stringify({ carrierId, clientIds }),
    }),
  completeStop: (routeId, stopId) =>
    apiFetch(`/api/routes/${routeId}/stops/${stopId}/complete`, {
      method: 'PUT',
    }),
};
