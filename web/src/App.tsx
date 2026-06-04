import { useEffect, useCallback, useState, useRef } from 'react';
import { useAppStore } from './store';
import DrawingCanvas from './components/DrawingCanvas';
import FloatingButton from './components/FloatingButton';
import BrushPanel from './components/BrushPanel';
import LayersPanel from './components/LayersPanel';
import GalleryPanel from './components/GalleryPanel';
import SettingsPanel from './components/SettingsPanel';
import Timeline from './components/Timeline';
import OnionSkinPanel from './components/OnionSkinPanel';
import SymmetryPanel from './components/SymmetryPanel';
import TransformPanel from './components/TransformPanel';
import SpriteSheetPanel from './components/SpriteSheetPanel';
import ExportPanel from './components/ExportPanel';
import ColorEffectsPanel from './components/ColorEffectsPanel';

// SVG icons as components
const BrushIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 19l7-7 3 3-7 7-3-3z" />
    <path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z" />
    <path d="M2 2l7.586 7.586" />
    <circle cx="11" cy="11" r="2" />
  </svg>
);

const LayerIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polygon points="12 2 2 7 12 12 22 7 12 2" />
    <polyline points="2 17 12 22 22 17" />
    <polyline points="2 12 12 17 22 12" />
  </svg>
);

const GalleryIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="3" width="7" height="7" />
    <rect x="14" y="3" width="7" height="7" />
    <rect x="14" y="14" width="7" height="7" />
    <rect x="3" y="14" width="7" height="7" />
  </svg>
);

const TimelineIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="2" y="6" width="20" height="12" rx="2" />
    <line x1="6" y1="6" x2="6" y2="18" />
    <line x1="10" y1="6" x2="10" y2="18" />
    <line x1="14" y1="6" x2="14" y2="18" />
    <line x1="18" y1="6" x2="18" y2="18" />
  </svg>
);

const SettingsIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="3" />
    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
  </svg>
);

const EraserIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M20 20H7L3 16a1 1 0 0 1 0-1.41l9.59-9.59a2 2 0 0 1 2.82 0l5.17 5.17a2 2 0 0 1 0 2.82L14 20" />
    <line x1="18" y1="13" x2="11" y2="6" />
    <line x1="3" y1="20" x2="21" y2="20" />
  </svg>
);

const OnionIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
    <ellipse cx="12" cy="14" rx="6" ry="8" />
    <ellipse cx="12" cy="14" rx="4" ry="6" opacity="0.6" />
    <ellipse cx="12" cy="14" rx="2" ry="4" opacity="0.3" />
    <path d="M12 2 C10 6, 8 8, 8 10" />
    <path d="M12 2 C14 6, 16 8, 16 10" />
  </svg>
);

const SymmetryIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
    <line x1="12" y1="2" x2="12" y2="22" strokeDasharray="2 2" />
    <path d="M8 6 L4 12 L8 18" />
    <path d="M16 6 L20 12 L16 18" />
  </svg>
);

const TransformIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="15 3 21 3 21 9" />
    <polyline points="9 21 3 21 3 15" />
    <line x1="21" y1="3" x2="14" y2="10" />
    <line x1="3" y1="21" x2="10" y2="14" />
  </svg>
);

const SpriteIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="2" y="2" width="20" height="20" rx="2" />
    <line x1="12" y1="2" x2="12" y2="22" />
    <line x1="2" y1="12" x2="22" y2="12" />
    <circle cx="7" cy="7" r="1.5" fill="currentColor" />
  </svg>
);

const ExportIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
    <polyline points="7 10 12 15 17 10" />
    <line x1="12" y1="15" x2="12" y2="3" />
  </svg>
);

const ColorIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <path d="M12 2a10 10 0 0 0 0 20" fill="currentColor" opacity="0.2" />
    <circle cx="12" cy="8" r="2" fill="currentColor" />
    <circle cx="8" cy="14" r="2" fill="currentColor" />
    <circle cx="16" cy="14" r="2" fill="currentColor" />
  </svg>
);

const KeyframeIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M15.5 2H8.6c-.4 0-.8.2-1.1.5l-3.1 3.1c-.3.3-.5.7-.5 1.1v5.6c0 .4.2.8.5 1.1l3.1 3.1c.3.3.7.5 1.1.5h5.6c.4 0 .8-.2 1.1-.5l3.1-3.1c.3-.3.5-.7.5-1.1V6.7c0-.4-.2-.8-.5-1.1l-3.1-3.1c-.3-.3-.7-.5-1.1-.5z" transform="translate(0 3)" />
  </svg>
);

const PointerIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M3 3l7.07 16.97 2.51-7.39 7.39-2.51L3 3z" />
    <path d="M13 13l6 6" />
  </svg>
);

// Brush cursor follower component
function BrushCursorFollower() {
  const { brushSize, activeTool } = useAppStore();
  const [pos, setPos] = useState({ x: -100, y: -100 });
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const onMove = (e: PointerEvent) => {
      if (activeTool === 'brush' || activeTool === 'eraser') {
        setPos({ x: e.clientX, y: e.clientY });
        setVisible(true);
      } else {
        setVisible(false);
      }
    };
    const onLeave = () => setVisible(false);
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerleave', onLeave);
    return () => {
      window.removeEventListener('pointermove', onMove);
      window.removeEventListener('pointerleave', onLeave);
    };
  }, [activeTool]);

  if (!visible) return null;

  const displaySize = Math.max(4, brushSize * 0.5);

  return (
    <div
      className="brush-cursor"
      style={{
        left: pos.x - displaySize / 2,
        top: pos.y - displaySize / 2,
        width: displaySize,
        height: displaySize,
        borderColor: activeTool === 'eraser' ? 'rgba(255,100,100,0.5)' : 'rgba(0,0,0,0.5)',
      }}
    />
  );
}

