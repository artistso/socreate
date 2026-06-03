package com.socreate.app.engine.renderer

/**
 * Bridge to the native C++ rendering engine.
 * Provides GPU-accelerated tile-based rendering, compositing, and filter processing.
 *
 * This is a placeholder for future native rendering integration.
 * Currently, all rendering uses the Android Canvas API via StrokeRenderer.
 */
class NativeRenderer {

    private var nativeHandle: Long = 0

    init {
        try {
            System.loadLibrary("socreate-native")
            nativeHandle = nativeInit()
        } catch (e: UnsatisfiedLinkError) {
            // Native library not available, fall back to Canvas rendering
        }
    }

    val isAvailable: Boolean get() = nativeHandle != 0L

    fun renderTile(
        textureId: Int,
        opacity: Float,
        blendMode: Int,
        x: Float, y: Float,
        width: Float, height: Float
    ) {
        if (nativeHandle != 0L) {
            nativeRenderTile(nativeHandle, textureId, opacity, blendMode, x, y, width, height)
        }
    }

    fun applyFilter(
        inputTexture: Int,
        outputTexture: Int,
        filterType: Int,
        intensity: Float
    ) {
        if (nativeHandle != 0L) {
            nativeApplyFilter(nativeHandle, inputTexture, outputTexture, filterType, intensity)
        }
    }

    fun release() {
        if (nativeHandle != 0L) {
            nativeRelease(nativeHandle)
            nativeHandle = 0
        }
    }

    // Native method declarations
    private external fun nativeInit(): Long
    private external fun nativeRenderTile(
        handle: Long, textureId: Int, opacity: Float, blendMode: Int,
        x: Float, y: Float, width: Float, height: Float
    )
    private external fun nativeApplyFilter(
        handle: Long, inputTexture: Int, outputTexture: Int,
        filterType: Int, intensity: Float
    )
    private external fun nativeRelease(handle: Long)
}
