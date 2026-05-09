export function StatCard({ label, value, unit, color = '#f5c842', icon }) {
  return (
    <div style={{
      background: '#1a1a1a',
      borderLeft: `3px solid ${color}`,
      padding: '20px 24px',
      display: 'flex',
      flexDirection: 'column',
      gap: 4,
    }}>
      <span style={{ fontSize: 11, color: '#666', letterSpacing: 2, textTransform: 'uppercase' }}>
        {icon} {label}
      </span>
      <span style={{
        fontFamily: 'Courier New',
        fontSize: 32,
        fontWeight: 700,
        color,
        lineHeight: 1,
      }}>
        {value}
        {unit && <span style={{ fontSize: 14, color: '#888', marginLeft: 6 }}>{unit}</span>}
      </span>
    </div>
  );
}
