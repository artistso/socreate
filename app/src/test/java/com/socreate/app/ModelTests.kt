package com.socreate.app.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ColorModelTest {

    @Test
    fun `fromHSB creates correct RGB values for red`() {
        val color = SoCreateColor.fromHSB(0f, 1f, 1f)
        assertThat(color.red).isWithin(0.01f).of(1f)
        assertThat(color.green).isWithin(0.01f).of(0f)
        assertThat(color.blue).isWithin(0.01f).of(0f)
    }

    @Test
    fun `fromHSB creates correct RGB values for green`() {
        val color = SoCreateColor.fromHSB(120f, 1f, 1f)
        assertThat(color.red).isWithin(0.01f).of(0f)
        assertThat(color.green).isWithin(0.01f).of(1f)
        assertThat(color.blue).isWithin(0.01f).of(0f)
    }

    @Test
    fun `fromArgb converts correctly`() {
        val color = SoCreateColor.fromArgb(0xFFFF0000.toInt())
        assertThat(color.red).isWithin(0.01f).of(1f)
        assertThat(color.green).isWithin(0.01f).of(0f)
        assertThat(color.blue).isWithin(0.01f).of(0f)
        assertThat(color.alpha).isWithin(0.01f).of(1f)
    }

    @Test
    fun `toArgb round-trips correctly`() {
        val original = SoCreateColor(0.5f, 0.3f, 0.8f, 0.9f)
        val argb = original.toArgb()
        val restored = SoCreateColor.fromArgb(argb)

        assertThat(restored.red).isWithin(0.01f).of(original.red)
        assertThat(restored.green).isWithin(0.01f).of(original.green)
        assertThat(restored.blue).isWithin(0.01f).of(original.blue)
        assertThat(restored.alpha).isWithin(0.01f).of(original.alpha)
    }

    @Test
    fun `withAlpha creates new color with updated alpha`() {
        val original = SoCreateColor.RED
        val withHalfAlpha = original.withAlpha(0.5f)

        assertThat(withHalfAlpha.red).isEqualTo(original.red)
        assertThat(withHalfAlpha.alpha).isEqualTo(0.5f)
        assertThat(original.alpha).isEqualTo(1f) // Original unchanged
    }
}

class PressureCurveTest {

    @Test
    fun `linear curve evaluates correctly`() {
        val curve = PressureCurve(
            points = listOf(
                ControlPoint(0f, 0f),
                ControlPoint(1f, 1f)
            )
        )

        assertThat(curve.evaluate(0f)).isWithin(0.001f).of(0f)
        assertThat(curve.evaluate(0.5f)).isWithin(0.001f).of(0.5f)
        assertThat(curve.evaluate(1f)).isWithin(0.001f).of(1f)
    }

    @Test
    fun `custom curve evaluates correctly`() {
        val curve = PressureCurve(
            points = listOf(
                ControlPoint(0f, 0.1f),
                ControlPoint(0.5f, 0.8f),
                ControlPoint(1f, 1f)
            )
        )

        assertThat(curve.evaluate(0f)).isWithin(0.001f).of(0.1f)
        assertThat(curve.evaluate(1f)).isWithin(0.001f).of(1f)
        // Mid point should be 0.8
        assertThat(curve.evaluate(0.5f)).isWithin(0.001f).of(0.8f)
    }

    @Test
    fun `curve clamps out-of-range values`() {
        val curve = PressureCurve(
            points = listOf(
                ControlPoint(0f, 0.2f),
                ControlPoint(1f, 0.9f)
            )
        )

        assertThat(curve.evaluate(-1f)).isWithin(0.001f).of(0.2f)
        assertThat(curve.evaluate(2f)).isWithin(0.001f).of(0.9f)
    }
}

class LayerStackTest {

    @Test
    fun `createDefault has one layer`() {
        val stack = LayerStack.createDefault()

        assertThat(stack.layers).hasSize(1)
        assertThat(stack.activeLayerId).isNotNull()
        assertThat(stack.activeLayer?.name).isEqualTo("Layer 1")
    }

    @Test
    fun `addLayer adds and selects new layer`() {
        val stack = LayerStack.createDefault()
        val newLayer = Layer(name = "Layer 2", sortOrder = 1)

        val result = stack.addLayer(newLayer)

        assertThat(result.layers).hasSize(2)
        assertThat(result.activeLayerId).isEqualTo(newLayer.id)
    }

    @Test
    fun `removeLayer deletes layer and selects adjacent`() {
        val layer1 = Layer(id = "1", name = "Layer 1", sortOrder = 0)
        val layer2 = Layer(id = "2", name = "Layer 2", sortOrder = 1)
        val stack = LayerStack(layers = listOf(layer1, layer2), activeLayerId = "2")

        val result = stack.removeLayer("2")

        assertThat(result.layers).hasSize(1)
        assertThat(result.activeLayerId).isEqualTo("1")
    }

