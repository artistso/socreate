package com.socreate.app.core.model

/**
 * All user intents (actions) for the Drawing screen.
 * Following MVI pattern — these are the only way to modify state.
 */
sealed class DrawingIntent : MviIntent {

    // ─── Stroke Drawing ──────────────────────────────────────────────────────

    /** User started a new stroke (stylus/finger down) */
    data class StrokeStarted(
        val point: StrokePoint,
        val x: Float,
        val y: Float
    ) : DrawingIntent()

    /** User moved while drawing */
    data class StrokeMoved(
        val points: List<StrokePoint>
    ) : DrawingIntent()

    /** User lifted stylus/finger, completing the stroke */
    data class StrokeEnded(
        val finalPoint: StrokePoint?
    ) : DrawingIntent()

    /** Stroke was cancelled (e.g., palm rejection) */
    object StrokeCancelled : DrawingIntent()

    // ─── Tool Selection ──────────────────────────────────────────────────────

    data class SelectTool(val tool: DrawingTool) : DrawingIntent()
    data class SelectBrush(val brushId: String) : DrawingIntent()
    data class SetBrushSize(val size: Float) : DrawingIntent()
    data class SetBrushOpacity(val opacity: Float) : DrawingIntent()
    data class SetBrushSmoothing(val smoothing: Float) : DrawingIntent()
    data class SetBrushBlendMode(val blendMode: BlendMode) : DrawingIntent()
    data class UpdateBrushProperties(val properties: BrushProperties) : DrawingIntent()

    // ─── Color ───────────────────────────────────────────────────────────────

    data class SetColor(val color: SoCreateColor) : DrawingIntent()
    data class PickColorFromCanvas(val x: Float, val y: Float) : DrawingIntent()
    data class SetColorHarmony(val harmony: ColorHarmony) : DrawingIntent()
    data class AddRecentColor(val color: SoCreateColor) : DrawingIntent()
    data class ClearRecentColors(val color: SoCreateColor) : DrawingIntent()

    // ─── Layers ──────────────────────────────────────────────────────────────

    object AddLayer : DrawingIntent()
    data class DeleteLayer(val layerId: String) : DrawingIntent()
    data class SelectLayer(val layerId: String) : DrawingIntent()
    data class SetLayerOpacity(val layerId: String, val opacity: Float) : DrawingIntent()
    data class SetLayerBlendMode(val layerId: String, val blendMode: BlendMode) : DrawingIntent()
    data class SetLayerVisibility(val layerId: String, val isVisible: Boolean) : DrawingIntent()
    data class SetLayerLocked(val layerId: String, val isLocked: Boolean) : DrawingIntent()
    data class RenameLayer(val layerId: String, val name: String) : DrawingIntent()
    data class MoveLayer(val fromIndex: Int, val toIndex: Int) : DrawingIntent()
    data class MergeLayerDown(val layerId: String) : DrawingIntent()
    object DuplicateLayer : DrawingIntent()
    data class SetClippingMask(val layerId: String, val isClipping: Boolean) : DrawingIntent()

    // ─── Canvas Navigation ───────────────────────────────────────────────────

    data class Zoom(val factor: Float, val pivotX: Float, val pivotY: Float) : DrawingIntent()
    data class Pan(val deltaX: Float, val deltaY: Float) : DrawingIntent()
    data class SetViewTransform(val zoom: Float, val panX: Float, val panY: Float) : DrawingIntent()
    object FitToScreen : DrawingIntent()
    object ResetZoom : DrawingIntent()
    object ZoomIn : DrawingIntent()
    object ZoomOut : DrawingIntent()

    // ─── Undo/Redo ───────────────────────────────────────────────────────────

    object Undo : DrawingIntent()
    object Redo : DrawingIntent()

    // ─── Selection & Transform ───────────────────────────────────────────────

    data class StartSelection(val type: SelectionType, val startX: Float, val startY: Float) : DrawingIntent()
    data class UpdateSelection(val currentX: Float, val currentY: Float) : DrawingIntent()
    object ConfirmSelection : DrawingIntent()
    object ClearSelection : DrawingIntent()
    data class StartTransform(val selection: Selection) : DrawingIntent()
    object ConfirmTransform : DrawingIntent()
    object CancelTransform : DrawingIntent()

    // ─── Quick Actions ───────────────────────────────────────────────────────

    data class ShowQuickMenu(val x: Float, val y: Float) : DrawingIntent()
    object HideQuickMenu : DrawingIntent()

    // ─── UI Toggles ──────────────────────────────────────────────────────────

    object ToggleBrushPicker : DrawingIntent()
    object ToggleColorPicker : DrawingIntent()
    object ToggleLayerPanel : DrawingIntent()
    object ToggleFullscreen : DrawingIntent()
    object ToggleRulers : DrawingIntent()
    object ToggleGrid : DrawingIntent()
    object HideAllPanels : DrawingIntent()

    // ─── Project ─────────────────────────────────────────────────────────────

    object SaveProject : DrawingIntent()
    object ExportImage : DrawingIntent()
    data class LoadProject(val projectId: String) : DrawingIntent()
    data class RenameProject(val name: String) : DrawingIntent()

    // ─── Animation (future phase) ────────────────────────────────────────────

    data class SetFrame(val frame: Int) : DrawingIntent()
    object PlayAnimation : DrawingIntent()
    object StopAnimation : DrawingIntent()
    object AddFrame : DrawingIntent()
    object DuplicateFrame : DrawingIntent()
    object DeleteFrame : DrawingIntent()
    data class SetFrameRate(val fps: Int) : DrawingIntent()
    object ToggleOnionSkinning : DrawingIntent()

    // ─── Flood Fill ──────────────────────────────────────────────────────────

    data class FloodFill(val x: Float, val y: Float, val tolerance: Float = 0.1f) : DrawingIntent()

    // ─── Clipboard ───────────────────────────────────────────────────────────

    object CopySelection : DrawingIntent()
    object CutSelection : DrawingIntent()
    object PasteClipboard : DrawingIntent()
}

/**
 * Side effects for the Drawing screen.
 */
sealed class DrawingEffect : MviEffect {
    data class ShowToast(val message: String) : DrawingEffect()
    data class ShowColorPicker(val initialColor: SoCreateColor) : DrawingEffect()
    data class ShowBrushPicker(val currentBrushId: String) : DrawingEffect()
    data class ShowExportDialog(val format: String) : DrawingEffect()
    data class HapticFeedback(val intensity: Float = 1f) : DrawingEffect()
    data class PlaySound(val soundId: Int) : DrawingEffect()
    object NavigateToGallery : DrawingEffect()
    object ProjectSaved : DrawingEffect()
    data class ColorPicked(val color: SoCreateColor) : DrawingEffect()
}
