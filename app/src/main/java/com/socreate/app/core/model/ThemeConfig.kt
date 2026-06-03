package com.socreate.app.core.model

import kotlinx.serialization.Serializable

/**
 * Color theme system for SoCreate.
 *
 * Provides multiple UI themes that change the app's visual appearance
 * including backgrounds, panels, toolbars, accent colors, and canvas chrome.
 * Inspired by HiPaint's custom themes & DIY workspace.
 *
 * Users can:
 * - Choose from built-in themes
 * - Create fully custom themes via the theme editor
 * - Share themes via JSON export/import
 * - Set different themes for drawing vs gallery views
 */
@Serializable
data class AppTheme(
    val id: String = "default_dark",
    val name: String = "SoCreate Dark",
    val isBuiltIn: Boolean = true,
    val colors: ThemeColors = ThemeColors(),
    val typography: ThemeTypography = ThemeTypography(),
    val shapes: ThemeShapes = ThemeShapes(),
    val isDarkMode: Boolean = true,
    val author: String = "Soquarky",
    val version: Int = 1
)

@Serializable
data class ThemeColors(
    // Primary accent (toolbar icons, sliders, selection highlights)
    val primary: SoCreateColor = SoCreateColor(0.914f, 0.271f, 0.376f),        // #E94560
    val primaryDark: SoCreateColor = SoCreateColor(0.722f, 0.188f, 0.290f),    // #B8304A
    val primaryLight: SoCreateColor = SoCreateColor(1f, 0.420f, 0.506f),       // #FF6B81

    // Accent (secondary highlights, floating panels)
    val accent: SoCreateColor = SoCreateColor(0.325f, 0.204f, 0.514f),         // #533483

    // Backgrounds
    val backgroundDark: SoCreateColor = SoCreateColor(0.059f, 0.204f, 0.376f),  // #0F3460
    val backgroundSurface: SoCreateColor = SoCreateColor(0.086f, 0.129f, 0.243f), // #16213E
    val backgroundCanvas: SoCreateColor = SoCreateColor(0.102f, 0.102f, 0.180f),  // #1A1A2E
    val backgroundPanel: SoCreateColor = SoCreateColor(0.086f, 0.129f, 0.243f, 0.91f),
    val backgroundToolbar: SoCreateColor = SoCreateColor(0.102f, 0.102f, 0.180f, 0.80f),

    // Text
    val textPrimary: SoCreateColor = SoCreateColor.WHITE,
    val textSecondary: SoCreateColor = SoCreateColor(0.69f, 0.69f, 0.69f),
    val textDisabled: SoCreateColor = SoCreateColor(0.4f, 0.4f, 0.4f),

    // Canvas chrome
    val canvasBorder: SoCreateColor = SoCreateColor(0.2f, 0.2f, 0.2f, 0.5f),
    val rulerColor: SoCreateColor = SoCreateColor(0.5f, 0.5f, 0.5f, 0.6f),
    val gridColor: SoCreateColor = SoCreateColor(0.5f, 0.5f, 0.5f, 0.2f),
    val selectionMarquee: SoCreateColor = SoCreateColor.WHITE,

    // Onion skin defaults
    val onionSkinBefore: SoCreateColor = SoCreateColor(1f, 0f, 0f, 0.3f),
    val onionSkinAfter: SoCreateColor = SoCreateColor(0f, 1f, 0f, 0.2f),

    // Modifier button colors (on-screen Ctrl/Shift/Alt)
    val modifierButtonBg: SoCreateColor = SoCreateColor(0.15f, 0.15f, 0.25f, 0.85f),
    val modifierButtonActiveBg: SoCreateColor = SoCreateColor(0.914f, 0.271f, 0.376f, 0.9f),
    val modifierButtonText: SoCreateColor = SoCreateColor.WHITE,
    val modifierButtonActiveText: SoCreateColor = SoCreateColor.WHITE,

    // Timeline
    val timelineBackground: SoCreateColor = SoCreateColor(0.08f, 0.08f, 0.14f),
    val timelineTrackBg: SoCreateColor = SoCreateColor(0.12f, 0.12f, 0.20f),
    val timelineFrameBg: SoCreateColor = SoCreateColor(0.18f, 0.18f, 0.28f),
    val timelineFrameSelected: SoCreateColor = SoCreateColor(0.914f, 0.271f, 0.376f),
    val timelineFrameKeyframe: SoCreateColor = SoCreateColor(1f, 0.84f, 0f),  // Gold

    // Layer panel
    val layerOutline: SoCreateColor = SoCreateColor(0f, 0.8f, 1f, 0.6f),  // Cyan for layer outlines
    val layerOutlineActive: SoCreateColor = SoCreateColor(0f, 1f, 0.6f, 0.9f),  // Green for active

    // Divider
    val divider: SoCreateColor = SoCreateColor(1f, 1f, 1f, 0.2f)
)

