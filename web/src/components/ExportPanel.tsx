import { useAppStore } from '../store';

export default function ExportPanel() {
  const { openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Export'] || { x: 100, y: 100 });
  
  if (!openPanels['export']) return null;

  return (
    <div className="floating-panel expanded" style={{ left: pos.x + 60, top: pos.y, width: 240 }}>
      <div className="panel-header">
        <span>Export</span>
        <button className="panel-close" onClick={() => closePanel('export')}>×</button>
      </div>
      <div className="panel-body">
        <div className="settings-label">Image Export</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6, marginBottom: 16 }}>
          <button className="kb-btn">📄 PNG</button>
          <button className="kb-btn">📄 JPEG</button>
          <button className="kb-btn">📄 PSD</button>
          <button className="kb-btn">📄 SVG</button>
        </div>
        <div className="settings-label">Animation Export</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6, marginBottom: 16 }}>
          <button className="kb-btn">🎬 MP4</button>
          <button className="kb-btn">🎞️ GIF</button>
          <button className="kb-btn">🎞️ APNG</button>
          <button className="kb-btn">🎞️ WebP</button>
        </div>
        <div className="settings-label">Sprite Sheet Export</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: 6 }}>
          <button className="kb-btn">📊 Export Sprite Sheet</button>
          <button className="kb-btn">📊 Export Frame Data</button>
        </div>
      </div>
    </div>
  );
}
