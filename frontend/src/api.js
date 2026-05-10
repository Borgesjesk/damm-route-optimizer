const BASE = 'http://localhost:8080/api';

const H = {
  'Content-Type': 'application/json',
  'X-API-Key': 'dammroute-hackathon-2026',
};

const get = (path) => fetch(`${BASE}${path}`, { headers: H });

const post = (path, body = {}) =>
  fetch(`${BASE}${path}`, { method: 'POST', headers: H, body: JSON.stringify(body) });

export const checkHealth = () => get('/health');

export const fetchCarriers = () => get('/carriers').then((r) => r.json());

export const fetchClients = () => get('/clients').then((r) => r.json());

export const optimiseRoute = (carrierId, clientIds) =>
  post('/routes/optimise', { carrierId, clientIds });

export const fetchRoute = (id) => get(`/routes/${id}`).then((r) => r.json());

export const fetchLoadingPlan = (id) =>
  get(`/routes/${id}/loading-plan`).then((r) => r.json());

export const fetchWarehouseSheet = (id) =>
  get(`/routes/${id}/warehouse-sheet`).then((r) => r.json());

export const confirmDelivery = (routeId, stopId) =>
  post(`/routes/${routeId}/stops/${stopId}/confirm-delivery`);

export const reportDamage = (routeId, stopId, data) =>
  post(`/routes/${routeId}/stops/${stopId}/damage`, data);

export const reportIncident = (routeId, data) =>
  post(`/routes/${routeId}/incident`, data);

export const fetchDashboardStats = () =>
  get('/dashboard/stats').then((r) => r.json());
