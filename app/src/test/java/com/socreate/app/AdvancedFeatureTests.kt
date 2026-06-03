package com.socreate.app.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SymmetryConfigTest {

    @Test
    fun `default symmetry is disabled`() {
        val config = SymmetryConfig()
        assertThat(config.isEnabled).isFalse()
        assertThat(config.type).isEqualTo(SymmetryType.VERTICAL)
    }

    @Test
    fun `all symmetry types are available`() {
        assertThat(SymmetryType.values()).hasSize(7)
        assertThat(SymmetryType.MANDALA.displayName).isEqualTo("Mandala")
    }
}

class SmartShapeTest {

    private val detector = com.socreate.app.engine.renderer.SmartShapeDetector()

    @Test
    fun `too few points returns no shape`() {
        val stroke = Stroke(
            points = listOf(
                StrokePoint.simple(10f, 10f),
                StrokePoint.simple(20f, 20f)
            )
        )
        val result = detector.detectShape(stroke)
        assertThat(result).isNull()
    }

    @Test
    fun `straight line is detected`() {
        val points = (0..20).map { i ->
            StrokePoint.simple(i * 10f, 0f)
        }
        val stroke = Stroke(points = points)
        val result = detector.detectShape(stroke)

        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(com.socreate.app.core.model.SmartShapeType.LINE)
        assertThat(result.confidence).isGreaterThan(0.8f)
    }
}

class AutoSaveConfigTest {

    @Test
    fun `default auto-save is enabled with 30 second interval`() {
        val config = AutoSaveConfig()
        assertThat(config.isEnabled).isTrue()
        assertThat(config.intervalSeconds).isEqualTo(30)
        assertThat(config.maxBackups).isEqualTo(5)
    }
}

class ExtendedCanvasTest {

    @Test
    fun `extended canvas calculates padding correctly`() {
        val ec = ExtendedCanvas(
            visibleWidth = 2800,
            visibleHeight = 1752,
            extendedWidth = 5600,
            extendedHeight = 3504
        )

        assertThat(ec.horizontalPadding).isEqualTo(1400)
        assertThat(ec.verticalPadding).isEqualTo(876)
    }

    @Test
    fun `extended canvas shows bounds by default`() {
        val ec = ExtendedCanvas()
        assertThat(ec.showBounds).isTrue()
    }
}

class GestureConfigTest {

    @Test
    fun `default gestures match HiPaint conventions`() {
        val config = GestureConfig()
        assertThat(config.twoFingerUndo).isTrue()
        assertThat(config.threeFingerRedo).isTrue()
        assertThat(config.longPressEyedropper).isTrue()
        assertThat(config.longPressDurationMs).isEqualTo(300L)
        assertThat(config.disableFingerDrawing).isFalse()
    }
}

class AudioTrackTest {

    @Test
    fun `audio track calculates timing correctly`() {
        val track = AudioTrack(
            filePath = "/audio/test.wav",
            startFrame = 24,
            frameRate = 24,
            trimStartMs = 1000,
            trimEndMs = 5000
        )

        assertThat(track.startMs).isEqualTo(1000L)
        assertThat(track.durationMs).isEqualTo(4000L)
    }
}

class FillSettingsTest {

    @Test
    fun `default fill is standard flood fill`() {
        val settings = FillSettings()
        assertThat(settings.mode).isEqualTo(FillMode.FLOOD_FILL)
        assertThat(settings.tolerance).isWithin(0.01f).of(0.1f)
        assertThat(settings.antiAlias).isTrue()
    }

    @Test
    fun `all fill modes are available`() {
        assertThat(FillMode.values()).hasSize(5)
    }
}

class VelocitySettingsTest {

    @Test
    fun `velocity settings default to disabled`() {
        val settings = VelocitySettings()
        assertThat(settings.isEnabled).isFalse()
    }
}

class PuppetWarpTest {

    @Test
    fun `warp pins have correct properties`() {
        val pin = WarpPin(
            originalX = 100f,
            originalY = 200f,
            currentX = 150f,
            currentY = 250f,
            isFixed = false,
            stiffness = 0.8f
        )

        assertThat(pin.originalX).isEqualTo(100f)
        assertThat(pin.currentX).isEqualTo(150f)
        assertThat(pin.isFixed).isFalse()
    }
}

class BrushPatternTest {

    @Test
    fun `brush pattern modes are complete`() {
        assertThat(PatternShapeMode.values()).hasSize(4)
        assertThat(PatternGrainMode.values()).hasSize(4)
    }
}

class FloatingPanelTest {

    @Test
    fun `all panel types available`() {
        assertThat(FloatingPanelType.values()).hasSize(7)
    }
}

class DrawingStateAdvancedTest {

    @Test
    fun `state includes all advanced features`() {
        val state = DrawingState()

        // All advanced features should have defaults
        assertThat(state.symmetryConfig.isEnabled).isFalse()
        assertThat(state.referenceImages).isEmpty()
        assertThat(state.activeSmartShape).isNull()
        assertThat(state.activePuppetWarp).isNull()
        assertThat(state.isLiquifying).isFalse()
        assertThat(state.shadingAssist.isEnabled).isFalse()
        assertThat(state.velocitySettings.isEnabled).isFalse()
        assertThat(state.extendedCanvas).isNull()
        assertThat(state.gestureConfig.twoFingerUndo).isTrue()
        assertThat(state.autoSaveConfig.isEnabled).isTrue()
        assertThat(state.audioTracks).isEmpty()
        assertThat(state.showNumericInput).isFalse()
    }

    @Test
    fun `state with symmetry enabled`() {
        val state = DrawingState(
            symmetryConfig = SymmetryConfig(
                isEnabled = true,
                type = SymmetryType.MANDALA,
                rotationalCount = 8
            )
        )

        assertThat(state.symmetryConfig.isEnabled).isTrue()
        assertThat(state.symmetryConfig.type).isEqualTo(SymmetryType.MANDALA)
        assertThat(state.symmetryConfig.rotationalCount).isEqualTo(8)
    }

    @Test
    fun `state with extended canvas for FlipaClip-style oversize drawing`() {
        val state = DrawingState(
            extendedCanvas = ExtendedCanvas(
                visibleWidth = 2800,
                visibleHeight = 1752,
                extendedWidth = 5600,
                extendedHeight = 3504
            )
        )

        assertThat(state.extendedCanvas).isNotNull()
        assertThat(state.extendedCanvas!!.horizontalPadding).isEqualTo(1400)
    }
}
