import { useState, useEffect, useRef } from 'react';
import { useAppStore } from '../store';

export default function Timeline() {
  const { 
    currentFrame, totalFrames, fps, isPlaying, togglePlayback, setCurrentFrame, 
    addKeyframe, removeKeyframe, layers, activeLayerId, openPanels, closePanel,
    setTotalFrames, setFps, onionSkin, setOnionSkin
  } = useAppStore();
  const [visible, setVisible] = useState(false);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    setVisible(!!openPanels['timeline']);
  }, [openPanels['timeline']]);

  // Playback loop
  useEffect(() => {
    if (isPlaying) {
      intervalRef.current = setInterval(() => {
        setCurrentFrame((currentFrame + 1) % totalFrames);
      }, 1000 / fps);
    } else if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isPlaying, fps, currentFrame, totalFrames, setCurrentFrame]);

  if (!visible) return null;

  const frames = [];
  for (let i = 0; i < totalFrames; i++) {
    const layer = layers.find(l => l.id === activeLayerId);
    const hasKeyframe = layer?.keyframes.includes(i);
    
    frames.push(
      <div
        key={i}
        className={`frame-cell ${i === currentFrame ? 'active' : ''} ${hasKeyframe ? 'has-keyframe' : ''}`}
        onClick={() => setCurrentFrame(i)}
      >
        {i + 1}
      </div>
    );
  }

  return (
    <div className="floating-panel expanded" style={{
      position: 'fixed',
      bottom: 20,
      left: '50%',
      transform: 'translateX(-50%)',
      width: 'min(90vw, 800px)',
      maxHeight: 180,
      zIndex: 80,
    }}>
      <div className="panel-header">
        <div className="settings-label" style={{ marginBottom: 0 }}>
          Timeline — Frame {currentFrame + 1}/{totalFrames}
        </div>
        <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
          <button className="kb-btn" onClick={togglePlayback}>
            {isPlaying ? '⏸' : '▶'}
          </button>
          <div style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
            <span style={{ fontSize: 9, color: 'var(--ui-text-muted)' }}>FPS</span>
            <input
              type="number"
              min={1}
              max={60}
              value={fps}
              onChange={e => setFps(Number(e.target.value))}
              style={{ width: 40, background: 'var(--ui-bg-solid)', border: '1px solid var(--ui-border)', borderRadius: 4, color: 'white', fontSize: 10, padding: '2px 4px' }}
            />
          </div>
          <button className="panel-close" onClick={() => closePanel('timeline')}>×</button>
        </div>
      </div>
      <div className="panel-body" style={{ padding: 8 }}>
        <div className="frames-row" style={{ display: 'flex', gap: 2, overflowX: 'auto', paddingBottom: 8 }}>
          {frames}
        </div>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', alignItems: 'center' }}>
          <button className="kb-btn" onClick={() => {
            if (activeLayerId) addKeyframe(activeLayerId, currentFrame);
          }}>
            🔑 Add Keyframe
          </button>
          <button className="kb-btn" onClick={() => {
            if (activeLayerId) removeKeyframe(activeLayerId, currentFrame);
          }}>
            🗑️ Remove Keyframe
          </button>
          <button className="kb-btn" onClick={() => setTotalFrames(totalFrames + 10)}>
            +10 Frames
          </button>
          <button
            className={`kb-btn ${onionSkin.enabled ? 'toggled' : ''}`}
            onClick={() => setOnionSkin({ enabled: !onionSkin.enabled })}
          >
            👁️ Onion Skin
          </button>
          {onionSkin.enabled && (
            <div style={{ display: 'flex', gap: 8, alignItems: 'center', fontSize: 10 }}>
              <span>Before: {onionSkin.framesBefore}</span>
              <input
                type="range"
                min={0}
                max={10}
                value={onionSkin.framesBefore}
                onChange={e => setOnionSkin({ framesBefore: Number(e.target.value) })}
                style={{ width: 60 }}
              />
              <span>After: {onionSkin.framesAfter}</span>
              <input
                type="range"
                min={0}
                max={10}
                value={onionSkin.framesAfter}
                onChange={e => setOnionSkin({ framesAfter: Number(e.target.value) })}
                style={{ width: 60 }}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
