import { useState, useRef, useEffect, type ReactNode } from 'react';
import { useAppStore, type FloatingPosition } from '../store';

interface FloatingButtonProps {
  id: string;
  defaultPos: FloatingPosition;
  icon: ReactNode;
  active?: boolean;
  onClick?: () => void;
}

export default function FloatingButton({ id, defaultPos, icon, active, onClick }: FloatingButtonProps) {
  const { floatingPositions, setFloatingPosition } = useAppStore();
  const [isDragging, setIsDragging] = useState(false);
  const [position, setPosition] = useState<FloatingPosition>(
    floatingPositions[id] || defaultPos
  );
  const startPos = useRef<FloatingPosition>({ x: 0, y: 0 });
  const btnRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Restore saved position
    if (floatingPositions[id]) {
      setPosition(floatingPositions[id]);
    }
  }, [id, floatingPositions]);

  const handlePointerDown = (e: React.PointerEvent) => {
    if (e.button !== 0) return;
    setIsDragging(true);
    startPos.current = {
      x: e.clientX - position.x,
      y: e.clientY - position.y,
    };
    (e.target as HTMLElement).setPointerCapture(e.pointerId);
    btnRef.current?.classList.add('dragging');
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!isDragging) return;
    const newX = e.clientX - startPos.current.x;
    const newY = e.clientY - startPos.current.y;
    setPosition({ x: newX, y: newY });
  };

  const handlePointerUp = () => {
    if (isDragging) {
      setIsDragging(false);
      btnRef.current?.classList.remove('dragging');
      setFloatingPosition(id, position);
    }
  };

  const handleClick = () => {
    if (!isDragging && onClick) {
      onClick();
    }
  };

  return (
    <div
      ref={btnRef}
      className={`floating-btn ${active ? 'active' : ''}`}
      style={{ left: position.x, top: position.y }}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onClick={handleClick}
    >
      {icon}
    </div>
  );
}
