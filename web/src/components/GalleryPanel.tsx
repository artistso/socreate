import { useAppStore, type Project } from '../store';

export default function GalleryPanel() {
  const { projects, activeProjectId, loadProject, newProject, openPanels, closePanel } = useAppStore();
  const pos = useAppStore(state => state.floatingPositions['Gallery'] || { x: 100, y: 100 });
  
  if (!openPanels['gallery']) return null;

  return (
    <div className="floating-panel expanded"
         style={{ left: pos.x + 60, top: pos.y, width: 280, maxHeight: 360 }}>
      <div className="panel-header">
        <span>Gallery</span>
        <button className="panel-close" onClick={() => closePanel('gallery')}>×</button>
      </div>
      <div className="panel-body">
        <button className="new-canvas-btn" onClick={newProject}>
          + New Canvas
        </button>
        {projects.length === 0 ? (
          <div style={{ padding: 16, textAlign: 'center', color: 'var(--ui-text-muted)', fontSize: 11 }}>
            No saved projects yet
          </div>
        ) : (
          <div className="gallery-grid">
            {projects.map((project: Project) => (
              <div
                key={project.id}
                className="gallery-item"
                onClick={() => loadProject(project.id)}
                style={{ borderColor: activeProjectId === project.id ? 'var(--accent)' : 'transparent' }}
              >
                <div className="gallery-thumb" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#2a2a2a' }}>
                  <span style={{ fontSize: 20 }}>🎨</span>
                </div>
                <div className="gallery-meta">
                  <div className="name">{project.name}</div>
                  <div className="info">{project.layers.length}L · {project.totalFrames}F · {project.fps}fps</div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
