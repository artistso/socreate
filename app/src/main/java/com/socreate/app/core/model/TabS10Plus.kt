package com.socreate.app.core.model

/**
 * Samsung Galaxy Tab S10+ hardware profile.
 *
 * All rendering, memory, and UX decisions are tuned for this device.
 *
 * Display:  12.4" Dynamic AMOLED 2X
 * Res:      2800 × 1752 px  (16:10)
 * Density:  266 ppi  (~2.0x mdpi → density bucket 320dpi = xhdpi)
 * Refresh:  120 Hz
 * Gamut:    Display P3
 * SoC:      MediaTek Dimensity 9300+
 * RAM:      12 GB
 * GPU:      Immortalis-G720 MC12
 * Stylus:   S Pen (Wacom EMR) — 4096 levels pressure, tilt, palm reject
 * Battery:  10,090 mAh
 * OS:       Android 14 / One UI 6.1.1
 */
object TabS10Plus {

    // ─── Display ────────────────────────────────────────────────────────────

    const val SCREEN_WIDTH  = 2800
    const val SCREEN_HEIGHT = 1752
    const val ASPECT_RATIO_W = 16f
    const val ASPECT_RATIO_H = 10f
    const val DPI = 266
    const val REFRESH_RATE_HZ = 120
    const val DENSITY_BUCKET = 320  // xhdpi

    /** Frame budget at 120 Hz */
    const val FRAME_BUDGET_MS = 8L  // ~8.33ms per frame

    /** AMOLED supports wide color gamut */
    val COLOR_SPACE = ColorSpace.DISPLAY_P3

    // ─── S Pen (Wacom EMR) ──────────────────────────────────────────────────

    const val PRESSURE_LEVELS = 4096
    const val PRESSURE_RESOLUTION = 1f / PRESSURE_LEVELS

    /** S Pen Creator Edition tip width in mm */
    const val SPEN_TIP_WIDTH_MM = 9f

    /** Tilt range in degrees the S Pen can detect */
    const val TILT_RANGE_DEGREES = 90f

    // ─── Memory & Performance ───────────────────────────────────────────────

    const val TOTAL_RAM_MB = 12288L
    /** Conservative max for layer bitmaps (leaves room for OS + other apps) */
    const val BITMAP_BUDGET_MB = 3072L  // 3 GB
    const val NATIVE_RENDER_THREADS = 4

    // ─── Canvas Presets for Tab S10+ ────────────────────────────────────────

    /**
     * Native screen resolution canvas.
     * Every pixel maps 1:1 to the display — no scaling artifacts.
     */
    val NATIVE = Canvas(
        width = SCREEN_WIDTH,
        height = SCREEN_HEIGHT,
        dpi = DPI,
        backgroundColor = SoCreateColor.WHITE,
        colorProfile = ColorProfile.DISPLAY_P3,
        orientation = CanvasOrientation.LANDSCAPE
    )

    /**
     * Retina-quality 4K canvas.
     * Ideal for art prints and high-resolution export.
     */
    val ART_4K = Canvas(
        width = 4096,
        height = 2560,   // 16:10
        dpi = 300,
        colorProfile = ColorProfile.DISPLAY_P3,
        orientation = CanvasOrientation.LANDSCAPE
    )

    /**
     * Ultra-high 8K for professional print work.
     * Requires careful memory management with layers.
     */
    val ART_8K = Canvas(
        width = 8192,
        height = 5120,
        dpi = 300,
        colorProfile = ColorProfile.DISPLAY_P3,
        orientation = CanvasOrientation.LANDSCAPE
    )

    /**
     * Social media optimized (Instagram / TikTok).
     */
    val INSTAGRAM_SQUARE = Canvas(
        width = 1080,
        height = 1080,
        dpi = 72,
        orientation = CanvasOrientation.SQUARE
    )

    /**
     * Instagram Stories / TikTok / YouTube Shorts.
     */
    val VERTICAL_VIDEO = Canvas(
        width = 1080,
        height = 1920,
        dpi = 72,
        orientation = CanvasOrientation.PORTRAIT
    )

    /**
     * YouTube / widescreen video export.
     */
    val YOUTUBE_4K = Canvas(
        width = 3840,
        height = 2160,
        dpi = 72,
        orientation = CanvasOrientation.LANDSCAPE
    )

    /**
     * A4 paper at 300 DPI — professional print.
     */
    val A4_PRINT = Canvas(
        width = 3508,
        height = 2480,  // Landscape A4
        dpi = 300,
        colorProfile = ColorProfile.ADOBE_RGB,
        orientation = CanvasOrientation.LANDSCAPE
    )

    /**
     * Tab S10+ native in portrait mode.
     * Matches the device in portrait orientation.
     */
    val NATIVE_PORTRAIT = Canvas(
        width = SCREEN_HEIGHT,
        height = SCREEN_WIDTH,
        dpi = DPI,
        colorProfile = ColorProfile.DISPLAY_P3,
        orientation = CanvasOrientation.PORTRAIT
    )

    /**
     * All available canvas presets, named for the UI.
     */
    val PRESETS: List<Pair<String, Canvas>> = listOf(
        "Tab S10+ Native (2800×1752)" to NATIVE,
        "Tab S10+ Portrait (1752×2800)" to NATIVE_PORTRAIT,
        "Art 4K (4096×2560)" to ART_4K,
        "Art 8K (8192×5120)" to ART_8K,
        "YouTube 4K (3840×2160)" to YOUTUBE_4K,
        "A4 Print 300 DPI" to A4_PRINT,
        "Instagram Square" to INSTAGRAM_SQUARE,
        "Vertical Video (1080×1920)" to VERTICAL_VIDEO,
    )

    // ─── Layer Budget Calculations ──────────────────────────────────────────

    /**
     * Calculate the maximum number of full-resolution ARGB_8888 layers
     * that fit within our bitmap budget for a given canvas size.
     */
    fun maxLayers(canvas: Canvas): Int {
        val bytesPerPixel = 4L // ARGB_8888
        val layerSizeMB = (canvas.width.toLong() * canvas.height * bytesPerPixel) / (1024 * 1024)
        return (BITMAP_BUDGET_MB / layerSizeMB).toInt().coerceIn(1, 200)
    }

    /**
     * Check if a canvas + layer count combination fits in memory.
     */
    fun fitsInMemory(canvas: Canvas, layers: Int): Boolean {
        val bytesPerPixel = 4L
        val totalMB = (canvas.width.toLong() * canvas.height * bytesPerPixel * layers) / (1024 * 1024)
        return totalMB <= BITMAP_BUDGET_MB
    }

    // ─── AMOLED-Specific Rendering Hints ────────────────────────────────────

    /** AMOLED screens have true black — #000000 uses no backlight. */
    const val AMOLED_BLACK = 0xFF000000L.toInt()

    /** Anti-glare coating on Tab S10+ slightly diffuses sharp edges.
     120Hz refresh compensates — brush preview should favor softness. */
    const val ANTI_GLARE_DIFFUSION = 0.02f
}