@Serializable
data class ThemeTypography(
    val headingSize: Float = 18f,
    val bodySize: Float = 14f,
    val captionSize: Float = 12f,
    val fontFamily: String = "sans-serif-medium"
)

@Serializable
data class ThemeShapes(
    val cornerRadius: Float = 12f,
    val toolbarCornerRadius: Float = 16f,
    val panelCornerRadius: Float = 8f,
    val buttonCornerRadius: Float = 8f
)

// ─── Built-in Theme Presets ─────────────────────────────────────────────────

object ThemePresets {

    val DEFAULT_DARK = AppTheme(
        id = "default_dark",
        name = "SoCreate Dark",
        isBuiltIn = true
    )

    val MIDNIGHT_OCEAN = AppTheme(
        id = "midnight_ocean",
        name = "Midnight Ocean",
        isBuiltIn = true,
        colors = ThemeColors(
            primary = SoCreateColor(0.118f, 0.565f, 1f),          // Dodger Blue
            primaryDark = SoCreateColor(0.082f, 0.396f, 0.698f),
            primaryLight = SoCreateColor(0.361f, 0.702f, 1f),
            accent = SoCreateColor(0f, 0.737f, 0.831f),            // Cyan
            backgroundDark = SoCreateColor(0.02f, 0.047f, 0.11f),
            backgroundSurface = SoCreateColor(0.035f, 0.063f, 0.14f),
            backgroundCanvas = SoCreateColor(0.043f, 0.075f, 0.16f),
            modifierButtonActiveBg = SoCreateColor(0.118f, 0.565f, 1f, 0.9f)
        )
    )

    val CRIMSON_STUDIO = AppTheme(
        id = "crimson_studio",
        name = "Crimson Studio",
        isBuiltIn = true,
        colors = ThemeColors(
            primary = SoCreateColor(0.867f, 0.125f, 0.173f),      // Crimson
            primaryDark = SoCreateColor(0.651f, 0.09f, 0.125f),
            primaryLight = SoCreateColor(1f, 0.322f, 0.373f),
            accent = SoCreateColor(1f, 0.753f, 0f),               // Amber
            backgroundDark = SoCreateColor(0.075f, 0.02f, 0.04f),
            backgroundSurface = SoCreateColor(0.1f, 0.035f, 0.05f),
            backgroundCanvas = SoCreateColor(0.12f, 0.05f, 0.07f),
            modifierButtonActiveBg = SoCreateColor(0.867f, 0.125f, 0.173f, 0.9f)
        )
    )

    val FOREST_CANVAS = AppTheme(
        id = "forest_canvas",
        name = "Forest Canvas",
        isBuiltIn = true,
        colors = ThemeColors(
            primary = SoCreateColor(0.098f, 0.635f, 0.388f),      // Emerald
            primaryDark = SoCreateColor(0.067f, 0.451f, 0.275f),
            primaryLight = SoCreateColor(0.192f, 0.8f, 0.514f),
            accent = SoCreateColor(0.6f, 0.824f, 0.2f),            // Lime
            backgroundDark = SoCreateColor(0.02f, 0.06f, 0.04f),
            backgroundSurface = SoCreateColor(0.035f, 0.08f, 0.05f),
            backgroundCanvas = SoCreateColor(0.047f, 0.09f, 0.06f),
            modifierButtonActiveBg = SoCreateColor(0.098f, 0.635f, 0.388f, 0.9f)
        )
    )

    val AMOLED_BLACK = AppTheme(
        id = "amoled_black",
        name = "AMOLED Black",
        isBuiltIn = true,
        colors = ThemeColors(
            primary = SoCreateColor(0.914f, 0.271f, 0.376f),
            primaryDark = SoCreateColor(0.722f, 0.188f, 0.290f),
            primaryLight = SoCreateColor(1f, 0.420f, 0.506f),
            accent = SoCreateColor(0.325f, 0.204f, 0.514f),
            backgroundDark = SoCreateColor(0f, 0f, 0f),
            backgroundSurface = SoCreateColor(0.03f, 0.03f, 0.03f),
            backgroundCanvas = SoCreateColor(0.02f, 0.02f, 0.02f),
            backgroundPanel = SoCreateColor(0.04f, 0.04f, 0.04f, 0.95f),
            backgroundToolbar = SoCreateColor(0.03f, 0.03f, 0.03f, 0.90f),
            modifierButtonActiveBg = SoCreateColor(0.914f, 0.271f, 0.376f, 0.9f)
        )
    )

