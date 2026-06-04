import { useAppStore } from '../store';

export default function SettingsPanel() {
  const { settings, updateSettings, openPanels, closePanel, saveCurrentProject, newProject } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Settings'] || { x: 100, y: 100 });
  
  if (!openPanels['settings']) return null;

  const toggle = (key: string) => {
    updateSettings({ [key]: !settings[key as keyof typeof settings] });
  };

  return (
    <div className="floating-panel expanded" style={{ left: pos.x + 60, top: pos.y, width: 280 }}>
      <div className="panel-header">
        <span>Settings</span>
        <button className="panel-close" onClick={() => closePanel('settings')}>×</button>
      </div>
      <div className="panel-body">
        <div className="settings-group">
          <div className="settings-label">Input</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 12 }}>Palm Rejection</span>
              <div className={`toggle-switch ${settings.palmRejection ? 'on' : ''}`} onClick={() => toggle('palmRejection')} />
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 12 }}>Stylus Only Mode</span>
              <div className={`toggle-switch ${settings.stylusOnly ? 'on' : ''}`} onClick={() => toggle('stylusOnly')} />
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 12 }}>Low Latency</span>
              <div className={`toggle-switch ${settings.lowLatency ? 'on' : ''}`} onClick={() => toggle('lowLatency')} />
            </div>
          </div>
        </div>
        <div className="settings-group">
          <div className="settings-label">Display</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 12 }}>Interface Scale</span>
              <input type="range" min={0.5} max={2} step={0.1} value={settings.interfaceScale}
                     onChange={e => updateSettings({ interfaceScale: Number(e.target.value) })}
                     style={{ width: 80 }} />
              <span style={{ fontSize: 11, color: 'var(--ui-text-muted)' }}>{settings.interfaceScale.toFixed(1)}x</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 12 }}>Font Scale</span>
              <input type="range" min={0.5} max={2} step={0.1} value={settings.fontScale}
                     onChange={e => updateSettings({ fontScale: Number(e.target.value) })}
                     style={{ width: 80 }} />
              <span style={{ fontSize: 11, color: 'var(--ui-text-muted)' }}>{settings.fontScale.toFixed(1)}x</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 12 }}>Thumbnail Size</span>
              <select value={settings.thumbnailSize} onChange={e => updateSettings({ thumbnailSize: e.target.value as any })}
                      style={{ background: 'var(--ui-bg-solid)', color: 'var(--ui-text)', border: '1px solid var(--ui-border)', borderRadius: 4, padding: '2px 6px', fontSize: 11 }}>
                <option value="small">Small</option>
                <option value="medium">Medium</option>
                <option value="large">Large</option>
              </select>
            </div>
          </div>
        </div>
        <div className="settings-group">
          <div className="settings-label">Project</div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <button className="kb-btn" onClick={saveCurrentProject}>💾 Save Project</button>
            <button className="kb-btn" onClick={newProject}>📄 New Project</button>
          </div>
        </div>
        <div style={{ fontSize: 9, color: 'var(--ui-text-muted)', textAlign: 'center', marginTop: 8, lineHeight: 1.6 }}>
          Developed by Steven Michael Allen Owens<br />
          @SoQuarky · An AdventuresInDrawing Production<br />
          soquarky.click · artistso.com
        </div>
      </div>
    </div>
  );
}
