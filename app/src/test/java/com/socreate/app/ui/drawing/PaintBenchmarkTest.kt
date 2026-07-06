package com.socreate.app.ui.drawing

import org.junit.Test
import kotlin.system.measureNanoTime
import androidx.compose.ui.graphics.Paint

class PaintBenchmarkTest {

    @Test
    fun benchmarkPaintCreation() {
        val iterations = 1000000

        // Warm up
        repeat(10000) {
            val p = Paint()
            p.alpha = 0.5f
        }

        val baselineTime = measureNanoTime {
            repeat(iterations) {
                val p = Paint().apply { alpha = 0.5f }
            }
        }

        // Warm up optimized
        val cachedPaint = Paint()
        repeat(10000) {
            cachedPaint.alpha = 0.5f
        }

        val optimizedTime = measureNanoTime {
            repeat(iterations) {
                cachedPaint.apply { alpha = 0.5f }
            }
        }

        println("=== BENCHMARK RESULTS ===")
        println("Baseline (object creation): \${baselineTime / 1_000_000} ms")
        println("Optimized (reused object): \${optimizedTime / 1_000_000} ms")
        val improvement = (baselineTime - optimizedTime).toDouble() / baselineTime * 100
        println(String.format("Improvement: %.2f%%", improvement))
    }
}
