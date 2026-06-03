package com.socreate.app.core.model

import com.socreate.app.engine.renderer.SmartShapeResult

/**
 * Complete state for the Drawing screen.
 * Immutable — all changes produce a new state via the reducer.
 *
 * Defaults are optimized for Samsung Galaxy Tab S10+.
 */
data class DrawingState(
    // Canvas (Tab S10+ native: 2800×1752)
    val canvas: Canvas = Canvas.DEFAULT,
    val zoom: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val rotation: Float = 0f,

    // Layers
    val layerStack: LayerStack = LayerStack.createDefault(),

    // Current tool
    val activeTool: DrawingTool = DrawingTool.BRUSH,
    val activeBrushId: String = Brush.HB_PENCIL_ID,
    val activeBrushProperties: BrushProperties = BrushProperties(),
    val activeColor: SoCreateColor = SoCreateColor.BLACK,
    val brushSize: Float = 10f,
    val brushOpacity: Float = 1f,
    val brushSmoothing: Float = 0.3f,

    // Stroke being drawn
    val currentStroke: Stroke? = null,
    val isDrawing: Boolean = false,

    // Selection
    val selection: Selection? = null,
    val isTransforming: Boolean = false,

    // UI state
    val showBrushPicker: Boolean = false,
    val showColorPicker: Boolean = false,
    val showLayerPanel: Boolean = false,
    val showQuickMenu: Boolean = false,
    val isFullscreen: Boolean = true,
    val showRulers: Boolean = false,
    val showGrid: Boolean = false,
    val quickMenuPosition: Pair<Float, Float> = 0f to 0f,

    // S Pen state
    val spenButtonHeld: Boolean = false,
    val spenHovering: Boolean = false,
    val spenHoverX: Float = 0f,
    val spenHoverY: Float = 0f,

    // Undo/Redo
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val undoCount: Int = 0,
    val redoCount: Int = 0,

    // Recent colors
    val recentColors: List<SoCreateColor> = emptyList(),
    val colorHarmony: ColorHarmony = ColorHarmony.NONE,

    // Project info
    val project: Project? = null,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false,

    // Animation (future phase)
    val animationTimeline: AnimationTimeline? = null,
    val isPlayingAnimation: Boolean = false,
    val currentFrame: Int = 0,

    // ─── Advanced Features (from FlipaClip, ibisPaint, CSP, HiPaint) ─────────

    // Symmetry (HiPaint)
    val symmetryConfig: SymmetryConfig = SymmetryConfig(),

    // Reference images (HiPaint)
    val referenceImages: List<ReferenceImage> = emptyList(),

    // Smart shape detection (Clip Studio v5)
    val activeSmartShape: SmartShape? = null,
    val smartShapeResult: SmartShapeResult? = null,

    // Puppet warp (Clip Studio Paint)
    val activePuppetWarp: PuppetWarp? = null,

    // Liquify (Clip Studio Paint)
    val liquifySettings: LiquifySettings = LiquifySettings(),
    val isLiquifying: Boolean = false,

    // Fill (ibisPaint)
    val fillSettings: FillSettings = FillSettings(),

    // Shading assist (Clip Studio v5)
    val shadingAssist: ShadingAssistConfig = ShadingAssistConfig(),

    // Velocity brush (Clip Studio v5)
    val velocitySettings: VelocitySettings = VelocitySettings(),

    // Extended canvas (FlipaClip Beta)
    val extendedCanvas: ExtendedCanvas? = null,

    // Gesture config (HiPaint)
    val gestureConfig: GestureConfig = GestureConfig(),

    // Auto-save (Clip Studio v5)
    val autoSaveConfig: AutoSaveConfig = AutoSaveConfig(),
    val hasUnsavedChanges: Boolean = false,

    // Time-lapse (ibisPaint / HiPaint)
    val timelapseConfig: TimelapseConfig = TimelapseConfig(),

    // Floating panels (ibisPaint v14)
    val floatingPanels: List<FloatingPanel> = emptyList(),

    // Numeric input (ibisPaint)
    val numericInputTarget: NumericInputTarget? = null,
    val showNumericInput: Boolean = false,

    // Audio tracks (FlipaClip)
    val audioTracks: List<AudioTrack> = emptyList(),

    // Device info
    val displayRefreshRate: Int = TabS10Plus.REFRESH_RATE_HZ,
    val supportsWideGamut: Boolean = true,

    // ─── Color Theme System ─────────────────────────────────────────────────
    val activeTheme: AppTheme = ThemePresets.DEFAULT_DARK,
    val availableThemes: List<AppTheme> = ThemePresets.ALL,

    // ─── Enhanced Onion Skin ────────────────────────────────────────────────
    val enhancedOnionSkin: OnionSkinConfig = OnionSkinConfig(),

    // ─── Layer Outlining (Aseprite-style) ──────────────────────────────────
    val layerOutlineConfig: LayerOutlineConfig = LayerOutlineConfig(),

    // ─── Multi-Frame Selection (Aseprite/Resprite) ─────────────────────────
    val multiFrameSelection: MultiFrameSelection = MultiFrameSelection(),
    val frameTags: List<FrameTag> = emptyList(),
    val celLinks: List<CelLink> = emptyList(),
    val frameColorCodes: Map<Int, FrameColor> = emptyMap(),

    // ─── On-Screen Modifier Keys ────────────────────────────────────────────
    val modifierKeys: ModifierKeyState = ModifierKeyState(),

    // ─── Puppet Mesh Tools ──────────────────────────────────────────────────
    val activePuppetMesh: PuppetMesh? = null,
    val meshTool: MeshTool = MeshTool.SELECT,
    val meshGenConfig: MeshGenConfig = MeshGenConfig(),
    val meshPoses: List<MeshPose> = emptyList(),

    // ─── User Account & Privacy ────────────────────────────────────────────
    val userAccount: UserAccount = UserAccount(),

    // ─── YouTube Sharing ────────────────────────────────────────────────────
    val youtubeShareConfig: YouTubeShareConfig = YouTubeShareConfig(),

    // ─── Artistso.com Integration ──────────────────────────────────────────
    val artistsoConfig: ArtistsoConfig = ArtistsoConfig(),
    val artistsoContent: List<ArtistsoContent> = emptyList(),

    // ─── Screen Overlay ────────────────────────────────────────────────────
    val overlayConfig: OverlayConfig = OverlayConfig(),

    // ─── Crash Reports (User-Owned) ────────────────────────────────────────
    val localCrashReports: List<CrashReport> = emptyList()
) : MviState {

    val viewTransform: ViewTransform
        get() = ViewTransform(zoom, panX, panY, rotation)

    fun screenToCanvas(screenX: Float, screenY: Float): Pair<Float, Float> {
        val cx = (screenX - panX) / zoom
        val cy = (screenY - panY) / zoom
        return cx to cy
    }

    fun canvasToScreen(canvasX: Float, canvasY: Float): Pair<Float, Float> {
        return (canvasX * zoom + panX) to (canvasY * zoom + panY)
    }
}

data class ViewTransform(
    val zoom: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val rotation: Float = 0f
)

enum class DrawingTool {
    BRUSH,
    ERASER,
    SMUDGE,
    SELECT,
    LASSO,
    TRANSFORM,
    WARP,
    FLOOD_FILL,
    EYEDROPPER,
    CLIPBOARD_PASTE,
    TEXT,
    SHAPE
}

data class Selection(
    val bounds: Bounds,
    val type: SelectionType = SelectionType.RECTANGLE,
    val isActive: Boolean = true,
    val mask: List<StrokePoint>? = null
)

enum class SelectionType {
    RECTANGLE, ELLIPSE, LASSO, AUTO
}

enum class ColorHarmony {
    NONE, COMPLEMENTARY, ANALOGOUS, TRIADIC, SPLIT_COMPLEMENTARY, TETRADIC
}
