import { create } from 'zustand';
import { v4 as uuidv4 } from 'uuid';

// Layer colors for auto-assignment
const LAYER_COLORS = [
  '#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#9b59b6',
  '#e84393', '#00cec9', '#fdcb6e', '#e17055', '#74b9ff',
  '#a29bfe', '#fd79a8', '#55efc4', '#fab1a0', '#81ecec',
];

export interface Layer {
  id: string;
  name: string;
  visible: boolean;
  opacity: number;
  locked: boolean;
  color: string;
  blendMode: string;
  imageData: string | null;
  keyframes: number[];
}

export interface Frame {
  id: string;
  layerData: Record<string, string | null>;
}

export interface Project {
  id: string;
  name: string;
  thumbnail: string | null;
  createdAt: number;
  updatedAt: number;
  layers: Layer[];
  currentFrame: number;
  totalFrames: number;
  fps: number;
}

export interface FloatingPosition {
  x: number;
  y: number;
}

export interface OnionSkinSettings {
  enabled: boolean;
  framesBefore: number;
  framesAfter: number;
  opacityBefore: number;
  opacityAfter: number;
}

export interface AppSettings {
  palmRejection: boolean;
  stylusOnly: boolean;
  lowLatency: boolean;
  interfaceScale: number;
  fontScale: number;
  thumbnailSize: 'small' | 'medium' | 'large';
  thumbnailScope: 'canvas' | 'layer';
}

interface AppState {
  // Active tool
  activeTool: string;
  setActiveTool: (tool: string) => void;

  // Brush settings
  brushSize: number;
  brushOpacity: number;
  brushColor: string;
  setBrushSize: (size: number) => void;
  setBrushOpacity: (opacity: number) => void;
  setBrushColor: (color: string) => void;
  brushType: string;
  setBrushType: (type: string) => void;

  // Layers
  layers: Layer[];
  activeLayerId: string | null;
  addLayer: () => void;
  removeLayer: (id: string) => void;
  setActiveLayer: (id: string) => void;
  toggleLayerVisibility: (id: string) => void;
  reorderLayers: (fromIndex: number, toIndex: number) => void;
  setLayerOpacity: (id: string, opacity: number) => void;
  setLayerBlendMode: (id: string, mode: string) => void;
  updateLayerData: (id: string, data: string | null) => void;
  duplicateLayer: (id: string) => void;
  mergeLayerDown: (id: string) => void;

  // Timeline / Animation
  currentFrame: number;
  totalFrames: number;
  fps: number;
  isPlaying: boolean;
  maxTime: number;
  setCurrentFrame: (frame: number) => void;
  setTotalFrames: (total: number) => void;
  setFps: (fps: number) => void;
  togglePlayback: () => void;
  addKeyframe: (layerId: string, frame: number) => void;
  removeKeyframe: (layerId: string, frame: number) => void;

  // Onion Skinning
  onionSkin: OnionSkinSettings;
  setOnionSkin: (settings: Partial<OnionSkinSettings>) => void;

  // Panels
  openPanels: Record<string, boolean>;
  togglePanel: (panelId: string) => void;
  closePanel: (panelId: string) => void;

  // Floating positions
  floatingPositions: Record<string, FloatingPosition>;
  setFloatingPosition: (id: string, pos: FloatingPosition) => void;
  savePositions: () => void;
  loadPositions: () => void;

  // Gallery / Projects
  projects: Project[];
  activeProjectId: string | null;
  newProject: () => void;
  loadProject: (id: string) => void;
  saveCurrentProject: () => void;

  // Settings
  settings: AppSettings;
  updateSettings: (s: Partial<AppSettings>) => void;

  // Keyboard toggles
  ctrlActive: boolean;
  shiftActive: boolean;
  altActive: boolean;
  toggleCtrl: () => void;
  toggleShift: () => void;
  toggleAlt: () => void;

  // Symmetry
  symmetryMode: string;
  setSymmetryMode: (mode: string) => void;

  // Canvas
  canvasWidth: number;
  canvasHeight: number;
}

