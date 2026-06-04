import { useState } from 'react';
import { useAppStore } from '../store';

const BRUSH_TYPES = [
  { id: 'round', name: 'Round', icon: '●' },
  { id: 'flat', name: 'Flat', icon: '▬' },
  { id: 'pencil', name: 'Pencil', icon: '✏️' },
  { id: 'airbrush', name: 'Airbrush', icon: '💨' },
  { id: 'marker', name: 'Marker', icon: '🖍️' },
  { id: 'calligraphy', name: 'Calligraphy', icon: '✒️' },
  { id: 'charcoal', name: 'Charcoal', icon: '🖤' },
  { id: 'watercolor', name: 'Watercolor', icon: '🎨' },
  { id: 'oil', name: 'Oil Paint', icon: '🖌️' },
  { id: 'spray', name: 'Spray', icon: '✨' },
  { id: 'smudge', name: 'Smudge', icon: '👆' },
  { id: 'blur', name: 'Blur', icon: '🌫️' },
  { id: 'sharpen', name: 'Sharpen', icon: '🔪' },
];

export default function BrushPanel() {
  const { brushSize, brushOpacity, brushColor, brushType, setBrushSize, setBrushOpacity, setBrushColor, setBrushType, openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Brush'] || { x: 100, y: 100 });
  const [expanded, setExpanded] = useState(false);
  
  if (!openPanels['brush']) return null;

  return (
    <div className={`floating-panel ${expanded ? 'expanded' : 'minimized'}`}
         style={{ left: pos.x + 60, top: pos.y, width: 240 }}>
      <div className="panel-header" onClick={() => setExpanded(!expanded)}>
        <span>Brush Settings</span>
        <button className="panel-close" onClick={(e) => { e.stopPropagation(); closePanel('brush'); }}>×</button>
      </div>
      {expanded && (
        <div className="panel-body" style={{ padding: 12 }}>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, marginBottom: 12 }}>
            {BRUSH_TYPES.map(b => (
              <div key={b.id}
                   className={`kb-btn ${brushType === b.id ? 'toggled' : ''}`}
                   onClick={() => setBrushType(b.id)}
                   title={b.name}>
                {b.icon}
              </div>
            ))}
          </div>
          <div className="settings-label">Size</div>
          <input type="range" min={1} max={100} value={brushSize}
                 onChange={e => setBrushSize(Number(e.target.value))}
                 style={{ width: '100%', marginBottom: 12 }} />
          <div className="settings-label">Opacity</div>
          <input type="range" min={0} max={100} value={brushOpacity}
                 onChange={e => setBrushOpacity(Number(e.target.value))}
                 style={{ width: '100%', marginBottom: 12 }} />
          <div className="settings-label">Color</div>
          <input type="color" value={brushColor}
                 onChange={e => setBrushColor(e.target.value)}
                 style={{ width: 40, height: 30, border: 'none', borderRadius: 4, cursor: 'pointer' }} />
        </div>
      )}
    </div>
  );
}
