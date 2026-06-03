package com.socreate.app.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DrawingReducerTest {

    private val reducer = DrawingReducer

    @Test
    fun `initial state has default values`() {
        val state = DrawingState()

        assertThat(state.zoom).isEqualTo(1f)
        assertThat(state.panX).isEqualTo(0f)
        assertThat(state.panY).isEqualTo(0f)
        assertThat(state.activeTool).isEqualTo(DrawingTool.BRUSH)
        assertThat(state.isDrawing).isFalse()
        assertThat(state.canUndo).isFalse()
        assertThat(state.canRedo).isFalse()
    }

    @Test
    fun `StrokeStarted sets isDrawing and creates currentStroke`() {
        val initialState = DrawingState()
        val point = StrokePoint.simple(100f, 200f)

        val result = reducer.reduce(
            initialState,
            DrawingIntent.StrokeStarted(point, 100f, 200f)
        )

        assertThat(result.newState.isDrawing).isTrue()
        assertThat(result.newState.currentStroke).isNotNull()
        assertThat(result.newState.currentStroke?.points).hasSize(1)
    }

    @Test
    fun `StrokeEnded clears currentStroke and sets isDrawing false`() {
        val point = StrokePoint.simple(100f, 200f)
        val state = DrawingState(
            currentStroke = Stroke().addPoint(point),
            isDrawing = true
        )

        val result = reducer.reduce(state, DrawingIntent.StrokeEnded(null))

        assertThat(result.newState.isDrawing).isFalse()
        assertThat(result.newState.currentStroke).isNull()
        assertThat(result.newState.canUndo).isTrue()
    }

    @Test
    fun `StrokeCancelled resets drawing state`() {
        val state = DrawingState(
            currentStroke = Stroke().addPoint(StrokePoint.simple(100f, 200f)),
            isDrawing = true
        )

        val result = reducer.reduce(state, DrawingIntent.StrokeCancelled)

        assertThat(result.newState.isDrawing).isFalse()
        assertThat(result.newState.currentStroke).isNull()
    }

    @Test
    fun `SelectTool changes active tool`() {
        val initialState = DrawingState()

        val result = reducer.reduce(
            initialState,
            DrawingIntent.SelectTool(DrawingTool.ERASER)
        )

        assertThat(result.newState.activeTool).isEqualTo(DrawingTool.ERASER)
    }

    @Test
    fun `SetBrushSize clamps to valid range`() {
        val initialState = DrawingState()

        val tooSmall = reducer.reduce(initialState, DrawingIntent.SetBrushSize(-10f))
        assertThat(tooSmall.newState.brushSize).isEqualTo(1f)

        val tooLarge = reducer.reduce(initialState, DrawingIntent.SetBrushSize(1000f))
        assertThat(tooLarge.newState.brushSize).isEqualTo(500f)

        val valid = reducer.reduce(initialState, DrawingIntent.SetBrushSize(50f))
        assertThat(valid.newState.brushSize).isEqualTo(50f)
    }

    @Test
    fun `SetBrushOpacity clamps to valid range`() {
        val initialState = DrawingState()

        val tooLow = reducer.reduce(initialState, DrawingIntent.SetBrushOpacity(0f))
        assertThat(tooLow.newState.brushOpacity).isEqualTo(0.01f)

        val tooHigh = reducer.reduce(initialState, DrawingIntent.SetBrushOpacity(2f))
        assertThat(tooHigh.newState.brushOpacity).isEqualTo(1f)
    }

    @Test
    fun `SetColor updates active color`() {
        val initialState = DrawingState()
        val red = SoCreateColor.RED

        val result = reducer.reduce(initialState, DrawingIntent.SetColor(red))

        assertThat(result.newState.activeColor).isEqualTo(red)
    }

    @Test
    fun `AddLayer adds new layer and selects it`() {
        val initialState = DrawingState()
        assertThat(initialState.layerStack.layers).hasSize(1)

        val result = reducer.reduce(initialState, DrawingIntent.AddLayer)

        assertThat(result.newState.layerStack.layers).hasSize(2)
        assertThat(result.newState.layerStack.activeLayerId).isNotNull()
    }

    @Test
    fun `DeleteLayer removes layer but prevents deleting last one`() {
        val stateWithTwoLayers = DrawingState(
            layerStack = LayerStack.createDefault().let { stack ->
                val layer2 = Layer(name = "Layer 2", sortOrder = 1)
                stack.addLayer(layer2)
            }
        )

        // Can delete when more than one
        val afterDelete = reducer.reduce(
            stateWithTwoLayers,
            DrawingIntent.DeleteLayer(stateWithTwoLayers.layerStack.layers.last().id)
        )
        assertThat(afterDelete.newState.layerStack.layers).hasSize(1)

        // Cannot delete the last layer
        val result = reducer.reduce(
            afterDelete.newState,
            DrawingIntent.DeleteLayer(afterDelete.newState.layerStack.layers.first().id)
        )
        assertThat(result.newState.layerStack.layers).hasSize(1)
        val toastEffect = result.effects.firstOrNull { it is DrawingEffect.ShowToast }
        assertThat(toastEffect).isNotNull()
    }

    @Test
    fun `Zoom clamps to min and max values`() {
        val initialState = DrawingState(zoom = 1f)

        val zoomedOut = reducer.reduce(initialState, DrawingIntent.Zoom(0.01f, 0f, 0f))
        assertThat(zoomedOut.newState.zoom).isAtLeast(0.1f)

        val zoomedIn = reducer.reduce(DrawingState(zoom = 100f), DrawingIntent.Zoom(10f, 0f, 0f))
        assertThat(zoomedIn.newState.zoom).isAtMost(64f)
    }

    @Test
    fun `Pan updates pan position`() {
        val initialState = DrawingState(panX = 100f, panY = 100f)

        val result = reducer.reduce(initialState, DrawingIntent.Pan(50f, -25f))

        assertThat(result.newState.panX).isEqualTo(150f)
        assertThat(result.newState.panY).isEqualTo(75f)
    }

    @Test
    fun `ToggleBrushPicker toggles visibility and closes others`() {
        val state = DrawingState(
            showBrushPicker = false,
            showColorPicker = true,
            showLayerPanel = true
        )

        val result = reducer.reduce(state, DrawingIntent.ToggleBrushPicker)

        assertThat(result.newState.showBrushPicker).isTrue()
        assertThat(result.newState.showColorPicker).isFalse()
        assertThat(result.newState.showLayerPanel).isFalse()
    }

    @Test
    fun `Undo sets canRedo to true`() {
        val state = DrawingState(
            canUndo = true,
            canRedo = false,
            undoCount = 5,
            redoCount = 0
        )

        val result = reducer.reduce(state, DrawingIntent.Undo)

        assertThat(result.newState.canRedo).isTrue()
        assertThat(result.newState.undoCount).isEqualTo(4)
        assertThat(result.newState.redoCount).isEqualTo(1)
    }

    @Test
    fun `screenToCanvas converts coordinates correctly`() {
        val state = DrawingState(
            zoom = 2f,
            panX = 100f,
            panY = 50f
        )

        val (cx, cy) = state.screenToCanvas(200f, 100f)

        // cx = (200 - 100) / 2 = 50
        // cy = (100 - 50) / 2 = 25
        assertThat(cx).isEqualTo(50f)
        assertThat(cy).isEqualTo(25f)
    }

    @Test
    fun `recent colors are updated after stroke ends`() {
        val state = DrawingState(
            currentStroke = Stroke(color = SoCreateColor.RED)
                .addPoint(StrokePoint.simple(0f, 0f)),
            isDrawing = true,
            recentColors = listOf(SoCreateColor.BLACK)
        )

        val result = reducer.reduce(state, DrawingIntent.StrokeEnded(null))

        assertThat(result.newState.recentColors).hasSize(2)
        assertThat(result.newState.recentColors.first()).isEqualTo(SoCreateColor.RED)
    }

    @Test
    fun `recent colors removes duplicates and caps at 30`() {
        val manyColors = (1..30).map {
            SoCreateColor(it.toFloat() / 30f, 0f, 0f)
        }
        val state = DrawingState(
            currentStroke = Stroke(color = SoCreateColor.RED)
                .addPoint(StrokePoint.simple(0f, 0f)),
            recentColors = manyColors
        )

        val result = reducer.reduce(state, DrawingIntent.StrokeEnded(null))

        assertThat(result.newState.recentColors).hasSize(30)
        assertThat(result.newState.recentColors.first()).isEqualTo(SoCreateColor.RED)
    }
}
