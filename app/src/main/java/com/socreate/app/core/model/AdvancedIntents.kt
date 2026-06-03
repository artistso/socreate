package com.socreate.app.core.model

/**
 * Additional intents for features inspired by FlipaClip, ibisPaint,
 * Clip Studio Paint, HiPaint, Aseprite, and Resprite.
 */
sealed class AdvancedIntent : MviIntent {

    // ─── Symmetry Drawing (HiPaint) ──────────────────────────────────────

    data class SetSymmetry(val config: SymmetryConfig) : AdvancedIntent()
    data class ToggleSymmetry(val type: SymmetryType = SymmetryType.VERTICAL) : AdvancedIntent()
    object CycleSymmetryType : AdvancedIntent()

    // ─── Reference Images (HiPaint) ─────────────────────────────────────

    data class AddReferenceImage(val filePath: String, val name: String) : AdvancedIntent()
    data class RemoveReferenceImage(val id: String) : AdvancedIntent()
    data class MoveReferenceImage(val id: String, val x: Float, val y: Float) : AdvancedIntent()
    data class ScaleReferenceImage(val id: String, val scale: Float) : AdvancedIntent()
    data class SetReferenceOpacity(val id: String, val opacity: Float) : AdvancedIntent()
    data class ToggleReferenceVisibility(val id: String) : AdvancedIntent()

    // ─── Audio Tracks (FlipaClip) ────────────────────────────────────────

    data class ImportAudio(val filePath: String, val name: String) : AdvancedIntent()
    data class RemoveAudio(val trackId: String) : AdvancedIntent()
    data class SetAudioStartFrame(val trackId: String, val frame: Int) : AdvancedIntent()
    data class SetAudioVolume(val trackId: String, val volume: Float) : AdvancedIntent()
    data class ToggleAudioMute(val trackId: String) : AdvancedIntent()
    data class TrimAudio(val trackId: String, val startMs: Long, val endMs: Long) : AdvancedIntent()
    object RecordVoice : AdvancedIntent()
    object StopRecording : AdvancedIntent()

    // ─── Animation Frames (FlipaClip Beta) ───────────────────────────────

    data class SetFrameDuration(val frameId: String, val duration: Float) : AdvancedIntent()
    data class DuplicateFrameToPosition(val frameId: String, val targetPosition: Int) : AdvancedIntent()
    data class CopyFrameAcrossTracks(
        val sourceLayerId: String,
        val sourceFrameIndex: Int,
        val targetLayerId: String,
        val targetFrameIndex: Int
    ) : AdvancedIntent()
    data class LabelFrame(val frameId: String, val label: String) : AdvancedIntent()
    data class SetFrameAsKeyframe(val frameId: String, val isKeyframe: Boolean) : AdvancedIntent()

    // ─── Smart Shapes (Clip Studio v5) ──────────────────────────────────

    data class ActivateSmartShape(val shape: SmartShapeType) : AdvancedIntent()
    data class ConfirmSmartShape(val shape: SmartShape) : AdvancedIntent()
    data class CancelSmartShape(val shape: SmartShape) : AdvancedIntent()
    object CycleSmartShape : AdvancedIntent()

    // ─── Puppet Warp (Clip Studio Paint) ────────────────────────────────

    data class StartPuppetWarp(val selectionBounds: Bounds) : AdvancedIntent()
    data class AddWarpPin(val x: Float, val y: Float) : AdvancedIntent()
    data class MoveWarpPin(val pinId: String, val x: Float, val y: Float) : AdvancedIntent()
    data class RemoveWarpPin(val pinId: String) : AdvancedIntent()
    data class SetPinFixed(val pinId: String, val isFixed: Boolean) : AdvancedIntent()
    object ConfirmPuppetWarp : AdvancedIntent()
    object CancelPuppetWarp : AdvancedIntent()

    // ─── Auto-Save (Clip Studio v5) ─────────────────────────────────────

    data class SetAutoSaveConfig(val config: AutoSaveConfig) : AdvancedIntent()
    object TriggerManualSave : AdvancedIntent()
    data class RecoverFromCrash(val snapshotId: String) : AdvancedIntent()
    object DismissRecovery : AdvancedIntent()

    // ─── Brush Patterns (ibisPaint v13) ─────────────────────────────────

