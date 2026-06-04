import { useAppStore } from '../store';

export default function SpriteSheetPanel() {
  const { openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Sprite Sheet'] || { x: 100, y: 100 });
  
  if (!openPanels['spritesheet']) return null;

  return (
    <div className="floating-panel expanded" style={{ left: pos.x + 60, top: pos.y, width: 260 }}>
      <div className="panel-header">
        <span>Sprite Sheet Import</span>
        <button className="panel-close" onClick={() => closePanel('spritesheet')}>×</button>
      </div>
      <div className="panel-body">
        <div className="settings-label">Import Sprite Sheet</div>
        <label className="kb-btn" style={{ display: 'block', textAlign: 'center', width: '100%', padding: 10, marginBottom: 10 }}>
          📁 Choose Image File
          <input type="file" accept="image/*" style={{ display: 'none' }} />
        </label>
        <div className="settings-label">Rows</div>
        <input type="number" min={1} defaultValue={4} style={{ width: 60, background: 'var(--ui-bg-solid)', border: '1px solid var(--ui-border)', borderRadius: 4, color: 'white', padding: '4px 6px', fontSize: 12, marginBottom: 8 }} />
        <div className="settings-label">Columns</div>
        <input type="number" min={1} defaultValue={4} style={{ width: 60, background: 'var(--ui-bg-solid)', border: '1px solid var(--ui-border)', borderRadius: 4, color: 'white', padding: '4px 6px', fontSize: 12, marginBottom: 8 }} />
        <div className="settings-label">Playback Speed (FPS)</div>
        <input type="range" min={1} max={60} defaultValue={12} style={{ width: '100%', marginBottom: 8 }} />
        <div className="settings-label">Loop Mode</div>
        <select style={{ width: '100%', background: 'var(--ui-bg-solid)', border: '1px solid var(--ui-border)', borderRadius: 4, color: 'white', padding: '4px 6px', fontSize: 12, marginBottom: 10 }}>
          <option>Repeat</option>
          <option>Ping Pong</option>
          <option>Reverse</option>
          <option>Once</option>
        </select>
        <div style={{ display: 'flex', gap: 6 }}>
          <button className="kb-btn" style={{ flex: 1 }}>▶ Play</button>
          <button className="kb-btn" style={{ flex: 1 }}>⏸ Pause</button>
          <button className="kb-btn" style={{ flex: 1 }}>⏹ Stop</button>
        </div>
      </div>
    </div>
  );
}