export default function App() {
  const {
    activeTool, setActiveTool,
    togglePanel, openPanels, closePanel,
    activeLayerId, addKeyframe, currentFrame,
    brushColor,
    savePositions,
  } = useAppStore();

  const [showSplash, setShowSplash] = useState(true);
  const [showHints, setShowHints] = useState(false);

  // Close panels when tapping on the canvas background
  const handleCanvasClick = useCallback((e: React.MouseEvent) => {
    const target = e.target as HTMLElement;
    if (target.closest('.floating-panel, .floating-btn, .timeline-container')) return;
    Object.keys(openPanels).forEach((id) => {
      if (openPanels[id]) {
        closePanel(id);
      }
    });
  }, [openPanels, closePanel]);

  // Save positions on window close
  useEffect(() => {
    const onUnload = () => savePositions();
    window.addEventListener('beforeunload', onUnload);
    return () => window.removeEventListener('beforeunload', onUnload);
  }, [savePositions]);

  // Keyboard shortcuts
  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) return;
      if (e.key === 'b') setActiveTool('brush');
      if (e.key === 'e') setActiveTool('eraser');
      if (e.key === 'v') setActiveTool('pointer');
      if (e.key === ' ') {
        e.preventDefault();
        useAppStore.getState().togglePlayback();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [setActiveTool]);

  // Splash screen then hints
  useEffect(() => {
    const timer = setTimeout(() => {
      setShowSplash(false);
      setShowHints(true);
      setTimeout(() => setShowHints(false), 4000);
    }, 2200);
    return () => clearTimeout(timer);
  }, []);

  if (showSplash) {
    return (
      <div style={{
        position: 'fixed', inset: 0,
        background: 'linear-gradient(135deg, #1a1a2e, #16213e, #0f3460)',
        display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center',
        zIndex: 99999,
      }}>
        <div style={{
          width: 100, height: 100, borderRadius: 24,
          overflow: 'hidden', marginBottom: 20,
          boxShadow: '0 0 60px rgba(255,107,53,0.4)',
          animation: 'fadeIn 0.5s ease',
        }}>
          <svg viewBox="0 0 100 100" style={{ width: '100%', height: '100%' }}>
            <defs>
              <linearGradient id="splashGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#FF6B35" />
                <stop offset="50%" stopColor="#9B59B6" />
                <stop offset="100%" stopColor="#4ECDC4" />
              </linearGradient>
            </defs>
            <rect fill="url(#splashGrad)" width="100" height="100" rx="20" />
            <text x="50" y="75" textAnchor="middle" fill="white" fontSize="60" fontWeight="bold" fontFamily="Arial">C</text>
            <text x="50" y="35" textAnchor="middle" fill="white" fontSize="14" fontWeight="bold" fontFamily="Arial">so</text>
          </svg>
        </div>
        <div style={{
          fontSize: 32, fontWeight: 800, color: 'white',
          letterSpacing: 2,
          background: 'linear-gradient(135deg, #ff6b35, #e84393, #ffd93d)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          animation: 'fadeIn 0.8s ease',
        }}>
          SoCreate
        </div>
        <div style={{
          fontSize: 11, color: 'rgba(255,255,255,0.4)',
          marginTop: 8, letterSpacing: 3, textTransform: 'uppercase',
          animation: 'fadeIn 1.2s ease',
        }}>
          Professional 2D Animation
        </div>
        <div style={{
          marginTop: 30, width: 120, height: 3, borderRadius: 2,
          background: 'rgba(255,255,255,0.1)', overflow: 'hidden',
        }}>
          <div style={{
            width: '100%', height: '100%',
            background: 'linear-gradient(90deg, #ff6b35, #e84393)',
            animation: 'loading 2s ease forwards',
          }} />
        </div>
        <div style={{
          position: 'absolute', bottom: 30,
          fontSize: 9, color: 'rgba(255,255,255,0.25)',
          textAlign: 'center', lineHeight: 1.8,
        }}>
          Developed by Steven Michael Allen Owens<br />
          @SoQuarky · An AdventuresInDrawing Production
        </div>
        <style>{`
          @keyframes loading {
            from { transform: translateX(-100%); }
            to { transform: translateX(0); }
          }
          @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
          }
        `}</style>
      </div>
    );
  }

  return (
    <div
      style={{ position: 'fixed', inset: 0, overflow: 'hidden' }}
      onClick={handleCanvasClick}
    >
      {/* Drawing Canvas - Full screen */}
      <DrawingCanvas />

      {/* Brush cursor indicator */}
      <BrushCursorFollower />

      {/* === FLOATING BUTTONS === */}

      {/* Brush Tool */}
      <FloatingButton
        id="Brush"
        defaultPos={{ x: 20, y: 20 }}
        icon={<BrushIcon />}
        active={activeTool === 'brush'}
        onClick={() => {
          setActiveTool('brush');
          togglePanel('brush');
        }}
      />

      {/* Eraser Tool */}
      <FloatingButton
        id="Eraser"
        defaultPos={{ x: 20, y: 76 }}
        icon={<EraserIcon />}
        active={activeTool === 'eraser'}
        onClick={() => setActiveTool('eraser')}
      />

      {/* Pointer / Selection */}
      <FloatingButton
        id="Pointer"
        defaultPos={{ x: 20, y: 132 }}
        icon={<PointerIcon />}
        active={activeTool === 'pointer'}
        onClick={() => setActiveTool('pointer')}
      />

      {/* Layers */}
      <FloatingButton
        id="Layers"
        defaultPos={{ x: typeof window !== 'undefined' ? window.innerWidth - 60 : 200, y: 20 }}
        icon={<LayerIcon />}
        active={openPanels['layers']}
        onClick={() => togglePanel('layers')}
      />

      {/* Gallery */}
      <FloatingButton
        id="Gallery"
        defaultPos={{ x: typeof window !== 'undefined' ? window.innerWidth - 60 : 200, y: 76 }}
        icon={<GalleryIcon />}
        active={openPanels['gallery']}
        onClick={() => togglePanel('gallery')}
      />

      {/* Timeline */}
      <FloatingButton
        id="Timeline"
        defaultPos={{ x: typeof window !== 'undefined' ? window.innerWidth / 2 - 24 : 200, y: typeof window !== 'undefined' ? window.innerHeight - 60 : 500 }}
        icon={<TimelineIcon />}
        active={openPanels['timeline']}
        onClick={() => togglePanel('timeline')}
      />

      {/* Keyframe */}
      <FloatingButton
        id="Keyframe"
        defaultPos={{ x: typeof window !== 'undefined' ? window.innerWidth / 2 + 32 : 200, y: typeof window !== 'undefined' ? window.innerHeight - 60 : 500 }}
        icon={<KeyframeIcon />}
        onClick={() => {
          if (activeLayerId) {
            addKeyframe(activeLayerId, currentFrame);
          }
        }}
      />

      {/* Onion Skinning */}
      <FloatingButton
        id="Onion Skin"
        defaultPos={{ x: 20, y: 188 }}
        icon={<OnionIcon />}
        active={openPanels['onion']}
        onClick={() => togglePanel('onion')}
      />

      {/* Symmetry */}
      <FloatingButton
        id="Symmetry"
        defaultPos={{ x: 20, y: 244 }}
        icon={<SymmetryIcon />}
        active={openPanels['symmetry']}
        onClick={() => togglePanel('symmetry')}
      />

      {/* Transform */}
      <FloatingButton
        id="Transform"
        defaultPos={{ x: 20, y: 300 }}
        icon={<TransformIcon />}
        active={openPanels['transform']}
        onClick={() => togglePanel('transform')}
      />

      {/* Sprite Sheet */}
      <FloatingButton
        id="Sprite Sheet"
        defaultPos={{ x: 20, y: 356 }}
        icon={<SpriteIcon />}
        active={openPanels['spritesheet']}
        onClick={() => togglePanel('spritesheet')}
      />

      {/* Export */}
      <FloatingButton
        id="Export"
        defaultPos={{ x: typeof window !== 'undefined' ? window.innerWidth - 60 : 200, y: 132 }}
        icon={<ExportIcon />}
        active={openPanels['export']}
        onClick={() => togglePanel('export')}
      />

      {/* Color Effects */}
      <FloatingButton
        id="Color Effects"
        defaultPos={{ x: typeof window !== 'undefined' ? window.innerWidth - 60 : 200, y: 188 }}
        icon={<ColorIcon />}
        active={openPanels['coloreffects']}
        onClick={() => togglePanel('coloreffects')}
      />

      {/* Settings */}
      <FloatingButton
        id="Settings"
        defaultPos={{ x: typeof window !== 'undefined' ? window.innerWidth - 60 : 200, y: 244 }}
        icon={<SettingsIcon />}
        active={openPanels['settings']}
        onClick={() => togglePanel('settings')}
      />

      {/* Current color indicator */}
      <div
        style={{
          position: 'absolute', left: 20, bottom: 20,
          width: 36, height: 36, borderRadius: '50%',
          background: brushColor,
          border: '3px solid rgba(255,255,255,0.3)',
          boxShadow: '0 2px 10px rgba(0,0,0,0.3)',
          zIndex: 100,
          cursor: 'pointer',
        }}
        onClick={(e) => {
          e.stopPropagation();
          togglePanel('brush');
        }}
      />

      {/* === FLOATING PANELS === */}
      <BrushPanel />
      <LayersPanel />
      <GalleryPanel />
      <SettingsPanel />
      <Timeline />
      <OnionSkinPanel />
      <SymmetryPanel />
      <TransformPanel />
      <SpriteSheetPanel />
      <ExportPanel />
      <ColorEffectsPanel />

      {/* Welcome hints */}
      {showHints && (
        <div style={{
          position: 'fixed', inset: 0, zIndex: 9990,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          pointerEvents: 'none',
          animation: 'fadeIn 0.5s ease',
        }}>
          <div style={{
            background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(10px)',
            borderRadius: 16, padding: '20px 30px',
            color: 'white', textAlign: 'center',
            maxWidth: 320,
            border: '1px solid rgba(255,255,255,0.1)',
          }}>
            <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 8 }}>
              Welcome to SoCreate ✨
            </div>
            <div style={{ fontSize: 11, color: '#aaa', lineHeight: 1.6 }}>
              Start drawing immediately on the canvas.<br />
              All buttons are draggable — place them anywhere!<br />
              Tap a tool button to open its panel.<br />
              Tap outside any panel to close it.
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