    data class CreateBrushPattern(
        val name: String,
        val shapeBitmapPath: String?,
        val grainBitmapPath: String?
    ) : AdvancedIntent()
    data class ImportBrushPattern(val filePath: String) : AdvancedIntent()
    data class ApplyBrushPattern(val patternId: String, val brushId: String) : AdvancedIntent()

    // ─── Gesture Config (HiPaint) ───────────────────────────────────────

    data class SetGestureConfig(val config: GestureConfig) : AdvancedIntent()
    data class TwoFingerUndo(val tapCount: Int) : AdvancedIntent()
    data class ThreeFingerRedo(val tapCount: Int) : AdvancedIntent()
    data class LongPressEyedropper(val x: Float, val y: Float) : AdvancedIntent()

    // ─── Numeric Input (ibisPaint) ──────────────────────────────────────

    data class ShowNumericInput(
        val target: NumericInputTarget,
        val currentValue: Float,
        val min: Float,
        val max: Float
    ) : AdvancedIntent()
    data class ApplyNumericInput(val value: Float) : AdvancedIntent()
    object DismissNumericInput : AdvancedIntent()

    // ─── Time-lapse (ibisPaint / HiPaint) ───────────────────────────────

    object StartTimelapse : AdvancedIntent()
    object PauseTimelapse : AdvancedIntent()
    object ResumeTimelapse : AdvancedIntent()
    object StopTimelapse : AdvancedIntent()
    data class ExportTimelapse(val config: TimelapseConfig) : AdvancedIntent()

    // ─── Extended Canvas (FlipaClip Beta) ───────────────────────────────

    data class SetExtendedCanvas(val config: ExtendedCanvas) : AdvancedIntent()
    object ToggleCanvasBounds : AdvancedIntent()

    // ─── Velocity Brush (Clip Studio v5) ────────────────────────────────

    data class SetVelocitySettings(val settings: VelocitySettings) : AdvancedIntent()
    data class ToggleVelocity(val isEnabled: Boolean) : AdvancedIntent()

    // ─── Liquify (Clip Studio Paint) ────────────────────────────────────

    data class SetLiquifyMode(val mode: LiquifyMode) : AdvancedIntent()
    data class SetLiquifyBrushSize(val size: Float) : AdvancedIntent()
    data class SetLiquifyPressure(val pressure: Float) : AdvancedIntent()
    object ConfirmLiquify : AdvancedIntent()
    object CancelLiquify : AdvancedIntent()

    // ─── Surrounding Fill (ibisPaint) ───────────────────────────────────

    data class SetFillMode(val mode: FillMode) : AdvancedIntent()
    data class ExecuteFill(val settings: FillSettings, val x: Float, val y: Float) : AdvancedIntent()
    data class SetFillTolerance(val tolerance: Float) : AdvancedIntent()

    // ─── Shading Assist (Clip Studio v5) ────────────────────────────────

    data class SetShadingAssist(val config: ShadingAssistConfig) : AdvancedIntent()
    object ApplyShadingAssist : AdvancedIntent()
    data class SetShadingLightAngle(val angle: Float) : AdvancedIntent()

    // ─── Color Match (Clip Studio v5) ──────────────────────────────────

    data class SetColorMatchReference(val imagePath: String) : AdvancedIntent()
    data class ApplyColorMatch(val config: ColorMatchConfig) : AdvancedIntent()

    // ─── Floating Panels (ibisPaint v14) ────────────────────────────────

    data class ShowFloatingPanel(val type: FloatingPanelType) : AdvancedIntent()
    data class MoveFloatingPanel(val panelId: String, val x: Float, val y: Float) : AdvancedIntent()
    data class ResizeFloatingPanel(val panelId: String, val width: Float, val height: Float) : AdvancedIntent()
    data class TogglePanelCollapsed(val panelId: String) : AdvancedIntent()
    object HideAllFloatingPanels : AdvancedIntent()

    // ═══════════════════════════════════════════════════════════════════════
    // NEW FEATURES — v1.1+
    // ═══════════════════════════════════════════════════════════════════════

    // ─── Color Theme System ─────────────────────────────────────────────

    data class SetTheme(val themeId: String) : AdvancedIntent()
    data class SetCustomTheme(val theme: AppTheme) : AdvancedIntent()
    data class ExportTheme(val themeId: String) : AdvancedIntent()
    data class ImportTheme(val jsonPath: String) : AdvancedIntent()
    object ShowThemePicker : AdvancedIntent()
    object CycleTheme : AdvancedIntent()

