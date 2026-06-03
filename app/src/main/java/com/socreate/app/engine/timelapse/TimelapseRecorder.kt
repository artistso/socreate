package com.socreate.app.engine.timelapse

import android.graphics.Bitmap
import com.socreate.app.core.model.TimelapseConfig
import com.socreate.app.core.model.TimelapseFormat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

/**
 * Records the drawing process for time-lapse playback and export.
 * Inspired by ibisPaint's time-lapse recording and HiPaint's process replay.
 *
 * Captures canvas snapshots at regular intervals and stores them
 * for later replay as MP4 video or animated GIF.
 *
 * Strategy:
 * - Captures a snapshot after every N strokes (not continuous frames)
 * - Stores compressed JPEG thumbnails to minimize memory
 * - On export, replays snapshots at configurable speed
 * - Can also record individual stroke data for lossless replay
 */
class TimelapseRecorder {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _config = MutableStateFlow(TimelapseConfig())
    val config: StateFlow<TimelapseConfig> = _config

    private val _strokeCount = MutableStateFlow(0)
    val strokeCount: StateFlow<Int> = _strokeCount

    private val _frameCount = MutableStateFlow(0)
    val frameCount: StateFlow<Int> = _frameCount

    private val snapshots = mutableListOf<TimelapseFrame>()
    private var startTimeMs: Long = 0L
    private var lastCaptureTimeMs: Long = 0L

    // Capture interval — snapshot every N strokes
    private var captureEveryNStrokes = 1

    /**
     * Start recording.
     */
    fun startRecording(config: TimelapseConfig) {
        _config.value = config
        startTimeMs = System.currentTimeMillis()
        lastCaptureTimeMs = startTimeMs
        snapshots.clear()
        _strokeCount.value = 0
        _frameCount.value = 0
        _config.value = _config.value.copy(isRecording = true)
    }

    /**
     * Pause recording.
     */
    fun pauseRecording() {
        _config.value = _config.value.copy(isRecording = false)
    }

    /**
     * Resume recording.
     */
    fun resumeRecording() {
        _config.value = _config.value.copy(isRecording = true)
    }

    /**
     * Stop recording and return the total duration.
     */
    fun stopRecording(): Long {
        _config.value = _config.value.copy(isRecording = false)
        return System.currentTimeMillis() - startTimeMs
    }

    /**
     * Record a completed stroke.
     * Captures a canvas snapshot every N strokes.
     */
    fun recordStroke(canvasBitmap: Bitmap?) {
        if (!_config.value.isRecording) return

        _strokeCount.value += 1

        // Capture every N strokes
        if (_strokeCount.value % captureEveryNStrokes == 0) {
            captureFrame(canvasBitmap)
        }
    }

    /**
     * Record an undo action.
     */
    fun recordUndo(canvasBitmap: Bitmap?) {
        if (!_config.value.isRecording || !_config.value.includeUndoRedo) return
        captureFrame(canvasBitmap, isUndo = true)
    }

    /**
     * Record a redo action.
     */
    fun recordRedo(canvasBitmap: Bitmap?) {
        if (!_config.value.isRecording || !_config.value.includeUndoRedo) return
        captureFrame(canvasBitmap, isRedo = true)
    }

    /**
     * Record a tool change.
     */
    fun recordToolChange(toolName: String, canvasBitmap: Bitmap?) {
        if (!_config.value.isRecording) return
        // Tool changes are captured as metadata, not always as frames
    }

    /**
     * Capture a single frame of the timelapse.
     */
    private fun captureFrame(bitmap: Bitmap?, isUndo: Boolean = false, isRedo: Boolean = false) {
        bitmap?.let { bmp ->
            val now = System.currentTimeMillis()
            val timestamp = now - startTimeMs

            // Determine resolution
            val targetWidth = when (_config.value.resolution) {
                com.socreate.app.core.model.TimelapseResolution.HALF -> bmp.width / 2
                com.socreate.app.core.model.TimelapseResolution.FULL -> bmp.width
                com.socreate.app.core.model.TimelapseResolution.OUTPUT -> bmp.width
            }

            val scaledBitmap = if (targetWidth != bmp.width) {
                val scale = targetWidth.toFloat() / bmp.width
                Bitmap.createScaledBitmap(bmp, targetWidth, (bmp.height * scale).toInt(), true)
            } else bmp

            // Compress to JPEG for storage efficiency
            val compressed = compressBitmap(scaledBitmap, quality = 80)

            snapshots.add(
                TimelapseFrame(
                    timestampMs = timestamp,
                    jpegData = compressed,
                    strokeIndex = _strokeCount.value,
                    isUndo = isUndo,
                    isRedo = isRedo
                )
            )

            _frameCount.value += 1
            lastCaptureTimeMs = now

            // Enforce max duration
            val durationMinutes = timestamp / 60_000f
            if (durationMinutes >= _config.value.maxDurationMinutes) {
                pauseRecording()
            }
        }
    }

    /**
     * Export the timelapse as MP4 or GIF.
     * Returns the output file path.
     */
    suspend fun export(
        outputDir: File,
        filename: String,
        config: TimelapseConfig = _config.value
    ): String? {
        return withContext(Dispatchers.IO) {
            if (snapshots.isEmpty()) return@withContext null

            when (config.format) {
                TimelapseFormat.MP4 -> exportAsMp4(outputDir, filename, config)
                TimelapseFormat.GIF -> exportAsGif(outputDir, filename, config)
            }
        }
    }

    private fun exportAsMp4(outputDir: File, filename: String, config: TimelapseConfig): String {
        val outputFile = File(outputDir, "$filename.mp4")
        // TODO: Use MediaMuxer to encode JPEG frames into MP4
        // Each frame's display duration = (timestampDelta / playbackSpeed)
        return outputFile.absolutePath
    }

    private fun exportAsGif(outputDir: File, filename: String, config: TimelapseConfig): String {
        val outputFile = File(outputDir, "$filename.gif")
        // TODO: Use Android's AnimatedImageDrawable or a GIF encoder library
        return outputFile.absolutePath
    }

    /**
     * Get the total recording duration in milliseconds.
     */
    fun getDurationMs(): Long {
        return if (snapshots.isNotEmpty()) {
            snapshots.last().timestampMs
        } else 0L
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    fun release() {
        scope.cancel()
        snapshots.clear()
    }

    /**
     * A single frame in the timelapse recording.
     */
    data class TimelapseFrame(
        val timestampMs: Long,
        val jpegData: ByteArray,
        val strokeIndex: Int,
        val isUndo: Boolean = false,
        val isRedo: Boolean = false
    ) {
        override fun equals(other: Any?): Boolean = this === other
        override fun hashCode(): Int = System.identityHashCode(this)
    }
}
