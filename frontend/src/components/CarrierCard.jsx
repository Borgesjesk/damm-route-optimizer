const STATUS_CONFIG = {
  TRUSTED: { color: '#1db954', glow: '#1db95440' },
  WARNING: { color: '#f5a623', glow: '#f5a62340' },
  BLOCKED: { color: '#e2001a', glow: '#e2001a20' },
};

const CarrierCard = ({ carrier, selected, onSelect }) => {
  const blocked = carrier.status === 'BLOCKED';
  const cfg = STATUS_CONFIG[carrier.status] ?? { color: '#555', glow: '#55555520' };
  const score = Math.max(0, Math.min(100, carrier.trustScore ?? 0));

  return (
    <div
      role={blocked ? undefined : 'button'}
      tabIndex={blocked ? -1 : 0}
      onClick={() => !blocked && onSelect(carrier)}
      onKeyDown={(e) => e.key === 'Enter' && !blocked && onSelect(carrier)}
      style={{
        background: blocked ? '#0d0d0d' : selected ? '#1c1c1c' : '#141414',
        border: `1px solid ${selected ? '#e2001a66' : blocked ? '#1a1a1a' : '#1e1e1e'}`,
        borderRadius: '8px',
        padding: '11px 12px',
        cursor: blocked ? 'not-allowed' : 'pointer',
        opacity: blocked ? 0.5 : 1,
        marginBottom: '7px',
        position: 'relative',
        transition: 'border-color 0.15s ease, background 0.15s ease',
        outline: 'none',
        boxShadow: selected ? '0 0 0 1px #e2001a22' : 'none',
      }}
    >
      {selected && (
        <div
          style={{
            position: 'absolute',
            top: 0,
            right: '12px',
            background: '#e2001a',
            color: '#fff',
            fontSize: '7px',
            padding: '2px 8px',
            borderRadius: '0 0 5px 5px',
            fontWeight: '800',
            letterSpacing: '1.5px',
          }}
        >
          SELECTED
        </div>
      )}

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          marginBottom: '9px',
        }}
      >
        <div>
          <div
            style={{
              fontFamily: "'Syne', sans-serif",
              fontWeight: '700',
              fontSize: '13px',
              color: blocked ? '#3a3a3a' : '#fff',
              marginBottom: '2px',
            }}
          >
            {carrier.name}
          </div>
          <div
            style={{
              fontSize: '9px',
              color: '#3a3a3a',
              letterSpacing: '0.5px',
              fontFamily: 'monospace',
            }}
          >
            {carrier.vehicleId ?? '—'} · {carrier.vehicleType ?? '—'}
          </div>
        </div>

        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '5px',
            fontSize: '8px',
            color: cfg.color,
            fontWeight: '800',
            letterSpacing: '1.5px',
            background: cfg.glow,
            border: `1px solid ${cfg.color}30`,
            borderRadius: '4px',
            padding: '3px 8px',
            flexShrink: 0,
            marginTop: '1px',
          }}
        >
          <div
            style={{
              width: '5px',
              height: '5px',
              borderRadius: '50%',
              background: cfg.color,
              boxShadow: blocked ? 'none' : `0 0 6px ${cfg.color}`,
            }}
          />
          {carrier.status}
        </div>
      </div>

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          marginBottom: '5px',
        }}
      >
        <span
          style={{
            fontSize: '8px',
            color: '#333',
            letterSpacing: '1.5px',
            textTransform: 'uppercase',
          }}
        >
          Trust Score
        </span>
        <span
          style={{
            fontSize: '8px',
            color: cfg.color,
            fontWeight: '700',
            fontFamily: 'monospace',
          }}
        >
          {score} / 100
        </span>
      </div>

      <div
        style={{
          height: '3px',
          background: '#1a1a1a',
          borderRadius: '2px',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            height: '100%',
            width: `${score}%`,
            background: cfg.color,
            borderRadius: '2px',
            transition: 'width 0.7s cubic-bezier(0.4, 0, 0.2, 1)',
            boxShadow: blocked ? 'none' : `0 0 8px ${cfg.color}70`,
          }}
        />
      </div>

      {blocked && (
        <div
          style={{
            marginTop: '10px',
            background: '#e2001a0a',
            border: '1px solid #e2001a25',
            borderRadius: '4px',
            padding: '6px 9px',
            display: 'flex',
            alignItems: 'center',
            gap: '7px',
            fontSize: '9px',
            color: '#e2001a',
            fontWeight: '700',
            letterSpacing: '0.5px',
          }}
        >
          <span style={{ fontSize: '11px' }}>⚠</span>
          FraudSentinel · ACCESS DENIED
        </div>
      )}
    </div>
  );
};

export default CarrierCard;