    // ─── Enhanced Onion Skin ────────────────────────────────────────────

    data class SetOnionSkinConfig(val config: OnionSkinConfig) : AdvancedIntent()
    data class SetOnionSkinMode(val mode: OnionSkinMode) : AdvancedIntent()
    data class SetOnionBlendMode(val mode: OnionBlendMode) : AdvancedIntent()
    data class SetOnionEffectOverlay(val overlay: OnionEffectOverlay) : AdvancedIntent()
    data class SetOnionFramesBefore(val count: Int) : AdvancedIntent()
    data class SetOnionFramesAfter(val count: Int) : AdvancedIntent()
    data class ToggleOnionMotionTrail(val isEnabled: Boolean) : AdvancedIntent()
    data class ToggleOnionGhostFrames(val isEnabled: Boolean) : AdvancedIntent()

    // ─── Layer Outlining (Aseprite-style) ───────────────────────────────

    data class SetLayerOutlineConfig(val config: LayerOutlineConfig) : AdvancedIntent()
    data class ToggleLayerOutline(val isEnabled: Boolean) : AdvancedIntent()
    data class SetLayerOutlineMode(val mode: LayerOutlineMode) : AdvancedIntent()
    data class SetLayerOutlineColor(val color: SoCreateColor) : AdvancedIntent()
    data class SetOutlineDashPattern(val pattern: OutlineDashPattern) : AdvancedIntent()

    // ─── Multi-Frame Selection (Aseprite/Resprite) ─────────────────────

    data class SelectFrame(val frameIndex: Int) : AdvancedIntent()
    data class DeselectFrame(val frameIndex: Int) : AdvancedIntent()
    data class ToggleFrameSelection(val frameIndex: Int) : AdvancedIntent()
    data class SelectFrameRange(val start: Int, val end: Int) : AdvancedIntent()
    data class SelectAllFrames(val totalFrames: Int) : AdvancedIntent()
    object ClearFrameSelection : AdvancedIntent()
    data class ApplyBatchAction(val action: BatchAction) : AdvancedIntent()

    // ─── On-Screen Modifier Keys (Aseprite/Resprite) ───────────────────

    data class SetCtrlPressed(val isPressed: Boolean) : AdvancedIntent()
    data class SetShiftPressed(val isPressed: Boolean) : AdvancedIntent()
    data class SetAltPressed(val isPressed: Boolean) : AdvancedIntent()
    data class SetMetaPressed(val isPressed: Boolean) : AdvancedIntent()
    data class SetModifierButtonPosition(val position: ModifierButtonPosition) : AdvancedIntent()
    data class ToggleOnScreenModifiers(val isVisible: Boolean) : AdvancedIntent()

    // ─── Frame Tags (Aseprite-style) ───────────────────────────────────

    data class AddFrameTag(val tag: FrameTag) : AdvancedIntent()
    data class RemoveFrameTag(val tagId: String) : AdvancedIntent()
    data class RenameFrameTag(val tagId: String, val name: String) : AdvancedIntent()
    data class SetFrameColorCode(val frameIndex: Int, val color: FrameColor) : AdvancedIntent()
    data class ClearFrameColorCode(val frameIndex: Int) : AdvancedIntent()

    // ─── Puppet Mesh Tools ──────────────────────────────────────────────

    data class GenerateMesh(val config: MeshGenConfig) : AdvancedIntent()
    data class SetMeshTool(val tool: MeshTool) : AdvancedIntent()
    data class AddMeshPin(
        val x: Float, val y: Float,
        val type: PinType = PinType.JOINT,
        val name: String = ""
    ) : AdvancedIntent()
    data class RemoveMeshPin(val pinId: String) : AdvancedIntent()
    data class MoveMeshPin(val pinId: String, val x: Float, val y: Float) : AdvancedIntent()
    data class AddMeshBone(
        val startPinId: String, val endPinId: String,
        val name: String = ""
    ) : AdvancedIntent()
    data class RemoveMeshBone(val boneId: String) : AdvancedIntent()
    data class SetMeshDensity(val density: MeshDensity) : AdvancedIntent()
    data class LoadRigPreset(val presetId: String) : AdvancedIntent()
    data class SaveMeshPose(val name: String) : AdvancedIntent()
    data class LoadMeshPose(val poseId: String) : AdvancedIntent()
    data class DeleteMeshPose(val poseId: String) : AdvancedIntent()
    data class SetPinInfluenceRadius(val pinId: String, val radius: Float) : AdvancedIntent()
    data class SetBoneIK(val boneId: String, val enabled: Boolean) : AdvancedIntent()
    data class ApplyRigPresetToMesh(val presetId: String) : AdvancedIntent()
    object ConfirmMesh : AdvancedIntent()
    object CancelMesh : AdvancedIntent()
    object ClearMesh : AdvancedIntent()