const defaultLayer = (): Layer => {
  const id = uuidv4();
  return {
    id,
    name: `Layer 1`,
    visible: true,
    opacity: 100,
    locked: false,
    color: LAYER_COLORS[0],
    blendMode: 'normal',
    imageData: null,
    keyframes: [0],
  };
};

const loadSavedPositions = (): Record<string, FloatingPosition> => {
  try {
    const saved = localStorage.getItem('socreate-positions');
    if (saved) return JSON.parse(saved);
  } catch {}
  return {};
};

const loadSavedProjects = (): Project[] => {
  try {
    const saved = localStorage.getItem('socreate-projects');
    if (saved) return JSON.parse(saved);
  } catch {}
  return [];
};

const firstLayer = defaultLayer();

export const useAppStore = create<AppState>((set, get) => ({
  activeTool: 'brush',
  setActiveTool: (tool) => set({ activeTool: tool }),

  brushSize: 8,
  brushOpacity: 100,
  brushColor: '#000000',
  setBrushSize: (size) => set({ brushSize: size }),
  setBrushOpacity: (opacity) => set({ brushOpacity: opacity }),
  setBrushColor: (color) => set({ brushColor: color }),
  brushType: 'round',
  setBrushType: (type) => set({ brushType: type }),

  layers: [firstLayer],
  activeLayerId: firstLayer.id,
  addLayer: () => {
    const state = get();
    const idx = state.layers.length;
    const newLayer: Layer = {
      id: uuidv4(),
      name: `Layer ${idx + 1}`,
      visible: true,
      opacity: 100,
      locked: false,
      color: LAYER_COLORS[idx % LAYER_COLORS.length],
      blendMode: 'normal',
      imageData: null,
      keyframes: [state.currentFrame],
    };
    set({ layers: [...state.layers, newLayer], activeLayerId: newLayer.id });
  },
  removeLayer: (id) => {
    const state = get();
    if (state.layers.length <= 1) return;
    const newLayers = state.layers.filter((l) => l.id !== id);
    const newActive = state.activeLayerId === id ? newLayers[0].id : state.activeLayerId;
    set({ layers: newLayers, activeLayerId: newActive });
  },
  setActiveLayer: (id) => set({ activeLayerId: id }),
  toggleLayerVisibility: (id) => {
    set((state) => ({
      layers: state.layers.map((l) => l.id === id ? { ...l, visible: !l.visible } : l),
    }));
  },
  reorderLayers: (from, to) => {
    const state = get();
    const arr = [...state.layers];
    const [item] = arr.splice(from, 1);
    arr.splice(to, 0, item);
    set({ layers: arr });
  },
  setLayerOpacity: (id, opacity) => {
    set((state) => ({
      layers: state.layers.map((l) => l.id === id ? { ...l, opacity } : l),
    }));
  },
  setLayerBlendMode: (id, mode) => {
    set((state) => ({
      layers: state.layers.map((l) => l.id === id ? { ...l, blendMode: mode } : l),
    }));
  },
  updateLayerData: (id, data) => {
    set((state) => ({
      layers: state.layers.map((l) => l.id === id ? { ...l, imageData: data } : l),
    }));
  },
  duplicateLayer: (id) => {
    const state = get();
    const layer = state.layers.find((l) => l.id === id);
    if (!layer) return;
    const idx = state.layers.length;
    const dup: Layer = {
      ...layer,
      id: uuidv4(),
      name: `${layer.name} (copy)`,
      color: LAYER_COLORS[idx % LAYER_COLORS.length],
    };
    set({ layers: [...state.layers, dup], activeLayerId: dup.id });
  },
  mergeLayerDown: (id) => {
    const state = get();
    const idx = state.layers.findIndex((l) => l.id === id);
    if (idx <= 0) return;
    const newLayers = state.layers.filter((_, i) => i !== idx);
    set({ layers: newLayers, activeLayerId: newLayers[idx - 1].id });
  },

  currentFrame: 0,
  totalFrames: 60,
  fps: 24,
  isPlaying: false,
  maxTime: 180,
  setCurrentFrame: (frame) => set({ currentFrame: frame }),
  setTotalFrames: (total) => set({ totalFrames: Math.min(total, get().maxTime * get().fps) }),
  setFps: (fps) => set({ fps: Math.min(fps, 60) }),
  togglePlayback: () => set((s) => ({ isPlaying: !s.isPlaying })),
  addKeyframe: (layerId, frame) => {
    set((state) => ({
      layers: state.layers.map((l) =>
        l.id === layerId
          ? { ...l, keyframes: [...new Set([...l.keyframes, frame])].sort((a, b) => a - b) }
          : l
      ),
    }));
  },
  removeKeyframe: (layerId, frame) => {
    set((state) => ({
      layers: state.layers.map((l) =>
        l.id === layerId
          ? { ...l, keyframes: l.keyframes.filter((f) => f !== frame) }
          : l
      ),
    }));
  },

  onionSkin: { enabled: false, framesBefore: 2, framesAfter: 1, opacityBefore: 30, opacityAfter: 20 },
  setOnionSkin: (settings) => set((s) => ({ onionSkin: { ...s.onionSkin, ...settings } })),

  openPanels: {},
  togglePanel: (panelId) => set((s) => ({
    openPanels: { ...s.openPanels, [panelId]: !s.openPanels[panelId] },
  })),
  closePanel: (panelId) => set((s) => ({
    openPanels: { ...s.openPanels, [panelId]: false },
  })),

  floatingPositions: loadSavedPositions(),
  setFloatingPosition: (id, pos) => set((s) => ({
    floatingPositions: { ...s.floatingPositions, [id]: pos },
  })),
  savePositions: () => {
    const positions = get().floatingPositions;
    localStorage.setItem('socreate-positions', JSON.stringify(positions));
  },
  loadPositions: () => {
    const positions = loadSavedPositions();
    set({ floatingPositions: positions });
  },

  projects: loadSavedProjects(),
  activeProjectId: null,
  newProject: () => {
    const state = get();
    if (state.layers.length > 0) {
      state.saveCurrentProject();
    }
    const newLayer = defaultLayer();
    set({
      layers: [newLayer],
      activeLayerId: newLayer.id,
      currentFrame: 0,
      totalFrames: 60,
      activeProjectId: uuidv4(),
    });
  },
  loadProject: (id) => {
    const project = get().projects.find((p) => p.id === id);
    if (project) {
      set({
        layers: project.layers,
        activeLayerId: project.layers[0]?.id || null,
        currentFrame: project.currentFrame,
        totalFrames: project.totalFrames,
        fps: project.fps,
        activeProjectId: project.id,
      });
    }
  },
  saveCurrentProject: () => {
    const state = get();
    const projectId = state.activeProjectId || uuidv4();
    const project: Project = {
      id: projectId,
      name: `Project ${state.projects.length + 1}`,
      thumbnail: null,
      createdAt: Date.now(),
      updatedAt: Date.now(),
      layers: state.layers,
      currentFrame: state.currentFrame,
      totalFrames: state.totalFrames,
      fps: state.fps,
    };
    const existing = state.projects.findIndex((p) => p.id === projectId);
    let newProjects: Project[];
    if (existing >= 0) {
      newProjects = [...state.projects];
      newProjects[existing] = project;
    } else {
      newProjects = [...state.projects, project];
    }
    set({ projects: newProjects, activeProjectId: projectId });
    try {
      localStorage.setItem('socreate-projects', JSON.stringify(newProjects));
    } catch {}
  },

  settings: {
    palmRejection: true,
    stylusOnly: false,
    lowLatency: true,
    interfaceScale: 1,
    fontScale: 1,
    thumbnailSize: 'medium',
    thumbnailScope: 'canvas',
  },
  updateSettings: (s) => set((state) => ({ settings: { ...state.settings, ...s } })),

  ctrlActive: false,
  shiftActive: false,
  altActive: false,
  toggleCtrl: () => set((s) => ({ ctrlActive: !s.ctrlActive })),
  toggleShift: () => set((s) => ({ shiftActive: !s.shiftActive })),
  toggleAlt: () => set((s) => ({ altActive: !s.altActive })),

  symmetryMode: 'none',
  setSymmetryMode: (mode) => set({ symmetryMode: mode }),

  canvasWidth: 2800,
  canvasHeight: 1752,
}));