    @Test
    fun `updateLayer modifies specific layer`() {
        val layer1 = Layer(id = "1", name = "Layer 1", opacity = 1f)
        val layer2 = Layer(id = "2", name = "Layer 2", opacity = 1f)
        val stack = LayerStack(layers = listOf(layer1, layer2), activeLayerId = "1")

        val result = stack.updateLayer("2") { it.withOpacity(0.5f) }

        assertThat(result.layers[0].opacity).isEqualTo(1f) // Unchanged
        assertThat(result.layers[1].opacity).isEqualTo(0.5f) // Updated
    }

    @Test
    fun `moveLayer reorders layers`() {
        val layer1 = Layer(id = "1", name = "Bottom", sortOrder = 0)
        val layer2 = Layer(id = "2", name = "Middle", sortOrder = 1)
        val layer3 = Layer(id = "3", name = "Top", sortOrder = 2)
        val stack = LayerStack(layers = listOf(layer1, layer2, layer3))

        val result = stack.moveLayer(2, 0) // Move Top to bottom

        assertThat(result.layers[0].id).isEqualTo("3")
        assertThat(result.layers[1].id).isEqualTo("1")
        assertThat(result.layers[2].id).isEqualTo("2")
    }

    @Test
    fun `mergeDown combines two layers`() {
        val layer1 = Layer(id = "1", name = "Bottom", sortOrder = 0)
        val layer2 = Layer(id = "2", name = "Top", sortOrder = 1)
        val stack = LayerStack(layers = listOf(layer1, layer2), activeLayerId = "2")

        val result = stack.mergeDown("2")

        assertThat(result.layers).hasSize(1)
        assertThat(result.activeLayerId).isEqualTo("1")
    }
}

class StrokeTest {

    @Test
    fun `empty stroke has zero bounds`() {
        val stroke = Stroke()
        assertThat(stroke.isEmpty).isTrue()
        assertThat(stroke.bounds.isEmpty).isTrue()
    }

    @Test
    fun `single point stroke has correct bounds`() {
        val stroke = Stroke(
            points = listOf(StrokePoint.simple(100f, 200f)),
            brushProperties = BrushProperties(baseSize = 10f)
        )

        assertThat(stroke.bounds.centerX).isWithin(1f).of(100f)
        assertThat(stroke.bounds.centerY).isWithin(1f).of(200f)
    }

    @Test
    fun `bounds enclose all points`() {
        val stroke = Stroke(
            points = listOf(
                StrokePoint.simple(10f, 20f),
                StrokePoint.simple(100f, 200f),
                StrokePoint.simple(50f, 100f)
            ),
            brushProperties = BrushProperties(baseSize = 0f)
        )

        assertThat(stroke.bounds.left).isWithin(1f).of(10f)
        assertThat(stroke.bounds.top).isWithin(1f).of(20f)
        assertThat(stroke.bounds.right).isWithin(1f).of(100f)
        assertThat(stroke.bounds.bottom).isWithin(1f).of(200f)
    }
}

class BoundsTest {

    @Test
    fun `intersection detection works`() {
        val a = Bounds(0f, 0f, 100f, 100f)
        val b = Bounds(50f, 50f, 100f, 100f)
        val c = Bounds(200f, 200f, 100f, 100f)

        assertThat(a.intersects(b)).isTrue()
        assertThat(a.intersects(c)).isFalse()
    }

    @Test
    fun `contains point works`() {
        val bounds = Bounds(10f, 10f, 50f, 50f)

        assertThat(bounds.contains(30f, 30f)).isTrue()
        assertThat(bounds.contains(5f, 5f)).isFalse()
    }

    @Test
    fun `union combines bounds`() {
        val a = Bounds(0f, 0f, 50f, 50f)
        val b = Bounds(100f, 100f, 50f, 50f)
        val union = a.union(b)

        assertThat(union.left).isEqualTo(0f)
        assertThat(union.top).isEqualTo(0f)
        assertThat(union.right).isEqualTo(150f)
        assertThat(union.bottom).isEqualTo(150f)
    }

    @Test
    fun `inflate expands bounds`() {
        val bounds = Bounds(10f, 10f, 50f, 50f)
        val inflated = bounds.inflate(5f)

        assertThat(inflated.left).isEqualTo(5f)
        assertThat(inflated.top).isEqualTo(5f)
        assertThat(inflated.width).isEqualTo(60f)
        assertThat(inflated.height).isEqualTo(60f)
    }
}

class BrushPresetsTest {

    @Test
    fun `all brushes have unique IDs`() {
        val brushes = com.socreate.app.engine.brush.BrushPresets.allBrushes
        val ids = brushes.map { it.id }
        assertThat(ids).hasSize(ids.toSet().size)
    }

    @Test
    fun `all brushes have valid properties`() {
        com.socreate.app.engine.brush.BrushPresets.allBrushes.forEach { brush ->
            assertThat(brush.name).isNotEmpty()
            assertThat(brush.properties.baseSize).isGreaterThan(0f)
            assertThat(brush.properties.minSize).isAtLeast(0f)
            assertThat(brush.properties.opacity).isAtMost(1f)
        }
    }
}
