import { useEffect, useRef, useCallback, useState } from 'react';
import { useAppStore } from '../store';

export default function DrawingCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const [size, setSize] = useState({ width: window.innerWidth, height: window.innerHeight });

  const {
    activeTool, brushSize, brushOpacity, brushColor, brushType,
    layers, activeLayerId, onionSkin, symmetryMode,
    canvasWidth, canvasHeight,
  } = useAppStore();

  // Resize handler
  useEffect(() => {
    const handleResize = () => {
      setSize({ width: window.innerWidth, height: window.innerHeight });
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Setup canvases for each layer
  useEffect(() => {
    if (!canvasRef.current) return;
    
    // Set canvas to match screen resolution for crisp drawing
    const canvas = canvasRef.current;
    canvas.width = size.width * window.devicePixelRatio;
    canvas.height = size.height * window.devicePixelRatio;
    canvas.style.width = `${size.width}px`;
    canvas.style.height = `${size.height}px`;
    
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
      // Fill with canvas background
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, size.width, size.height);
    }
  }, [size]);

  // Drawing handlers
  const isDrawing = useRef(false);
  const lastPos = useRef<{ x: number; y: number } | null>(null);

  const getCanvasPos = useCallback((e: PointerEvent) => {
    if (!canvasRef.current) return { x: 0, y: 0 };
    const rect = canvasRef.current.getBoundingClientRect();
    return {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    };
  }, []);

  const handlePointerDown = useCallback((e: PointerEvent) => {
    if (activeTool === 'pointer') return;
    
    // Palm rejection
    if (useAppStore.getState().settings.palmRejection && e.pointerType === 'touch') {
      if (e.width > 20 || e.height > 15) return;
    }
    
    // Stylus only
    if (useAppStore.getState().settings.stylusOnly && e.pointerType !== 'pen') return;
    
    isDrawing.current = true;
    lastPos.current = getCanvasPos(e);
    
    // Draw initial dot
    const ctx = canvasRef.current?.getContext('2d');
    if (ctx && lastPos.current) {
      ctx.beginPath();
      ctx.arc(lastPos.current.x, lastPos.current.y, brushSize / 2, 0, Math.PI * 2);
      ctx.fillStyle = activeTool === 'eraser' ? '#ffffff' : brushColor;
      ctx.globalAlpha = brushOpacity / 100;
      ctx.fill();
      ctx.globalAlpha = 1;
    }
  }, [activeTool, brushSize, brushColor, brushOpacity, getCanvasPos]);

  const handlePointerMove = useCallback((e: PointerEvent) => {
    if (!isDrawing.current || !lastPos.current) return;
    
    const pos = getCanvasPos(e);
    const ctx = canvasRef.current?.getContext('2d');
    if (!ctx) return;
    
    ctx.beginPath();
    ctx.moveTo(lastPos.current.x, lastPos.current.y);
    ctx.lineTo(pos.x, pos.y);
    
    if (activeTool === 'brush') {
      ctx.strokeStyle = brushColor;
      ctx.lineWidth = brushSize;
      ctx.globalAlpha = brushOpacity / 100;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.stroke();
      ctx.globalAlpha = 1;
      
      // Symmetry
      if (symmetryMode !== 'none') {
        drawSymmetry(ctx, lastPos.current, pos);
      }
    } else if (activeTool === 'eraser') {
      ctx.globalCompositeOperation = 'destination-out';
      ctx.strokeStyle = 'rgba(0,0,0,1)';
      ctx.lineWidth = brushSize;
      ctx.stroke();
      ctx.globalCompositeOperation = 'source-over';
    }
    
    lastPos.current = pos;
  }, [activeTool, brushSize, brushColor, brushOpacity, symmetryMode, getCanvasPos]);

  const handlePointerUp = useCallback(() => {
    isDrawing.current = false;
    lastPos.current = null;
  }, []);

  const drawSymmetry = useCallback((ctx: CanvasRenderingContext2D, from: { x: number; y: number }, to: { x: number; y: number }) => {
    const cx = size.width / 2;
    const cy = size.height / 2;
    
    ctx.save();
    ctx.strokeStyle = brushColor;
    ctx.lineWidth = brushSize;
    ctx.globalAlpha = brushOpacity / 100;
    ctx.lineCap = 'round';
    
    switch (symmetryMode) {
      case 'h':
        ctx.beginPath();
        ctx.moveTo(size.width - from.x, from.y);
        ctx.lineTo(size.width - to.x, to.y);
        ctx.stroke();
        break;
      case 'v':
        ctx.beginPath();
        ctx.moveTo(from.x, size.height - from.y);
        ctx.lineTo(to.x, size.height - to.y);
        ctx.stroke();
        break;
      case 'quad':
        ctx.beginPath();
        ctx.moveTo(size.width - from.x, from.y);
        ctx.lineTo(size.width - to.x, to.y);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(from.x, size.height - from.y);
        ctx.lineTo(to.x, size.height - to.y);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(size.width - from.x, size.height - from.y);
        ctx.lineTo(size.width - to.x, size.height - to.y);
        ctx.stroke();
        break;
      default:
        if (symmetryMode.startsWith('rad') || symmetryMode === 'mandala') {
          const segments = symmetryMode === 'rad4' ? 4 : symmetryMode === 'rad6' ? 6 : symmetryMode === 'rad8' ? 8 : 12;
          for (let i = 1; i < segments; i++) {
            const angle = (i * 360 / segments) * Math.PI / 180;
            ctx.save();
            ctx.translate(cx, cy);
            ctx.rotate(angle);
            ctx.translate(-cx, -cy);
            ctx.beginPath();
            ctx.moveTo(from.x, from.y);
            ctx.lineTo(to.x, to.y);
            ctx.stroke();
            ctx.restore();
          }
        }
    }
    ctx.restore();
  }, [symmetryMode, brushSize, brushColor, brushOpacity, size]);

  // Onion skin rendering
  useEffect(() => {
    if (!onionSkin.enabled || !canvasRef.current) return;
    
    const ctx = canvasRef.current.getContext('2d');
    if (!ctx) return;
    
    // This would overlay previous/next frames
    // Simplified for now
  }, [onionSkin]);

  // Attach pointer events
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    
    canvas.addEventListener('pointerdown', handlePointerDown);
    canvas.addEventListener('pointermove', handlePointerMove);
    canvas.addEventListener('pointerup', handlePointerUp);
    canvas.addEventListener('pointerleave', handlePointerUp);
    canvas.addEventListener('pointercancel', handlePointerUp);
    
    return () => {
      canvas.removeEventListener('pointerdown', handlePointerDown);
      canvas.removeEventListener('pointermove', handlePointerMove);
      canvas.removeEventListener('pointerup', handlePointerUp);
      canvas.removeEventListener('pointerleave', handlePointerUp);
      canvas.removeEventListener('pointercancel', handlePointerUp);
    };
  }, [handlePointerDown, handlePointerMove, handlePointerUp]);

  // Sync layer canvases
  useEffect(() => {
    layers.forEach((layer) => {
      if (!layer.visible) return;
      // In a full implementation, each layer would have its own canvas
      // For now, we composite on the main canvas
    });
  }, [layers]);

  return (
    <div ref={containerRef} className="canvas-container">
      <canvas
        ref={canvasRef}
        style={{
          cursor: activeTool === 'pointer' ? 'default' : 'crosshair',
        }}
      />
    </div>
  );
}