    // ─── Google Sign-In ─────────────────────────────────────────────────

    object RequestGoogleSignIn : AdvancedIntent()
    object SignOutGoogle : AdvancedIntent()
    data class GoogleSignInResult(
        val accountId: String,
        val displayName: String,
        val email: String,
        val photoUrl: String
    ) : AdvancedIntent()
    data class SetYouTubeConnected(val isConnected: Boolean) : AdvancedIntent()

    // ─── YouTube Sharing (Animations Only) ──────────────────────────────

    data class ShareToYouTube(val config: YouTubeShareConfig) : AdvancedIntent()
    data class SetYouTubeTitle(val title: String) : AdvancedIntent()
    data class SetYouTubeDescription(val description: String) : AdvancedIntent()
    data class SetYouTubePrivacy(val privacy: YouTubePrivacy) : AdvancedIntent()
    data class SetYouTubeResolution(val resolution: YouTubeResolution) : AdvancedIntent()
    data class SetYouTubeTags(val tags: List<String>) : AdvancedIntent()
    object PrepareAnimationForYouTube : AdvancedIntent()
    object CancelYouTubeShare : AdvancedIntent()

    // ─── Screen Overlay Permission ──────────────────────────────────────

    object RequestOverlayPermission : AdvancedIntent()
    data class SetOverlayMode(val mode: OverlayMode) : AdvancedIntent()
    data class SetOverlayConfig(val config: OverlayConfig) : AdvancedIntent()
    object ToggleOverlay : AdvancedIntent()

    // ─── Crash Reporting (User-Owned) ──────────────────────────────────

    data class StoreCrashReport(val report: CrashReport) : AdvancedIntent()
    data class DeleteCrashReport(val reportId: String) : AdvancedIntent()
    object ClearAllCrashReports : AdvancedIntent()
    data class AcceptCrashDataOwnership(val accepted: Boolean) : AdvancedIntent()
    data class SetCrashReportConfig(val config: CrashReportConfig) : AdvancedIntent()
    data class PostCrashToGithub(val reportId: String) : AdvancedIntent()
    data class EmailCrashReport(val reportId: String) : AdvancedIntent()
    data class EditCrashReportNotes(val reportId: String, val notes: String) : AdvancedIntent()
    data class EditCrashReportSteps(val reportId: String, val steps: String) : AdvancedIntent()
    data class SetGitHubCredentials(val username: String, val repoName: String, val token: String) : AdvancedIntent()

    // ─── Artistso.com Integration ──────────────────────────────────────

    object LoadArtistsoContent : AdvancedIntent()
    data class SetArtistsoConfig(val config: ArtistsoConfig) : AdvancedIntent()
    data class OpenArtistsoTutorial(val contentId: String) : AdvancedIntent()
    data class OpenArtistsoDemo(val contentId: String) : AdvancedIntent()
    object OpenArtistsoInBrowser : AdvancedIntent()
    object RefreshArtistsoFeed : AdvancedIntent()
    data class SearchArtistsoContent(val query: String) : AdvancedIntent()
}

/** Targets for numeric input */
enum class NumericInputTarget {
    BRUSH_SIZE,
    BRUSH_OPACITY,
    BRUSH_SMOOTHING,
    CANVAS_WIDTH,
    CANVAS_HEIGHT,
    CANVAS_DPI,
    LAYER_OPACITY,
    ZOOM_LEVEL,
    ROTATION_ANGLE,
    SELECTION_X,
    SELECTION_Y,
    SELECTION_WIDTH,
    SELECTION_HEIGHT,
    FRAME_DURATION,
    EXPORT_SCALE,
    CUSTOM
}
