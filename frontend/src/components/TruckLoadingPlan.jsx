import { useState } from 'react';

const TABS = [
  { id: 'order', label: 'Loading Order' },
  { id: 'truck', label: 'Truck View' },
  { id: 'warehouse', label: 'Warehouse' },
];

function EmptyState({ text }) {
  return (
    <div
      style={{
        color: '#2a2a2a',
        fontSize: '11px',
        textAlign: 'center',
        padding: '24px 16px',
        fontStyle: 'italic',
      }}
    >
      {text}
    </div>
  );
}

function TruckZone({ label, sublabel, items, color }) {
  return (
    <div
      style={{
        flex: 1,
        padding: '8px 6px',
        borderRight: label !== 'BACK' ? '1px solid #1a1a1a' : 'none',
        minWidth: 0,
      }}
    >
      <div
        style={{
          fontSize: '8px',
          color: color,
          textAlign: 'center',
          letterSpacing: '1px',
          fontWeight: '800',
          marginBottom: '2px',
        }}
      >
        {label}
      </div>
      <div
        style={{
          fontSize: '7px',
          color: '#2a2a2a',
          textAlign: 'center',
          marginBottom: '7px',
          letterSpacing: '0.5px',
        }}
      >
        {sublabel}
      </div>
      {items.length === 0 ? (
        <div style={{ color: '#222', fontSize: '9px', textAlign: 'center' }}>—</div>
      ) : (
        items.map((item, i) => (
          <div
            key={i}
            style={{
              background: `${color}12`,
              border: `1px solid ${color}28`,
              borderRadius: '3px',
              padding: '3px 4px',
              fontSize: '8px',
              color: color,
              marginBottom: '3px',
              textAlign: 'center',
              lineHeight: 1.3,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
            title={item.clientName}
          >
            #{item.order ?? i + 1} {(item.clientName ?? '').split(' ')[0]}
          </div>
        ))
      )}
    </div>
  );
}

const TruckLoadingPlan = ({ loadingPlan, warehouseSheet }) => {
  const [active, setActive] = useState('order');
  const items = loadingPlan?.items ?? [];
  const instructions = warehouseSheet?.instructions ?? [];

  const third = Math.ceil(items.length / 3) || 1;
  const zones = {
    front: items.slice(third * 2),
    middle: items.slice(third, third * 2),
    back: items.slice(0, third),
  };

  return (
    <div style={{ marginTop: '18px' }}>
      <div
        style={{
          fontSize: '8px',
          color: '#333',
          letterSpacing: '2.5px',
          textTransform: 'uppercase',
          marginBottom: '10px',
          paddingBottom: '6px',
          borderBottom: '1px solid #161616',
        }}
      >
        Truck Loading Plan
      </div>

      {/* Tab bar */}
      <div
        style={{
          display: 'flex',
          gap: '3px',
          marginBottom: '12px',
          background: '#0d0d0d',
          padding: '3px',
          borderRadius: '6px',
          border: '1px solid #1a1a1a',
        }}
      >
        {TABS.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActive(tab.id)}
            style={{
              flex: 1,
              padding: '6px 4px',
              background: active === tab.id ? '#1c1c1c' : 'transparent',
              border: active === tab.id ? '1px solid #2a2a2a' : '1px solid transparent',
              borderRadius: '4px',
              color: active === tab.id ? '#fff' : '#3a3a3a',
              fontSize: '8px',
              cursor: 'pointer',
              letterSpacing: '0.8px',
              fontFamily: 'monospace',
              fontWeight: active === tab.id ? '700' : '400',
              transition: 'all 0.15s ease',
              textTransform: 'uppercase',
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Loading Order */}
      {active === 'order' && (
        <div>
          {items.length === 0 ? (
            <EmptyState text="Generate a route to see loading order" />
          ) : (
            items.map((item, i) => (
              <div
                key={i}
                style={{
                  display: 'flex',
                  gap: '10px',
                  alignItems: 'flex-start',
                  padding: '8px 10px',
                  background: '#111',
                  border: '1px solid #1a1a1a',
                  borderRadius: '6px',
                  marginBottom: '5px',
                }}
              >
                <div
                  style={{
                    width: '22px',
                    height: '22px',
                    borderRadius: '50%',
                    background: '#e2001a',
                    color: '#fff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '10px',
                    fontWeight: '800',
                    flexShrink: 0,
                    boxShadow: '0 0 8px #e2001a50',
                  }}
                >
                  {item.order ?? i + 1}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div
                    style={{
                      fontSize: '11px',
                      fontWeight: '600',
                      color: '#ccc',
                      marginBottom: '4px',
                      fontFamily: "'Syne', sans-serif",
                    }}
                  >
                    {item.clientName}
                  </div>
                  {(item.products ?? []).map((p, j) => (
                    <div
                      key={j}
                      style={{
                        fontSize: '9px',
                        color: '#444',
                        display: 'flex',
                        justifyContent: 'space-between',
                        marginBottom: '1px',
                      }}
                    >
                      <span>{p.name}</span>
                      <span style={{ color: '#f5c842', fontWeight: '600' }}>
                        {p.qty} u · {p.pallets} pal.
                      </span>
                    </div>
                  ))}
                  {!(item.products ?? []).length && item.units && (
                    <div style={{ fontSize: '9px', color: '#444' }}>
                      {item.units} units
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Truck View */}
      {active === 'truck' && (
        <div>
          <div
            style={{
              background: '#0d0d0d',
              border: '1px solid #1e1e1e',
              borderRadius: '8px',
              overflow: 'hidden',
            }}
          >
            {/* Cabin */}
            <div
              style={{
                background: '#151515',
                padding: '5px 10px',
                fontSize: '8px',
                color: '#2a2a2a',
                letterSpacing: '3px',
                textAlign: 'center',
                borderBottom: '1px solid #1a1a1a',
                textTransform: 'uppercase',
              }}
            >
              ▶ CABIN
            </div>

            {/* Zone labels row */}
            <div
              style={{
                display: 'flex',
                borderBottom: '1px solid #1a1a1a',
                minHeight: '90px',
              }}
            >
              <TruckZone
                label="FRONT"
                sublabel="last in"
                items={zones.front}
                color="#e2001a"
              />
              <TruckZone
                label="MIDDLE"
                sublabel="mid load"
                items={zones.middle}
                color="#f5c842"
              />
              <TruckZone
                label="BACK"
                sublabel="first out"
                items={zones.back}
                color="#1db954"
              />
            </div>

            {/* Door */}
            <div
              style={{
                background: '#0f0f0f',
                padding: '4px 12px',
                fontSize: '8px',
                color: '#2a2a2a',
                textAlign: 'right',
                letterSpacing: '2px',
              }}
            >
              LOADING DOOR ◀
            </div>
          </div>

          <div
            style={{
              fontSize: '8px',
              color: '#2a2a2a',
              textAlign: 'center',
              marginTop: '8px',
              letterSpacing: '1px',
            }}
          >
            Load FRONT first · Deliver from BACK
          </div>
        </div>
      )}

      {/* Warehouse */}
      {active === 'warehouse' && (
        <div>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '10px',
            }}
          >
            <div style={{ fontSize: '9px', color: '#333' }}>Printable instructions</div>
            <button
              onClick={() => window.print()}
              style={{
                background: 'transparent',
                border: '1px solid #2a2a2a',
                color: '#555',
                padding: '4px 10px',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '8px',
                letterSpacing: '1.5px',
                fontFamily: 'monospace',
                textTransform: 'uppercase',
                transition: 'border-color 0.15s, color 0.15s',
              }}
            >
              ⎙ Print
            </button>
          </div>

          {instructions.length === 0 ? (
            <EmptyState text="No warehouse instructions available" />
          ) : (
            instructions.map((inst, i) => (
              <div
                key={i}
                style={{
                  padding: '8px 10px',
                  background: '#111',
                  border: '1px solid #1a1a1a',
                  borderLeft: '3px solid #f5c842',
                  borderRadius: '0 5px 5px 0',
                  marginBottom: '5px',
                  fontSize: '10px',
                  color: '#999',
                  lineHeight: 1.55,
                }}
              >
                <span
                  style={{
                    color: '#f5c84250',
                    marginRight: '8px',
                    fontWeight: '700',
                    fontSize: '9px',
                  }}
                >
                  {String(i + 1).padStart(2, '0')}.
                </span>
                {typeof inst === 'string' ? inst : inst.text ?? JSON.stringify(inst)}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default TruckLoadingPlan;
