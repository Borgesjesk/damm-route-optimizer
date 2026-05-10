const StatCard = ({ label, value, unit = '', accent = '#f5c842', icon = '' }) => (
  <div
    style={{
      background: '#111',
      border: '1px solid #1e1e1e',
      borderTop: `2px solid ${accent}`,
      borderRadius: '6px',
      padding: '11px 14px',
      flex: 1,
      minWidth: 0,
      position: 'relative',
      overflow: 'hidden',
    }}
  >
    <div
      style={{
        position: 'absolute',
        inset: 0,
        background: `radial-gradient(ellipse at top left, ${accent}08 0%, transparent 60%)`,
        pointerEvents: 'none',
      }}
    />
    <div
      style={{
        fontSize: '8px',
        color: '#444',
        letterSpacing: '2px',
        textTransform: 'uppercase',
        marginBottom: '6px',
        fontFamily: 'monospace',
      }}
    >
      {label}
    </div>
    <div style={{ display: 'flex', alignItems: 'baseline', gap: '5px' }}>
      <span
        style={{
          fontSize: '26px',
          fontWeight: '800',
          fontFamily: "'Syne', sans-serif",
          color: '#fff',
          lineHeight: 1,
          textShadow: `0 0 20px ${accent}40`,
        }}
      >
        {value ?? '—'}
      </span>
      {unit && (
        <span
          style={{
            fontSize: '11px',
            color: accent,
            fontWeight: '700',
            fontFamily: 'monospace',
          }}
        >
          {unit}
        </span>
      )}
    </div>
    {icon && (
      <div
        style={{
          position: 'absolute',
          right: '10px',
          bottom: '6px',
          fontSize: '20px',
          opacity: 0.07,
          userSelect: 'none',
        }}
      >
        {icon}
      </div>
    )}
  </div>
);

export default StatCard;
