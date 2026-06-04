import { useAppStore, type Layer } from '../store';

export default function LayersPanel() {
  const { layers, activeLayerId, setActiveLayer, addLayer, removeLayer, toggleLayerVisibility, setLayerOpacity, reorderLayers, openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Layers'] || { x: 100, y: 100 });
  
  if (!openPanels['layers']) return null;

  const handleDragStart = (e: React.DragEvent, index: number) => {
    e.dataTransfer.setData('text/plain', index.toString());
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = (e: React.DragEvent, dropIndex: number) => {
    e.preventDefault();
    const dragIndex = parseInt(e.dataTransfer.getData('text/plain'), 10);
    if (dragIndex !== dropIndex) {
      reorderLayers(dragIndex, dropIndex);
    }
  };

  return (
    <div className="floating-panel expanded"
         style={{ left: pos.x + 60, top: pos.y, width: 260, maxHeight: 400 }}>
      <div className="panel-header">
        <span>Layers ({layers.length})</span>
        <button className="panel-close" onClick={() => closePanel('layers')}>×</button>
      </div>
      <div className="panel-body" style={{ padding: 8 }}>
        {layers.map((layer: Layer, index: number) => (
          <div
            key={layer.id}
            className={`layer-item ${activeLayerId === layer.id ? 'selected' : ''}`}
            onClick={() => setActiveLayer(layer.id)}
            draggable
            onDragStart={(e) => handleDragStart(e, index)}
            onDragOver={handleDragOver}
            onDrop={(e) => handleDrop(e, index)}
          >
            <div className="layer-color-dot" style={{ background: layer.color }} />
            <div className="layer-info" style={{ flex: 1 }}>
              <div className="layer-name">{layer.name}</div>
              <div className="layer-opacity">{layer.opacity}% · {layer.blendMode}</div>
            </div>
            <button
              className={`layer-btn ${layer.visible ? 'vis' : ''}`}
              onClick={(e) => { e.stopPropagation(); toggleLayerVisibility(layer.id); }}
            >
              {layer.visible ? '👁️' : '🚫'}
            </button>
            <button
              className="layer-btn"
              onClick={(e) => { e.stopPropagation(); removeLayer(layer.id); }}
            >
              🗑️
            </button>
          </div>
        ))}
        <div style={{ marginTop: 8 }}>
          <input
            type="range"
            min={0}
            max={100}
            value={layers.find(l => l.id === activeLayerId)?.opacity || 100}
            onChange={(e) => {
              if (activeLayerId) setLayerOpacity(activeLayerId, Number(e.target.value));
            }}
            style={{ width: '100%' }}
          />
          <div className="settings-label">Layer Opacity</div>
        </div>
        <button
          className="add-btn"
          onClick={addLayer}
          style={{ marginTop: 8 }}
        >
          + Add Layer
        </button>
      </div>
    </div>
  );
}
