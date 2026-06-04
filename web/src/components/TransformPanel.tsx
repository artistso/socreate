import { useAppStore } from '../store';

export default function TransformPanel() {
  const { openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Transform'] || { x: 100, y: 100 });
  
  if (!openPanels['transform']) return null;

  return (
    <div className="floating-panel expanded" style={{ left: pos.x + 60, top: pos.y, width: 240 }}>
      <div className="panel-header">
        <span>Transform</span>
        <button className="panel-close" onClick={() => closePanel('transform')}>×</button>
      </div>
      <div className="panel-body">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <div className="settings-label">Scale</div>
          <input type="range" min={10} max={400} defaultValue={100} style={{ width: '100%' }} />
          <div className="settings-label">Rotation</div>
          <input type="range" min={0} max={360} defaultValue={0} style={{ width: '100%' }} />
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="kb-btn" style={{ flex: 1 }}>↔ Flip H</button>
            <button className="kb-btn" style={{ flex: 1 }}>↕ Flip V</button>
          </div>
          <button className="kb-btn" style={{ width: '100%' }}>↩ Reset Transform</button>
        </div>
      </div>
    </div>
  );
}