    val PAPER_WHITE = AppTheme(
        id = "paper_white",
        name = "Paper White",
        isBuiltIn = true,
        isDarkMode = false,
        colors = ThemeColors(
            primary = SoCreateColor(0.914f, 0.271f, 0.376f),
            primaryDark = SoCreateColor(0.722f, 0.188f, 0.290f),
            primaryLight = SoCreateColor(1f, 0.420f, 0.506f),
            accent = SoCreateColor(0.325f, 0.204f, 0.514f),
            backgroundDark = SoCreateColor(0.95f, 0.95f, 0.95f),
            backgroundSurface = SoCreateColor(1f, 1f, 1f),
            backgroundCanvas = SoCreateColor(0.94f, 0.94f, 0.94f),
            backgroundPanel = SoCreateColor(1f, 1f, 1f, 0.95f),
            backgroundToolbar = SoCreateColor(0.97f, 0.97f, 0.97f, 0.90f),
            textPrimary = SoCreateColor(0.1f, 0.1f, 0.1f),
            textSecondary = SoCreateColor(0.4f, 0.4f, 0.4f),
            textDisabled = SoCreateColor(0.6f, 0.6f, 0.6f),
            divider = SoCreateColor(0f, 0f, 0f, 0.12f),
            modifierButtonBg = SoCreateColor(0.9f, 0.9f, 0.9f, 0.9f),
            modifierButtonText = SoCreateColor(0.2f, 0.2f, 0.2f),
            timelineBackground = SoCreateColor(0.92f, 0.92f, 0.92f),
            timelineTrackBg = SoCreateColor(0.96f, 0.96f, 0.96f),
            timelineFrameBg = SoCreateColor(0.88f, 0.88f, 0.88f)
        )
    )

    val SUNSET_GRADIENT = AppTheme(
        id = "sunset_gradient",
        name = "Sunset Gradient",
        isBuiltIn = true,
        colors = ThemeColors(
            primary = SoCreateColor(1f, 0.522f, 0.251f),          // Orange
            primaryDark = SoCreateColor(0.851f, 0.384f, 0.149f),
            primaryLight = SoCreateColor(1f, 0.651f, 0.4f),
            accent = SoCreateColor(0.957f, 0.263f, 0.624f),       // Pink
            backgroundDark = SoCreateColor(0.08f, 0.03f, 0.07f),
            backgroundSurface = SoCreateColor(0.1f, 0.05f, 0.09f),
            backgroundCanvas = SoCreateColor(0.12f, 0.06f, 0.1f),
            modifierButtonActiveBg = SoCreateColor(1f, 0.522f, 0.251f, 0.9f)
        )
    )

    val GALAXY_PURPLE = AppTheme(
        id = "galaxy_purple",
        name = "Galaxy Purple",
        isBuiltIn = true,
        colors = ThemeColors(
            primary = SoCreateColor(0.565f, 0.275f, 0.914f),      // Purple
            primaryDark = SoCreateColor(0.42f, 0.18f, 0.722f),
            primaryLight = SoCreateColor(0.702f, 0.42f, 1f),
            accent = SoCreateColor(0.251f, 0.878f, 0.816f),       // Mint
            backgroundDark = SoCreateColor(0.05f, 0.02f, 0.09f),
            backgroundSurface = SoCreateColor(0.07f, 0.035f, 0.12f),
            backgroundCanvas = SoCreateColor(0.09f, 0.05f, 0.14f),
            modifierButtonActiveBg = SoCreateColor(0.565f, 0.275f, 0.914f, 0.9f)
        )
    )

    val ALL = listOf(
        DEFAULT_DARK, MIDNIGHT_OCEAN, CRIMSON_STUDIO, FOREST_CANVAS,
        AMOLED_BLACK, PAPER_WHITE, SUNSET_GRADIENT, GALAXY_PURPLE
    )

    fun getById(id: String): AppTheme = ALL.find { it.id == id } ?: DEFAULT_DARK
}
