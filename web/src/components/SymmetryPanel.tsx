import { useAppStore } from '../store';

export default function SymmetryPanel() {
  const { symmetryMode, setSymmetryMode, openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Symmetry'] || { x: 100, y: 100 });
  
  if (!openPanels['symmetry']) return null;

  const modes = [
    { id: 'none', label: 'None' },
    { id: 'h', label: 'Horizontal' },
    { id: 'v', label: 'Vertical' },
    { id: 'quad', label: 'Quadrant' },
    { id: 'rad4', label: 'Radial 4' },
    { id: 'rad6', label: 'Radial 6' },
    { id: 'rad8', label: 'Radial 8' },
    { id: 'mandala', label: 'Mandala' },
  ];

  return (
    <div className="floating-panel expanded" style={{ left: pos.x + 60, top: pos.y, width: 220 }}>
      <div className="panel-header">
        <span>Symmetry</span>
        <button className="panel-close" onClick={() => closePanel('symmetry')}>×</button>
      </div>
      <div className="panel-body">
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
          {modes.map(m => (
            <button
              key={m.id}
              className={`kb-btn ${symmetryMode === m.id ? 'toggled' : ''}`}
              onClick={() => setSymmetryMode(m.id)}
              style={{ textAlign: 'center' }}
            >
              {m.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
