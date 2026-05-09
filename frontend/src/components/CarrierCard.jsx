export function CarrierCard({ carrier, selected, onSelect }) {
  const colors = {
    TRUSTED: '#1db954',
    WARNING: '#f5c842',
    BLOCKED: '#e63329',
  };
  const color = colors[carrier.trustLevel] || '#888';

  return (
    <div
      onClick={() => carrier.trustLevel !== 'BLOCKED' && onSelect(carrier)}
      style={{
        background: selected ? '#2a2a1a' : '#1a1a1a',
        border: `1px solid ${selected ? '#f5c842' : carrier.trustLevel === 'BLOCKED' ? '#3a1a1a' : '#2a2a2a'}`,
        padding: '16px',
        cursor: carrier.trustLevel === 'BLOCKED' ? 'not-allowed' : 'pointer',
        opacity: carrier.trustLevel === 'BLOCKED' ? 0.6 : 1,
        transition: 'all 0.2s',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: 13, fontWeight: 600, color: '#ddd' }}>{carrier.name}</span>
        <span style={{
          fontSize: 10,
          padding: '2px 8px',
          background: color + '22',
          color,
          letterSpacing: 1,
        }}>
          {carrier.trustLevel}
        </span>
      </div>
      <div style={{ marginTop: 8 }}>
        <div style={{
          height: 4,
          background: '#0a0a0a',
          borderRadius: 2,
          overflow: 'hidden',
        }}>
          <div style={{
            height: '100%',
            width: `${carrier.trustScore}%`,
            background: color,
            transition: 'width 0.5s ease',
          }} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
          <span style={{ fontSize: 10, color: '#555' }}>
            {carrier.documentVerified ? '✓ Docs verified' : '✗ Docs unverified'}
          </span>
          <span style={{ fontSize: 10, color }}>{carrier.trustScore}/100</span>
        </div>
      </div>
      {carrier.trustLevel === 'BLOCKED' && (
        <div style={{
          marginTop: 8,
          fontSize: 10,
          color: '#e63329',
          padding: '4px 8px',
          background: '#1a0000',
        }}>
          🛡️ BLOCKED by FraudSentinel
        </div>
      )}
    </div>
  );
}
