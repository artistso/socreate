import { useAppStore } from '../store';

export default function ColorEffectsPanel() {
  const { openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Color Effects'] || { x: 100, y: 100 });
  
  if (!openPanels['coloreffects']) return null;

  return (
    <div className="floating-panel expanded" style={{ left: pos.x + 60, top: pos.y, width: 240 }}>
      <div className="panel-header">
        <span>Color Effects</span>
        <button className="panel-close" onClick={() => closePanel('coloreffects')}>×</button>
      </div>
      <div className="panel-body">
        <div className="settings-label">Hue</div>
        <input type="range" min={0} max={360} defaultValue={0} style={{ width: '100%', marginBottom: 8 }} />
        <div className="settings-label">Saturation</div>
        <input type="range" min={0} max={200} defaultValue={100} style={{ width: '100%', marginBottom: 8 }} />
        <div className="settings-label">Brightness</div>
        <input type="range" min={0} max={200} defaultValue={100} style={{ width: '100%', marginBottom: 8 }} />
        <div className="settings-label">Contrast</div>
        <input type="range" min={0} max={200} defaultValue={100} style={{ width: '100%', marginBottom: 8 }} />
        <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
          <button className="kb-btn" style={{ flex: 1 }}>🎨 Presets</button>
          <button className="kb-btn" style={{ flex: 1 }}>↩ Reset</button>
        </div>
      </div>
    </div>
  );
}
