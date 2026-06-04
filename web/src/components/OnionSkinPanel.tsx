import { useAppStore } from '../store';

export default function OnionSkinPanel() {
  const { onionSkin, setOnionSkin, openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Onion Skin'] || { x: 100, y: 100 });
  
  if (!openPanels['onion']) return null;

  return (
    <div className="floating-panel expanded" style={{ left: pos.x + 60, top: pos.y, width: 240 }}>
      <div className="panel-header">
        <span>Onion Skinning</span>
        <button className="panel-close" onClick={() => closePanel('onion')}>×</button>
      </div>
      <div className="panel-body">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
          <span style={{ fontSize: 12 }}>Enabled</span>
          <div className={`toggle-switch ${onionSkin.enabled ? 'on' : ''}`} onClick={() => setOnionSkin({ enabled: !onionSkin.enabled })} />
        </div>
        {onionSkin.enabled && (
          <>
            <div className="settings-label">Previous Frames: {onionSkin.framesBefore}</div>
            <input type="range" min={0} max={10} value={onionSkin.framesBefore}
                   onChange={e => setOnionSkin({ framesBefore: Number(e.target.value) })}
                   style={{ width: '100%', marginBottom: 10 }} />
            <div className="settings-label">Next Frames: {onionSkin.framesAfter}</div>
            <input type="range" min={0} max={10} value={onionSkin.framesAfter}
                   onChange={e => setOnionSkin({ framesAfter: Number(e.target.value) })}
                   style={{ width: '100%', marginBottom: 10 }} />
            <div className="settings-label">Opacity Before: {onionSkin.opacityBefore}%</div>
            <input type="range" min={5} max={80} value={onionSkin.opacityBefore}
                   onChange={e => setOnionSkin({ opacityBefore: Number(e.target.value) })}
                   style={{ width: '100%', marginBottom: 10 }} />
            <div className="settings-label">Opacity After: {onionSkin.opacityAfter}%</div>
            <input type="range" min={5} max={80} value={onionSkin.opacityAfter}
                   onChange={e => setOnionSkin({ opacityAfter: Number(e.target.value) })}
                   style={{ width: '100%' }} />
          </>
        )}
      </div>
    </div>
  );
}
