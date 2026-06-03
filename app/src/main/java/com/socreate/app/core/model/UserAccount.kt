package com.socreate.app.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * User account, crash reporting, and sharing models.
 *
 * Privacy-first design:
 * - Google Sign-In connects user directly to Google; developer NEVER sees credentials
 * - Crash data is OWNED by the end user, stored on their device
 * - Crash reporting to developer is strictly OPT-IN
 * - User can post crash reports to THEIR OWN GitHub and email a link
 * - Developer only sees what the user explicitly chooses to send
 *
 * Google Sign-In is used solely for:
 * - YouTube sharing (requires Google account)
 * - Google Play Games integration (future)
 * - Cloud backup to user's own Google Drive (future)
 *
 * The developer (Soquarky / AdventuresInDrawing) never receives, stores, or
 * processes Google account data, tokens, or credentials.
 */

// ─── User Account ───────────────────────────────────────────────────────────

@Serializable
data class UserAccount(
    val isSignedIn: Boolean = false,
    val googleAccountId: String = "",          // Opaque ID — never sent to developer
    val displayName: String = "",
    val email: String = "",                    // Never sent to developer
    val profilePhotoUrl: String = "",
    val youtubeChannelId: String = "",
    val youtubeConnected: Boolean = false,
    val permissions: UserPermissions = UserPermissions(),
    val preferences: UserPreferences = UserPreferences(),
    val crashReportConfig: CrashReportConfig = CrashReportConfig(),
    val firstLoginTimestamp: Long = 0L,
    val lastLoginTimestamp: Long = 0L
)

@Serializable
data class UserPermissions(
    val hasOverlayPermission: Boolean = false,     // SYSTEM_ALERT_WINDOW
    val hasStoragePermission: Boolean = false,
    val hasMicrophonePermission: Boolean = false,   // For voice recording
    val hasCameraPermission: Boolean = false,       // For reference photos
    val hasNotificationPermission: Boolean = false,
    val hasGoogleSignIn: Boolean = false,
    val hasYouTubeAccess: Boolean = false
)

@Serializable
data class UserPreferences(
    val selectedThemeId: String = "default_dark",
    val useOnScreenModifiers: Boolean = true,
    val modifierButtonPosition: ModifierButtonPosition = ModifierButtonPosition.BOTTOM_LEFT,
    val autoSaveEnabled: Boolean = true,
    val crashReportingOptIn: Boolean = false,
    val analyticsOptIn: Boolean = false,
    val showArtistsoFeed: Boolean = true,
    val defaultCanvasPreset: String = "NATIVE_2800x1752",
    val defaultFrameRate: Int = 24,
    val pressureSensitivity: Float = 1f,
    val palmRejection: Boolean = true
)

// ─── Crash Report System (User-Owned) ───────────────────────────────────────

@Serializable
data class CrashReportConfig(
    // Privacy: Crash data is OWNED by the end user.
    // Developer NEVER collects crash data automatically.
    // All sharing is OPT-IN and user-initiated.
    val crashDataOwnershipNoticeAccepted: Boolean = false,
    val autoCollectCrashData: Boolean = false,          // User must opt in
    val storeCrashDataLocally: Boolean = true,           // Always stored on device
    val maxLocalCrashReports: Int = 50,                  // Keep last 50 on device
    val includeDeviceModel: Boolean = true,              // Include in report
    val includeAndroidVersion: Boolean = true,           // Include in report
    val includeAppVersion: Boolean = true,               // Include in report
    val includeStackTraces: Boolean = true,              // Include in report
    val includeMemoryInfo: Boolean = false,              // Opt-in only
    val includeScreenshot: Boolean = false,              // Opt-in only
    val includeCanvasState: Boolean = false,             // Opt-in only
    // GitHub integration
    val githubUsername: String = "",
    val githubRepoName: String = "",                     // User's repo for crash reports
    val githubToken: String = "",                        // User's PAT — stored locally ONLY
    // Email reporting
    val reportEmail: String = "soquarky@artistso.com",   // Developer's email for crash reports
    val emailSubjectPrefix: String = "[SoCreate Crash Report]"
)

@Serializable
data class CrashReport(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val crashType: CrashType = CrashType.RUNTIME_EXCEPTION,
    val threadName: String = "",
    val exceptionClass: String = "",
    val exceptionMessage: String = "",
    val stackTrace: String = "",
    val deviceModel: String = "",
    val deviceManufacturer: String = "",
    val androidVersion: String = "",
    val apiLevel: Int = 0,
    val appVersionName: String = "",
    val appVersionCode: Int = 0,
    val isTablet: Boolean = true,
    val screenResolution: String = "",
    val totalMemoryMb: Long = 0,
    val freeMemoryMb: Long = 0,
    val nativeHeapSizeMb: Long = 0,
    val canvasWidth: Int = 0,
    val canvasHeight: Int = 0,
    val layerCount: Int = 0,
    val frameCount: Int = 0,
    val projectOpenDuration: Long = 0,
    val stepsToReproduce: String = "",           // User can add details
    val userNotes: String = "",                  // User's description of the issue
    val screenshotPath: String? = null,
    val githubIssueUrl: String? = null,          // If posted to GitHub
    val emailSent: Boolean = false,
    val emailTimestamp: Long = 0L
) {
    /**
     * Format the crash report as a human-readable email body.
     * The user can edit this before sending.
     */
    fun toEmailBody(): String = buildString {
        appendLine("SoCreate Crash Report")
        appendLine("═══════════════════════════════════════")
        appendLine()
        appendLine("Device: $deviceManufacturer $deviceModel")
        appendLine("Android: $androidVersion (API $apiLevel)")
        appendLine("App Version: $appVersionName ($appVersionCode)")
        appendLine("Screen: $screenResolution")
        appendLine()
        appendLine("Exception: $exceptionClass")
        appendLine("Message: $exceptionMessage")
        appendLine("Thread: $threadName")
        appendLine()
        appendLine("Stack Trace:")
        appendLine(stackTrace.take(3000))  // Limit to avoid oversized emails
        appendLine()
        appendLine("Memory: Free ${freeMemoryMb}MB / Total ${totalMemoryMb}MB")
        appendLine("Canvas: ${canvasWidth}x${canvasHeight}, $layerCount layers, $frameCount frames")
        appendLine()
        appendLine("Steps to Reproduce:")
        appendLine(stepsToReproduce.ifEmpty { "(User did not provide steps)" })
        appendLine()
        appendLine("User Notes:")
        appendLine(userNotes.ifEmpty { "(No additional notes)" })
        appendLine()
        if (!githubIssueUrl.isNullOrEmpty()) {
            appendLine("GitHub Issue: $githubIssueUrl")
            appendLine()
        }
        appendLine("──")
        appendLine("Report generated by SoCreate — crash data owned by the user")
        appendLine("App by Soquarky / AdventuresInDrawing")
    }

    /**
     * Format as a GitHub issue body (Markdown).
     */
    fun toGitHubIssueBody(): String = buildString {
        appendLine("## SoCreate Crash Report")
        appendLine()
        appendLine("### Device Info")
        appendLine("| Property | Value |")
        appendLine("|---|---|")
        appendLine("| Device | $deviceManufacturer $deviceModel |")
        appendLine("| Android | $androidVersion (API $apiLevel) |")
        appendLine("| App Version | $appVersionName ($appVersionCode) |")
        appendLine("| Screen | $screenResolution |")
        appendLine("| Memory | Free ${freeMemoryMb}MB / ${totalMemoryMb}MB |")
        appendLine()
        appendLine("### Exception")
        appendLine("```")
        appendLine("$exceptionClass: $exceptionMessage")
        appendLine(stackTrace.take(5000))
        appendLine("```")
        appendLine()
        appendLine("### Canvas State")
        appendLine("- Canvas: ${canvasWidth}x${canvasHeight}")
        appendLine("- Layers: $layerCount")
        appendLine("- Frames: $frameCount")
        appendLine()
        if (stepsToReproduce.isNotBlank()) {
            appendLine("### Steps to Reproduce")
            appendLine(stepsToReproduce)
            appendLine()
        }
        if (userNotes.isNotBlank()) {
            appendLine("### Additional Notes")
            appendLine(userNotes)
            appendLine()
        }
        appendLine("---")
        appendLine("*Report generated by SoCreate — crash data owned by the user*")
    }

    /**
     * Format as a concise email subject line.
     */
    fun toEmailSubject(): String {
        val device = deviceModel.take(20)
        val error = exceptionClass.substringAfterLast(".").take(30)
        return "[SoCreate Crash] $error on $device"
    }
}

@Serializable
enum class CrashType {
    RUNTIME_EXCEPTION,
    ANR,                    // Application Not Responding
    NATIVE_CRASH,           // SIGSEGV, SIGABRT from C++ code
    OUT_OF_MEMORY,
    RENDERING_ERROR,
    FILE_IO_ERROR,
    CORRUPTION_ERROR,       // Project file corruption
    GPU_ERROR,              // OpenGL/Vulkan error
    UNKNOWN
}

// ─── YouTube Sharing ────────────────────────────────────────────────────────

@Serializable
data class YouTubeShareConfig(
    val isAvailable: Boolean = false,
    val videoTitle: String = "",
    val videoDescription: String = "",
    val tags: List<String> = emptyList(),
    val privacy: YouTubePrivacy = YouTubePrivacy.PUBLIC,
    val categoryId: Int = 24,               // 24 = Entertainment
    val madeForKids: Boolean = false,
    val defaultTitleFormat: String = "SoCreate Animation — {project_name}",
    val defaultDescription: String = buildString {
        appendLine("Created with SoCreate — Animate Your Imagination")
        appendLine()
        appendLine("Learn art & animation: https://artistso.com")
        appendLine("Get SoCreate: https://soquarky.click")
        appendLine()
        appendLine("#SoCreate #Animation #DigitalArt #ArtAnimation")
    },
    val includeWatermark: Boolean = false,
    val watermarkPosition: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
    val exportResolution: YouTubeResolution = YouTubeResolution.FULL_HD,
    val exportFrameRate: Int = 24,
    val exportFormat: String = "video/mp4"
)

@Serializable
enum class YouTubePrivacy {
    PUBLIC,
    UNLISTED,
    PRIVATE
}

@Serializable
enum class YouTubeResolution(val width: Int, val height: Int, val label: String) {
    SD_480(854, 480, "480p"),
    HD_720(1280, 720, "720p"),
    FULL_HD(1920, 1080, "1080p"),
    QUAD_HD(2560, 1440, "1440p"),
    FOUR_K(3840, 2160, "4K")
}

@Serializable
enum class WatermarkPosition {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER, NONE
}

// ─── Artistso.com Integration ───────────────────────────────────────────────

@Serializable
data class ArtistsoConfig(
    val isEnabled: Boolean = true,
    val baseUrl: String = "https://artistso.com",
    val apiEndpoint: String = "https://artistso.com/api/v1",
    val feedEnabled: Boolean = true,
    val tutorialsEnabled: Boolean = true,
    val demosEnabled: Boolean = true,
    val communityEnabled: Boolean = true,
    val showInGallery: Boolean = true,
    val showInSettings: Boolean = true,
    val cacheDurationHours: Int = 4,
    val maxCachedItems: Int = 100,
    val instructorHandle: String = "@SoQuarky",
    val featuredCategories: List<String> = listOf(
        "Animation Basics",
        "Character Design",
        "Motion Principles",
        "Digital Painting",
        "Storyboarding",
        "Rigging & Puppet Animation",
        "Effects & Particles",
        "Color Theory",
        "Perspective Drawing",
        "Gesture Drawing"
    )
)

@Serializable
data class ArtistsoContent(
    val id: String,
    val title: String,
    val description: String = "",
    val type: ArtistsoContentType = ArtistsoContentType.TUTORIAL,
    val thumbnailUrl: String = "",
    val videoUrl: String = "",
    val articleUrl: String = "",
    val duration: Int = 0,           // seconds
    val difficulty: Difficulty = Difficulty.BEGINNER,
    val category: String = "",
    val tags: List<String> = emptyList(),
    val author: String = "Steven Michael Allen Owens",
    val authorHandle: String = "@SoQuarky",
    val publishedAt: Long = 0,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val isFeatured: Boolean = false,
    val isInAppAccessible: Boolean = true
)

@Serializable
enum class ArtistsoContentType {
    TUTORIAL,
    DEMO,
    LIVE_SESSION,
    ARTICLE,
    TIME_LAPSE,
    BREAKDOWN,
    CHALLENGE,
    COURSE
}

@Serializable
enum class Difficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    PROFESSIONAL("Professional"),
    ALL_LEVELS("All Levels")
}

// ─── Screen Overlay Permission ──────────────────────────────────────────────

@Serializable
data class OverlayConfig(
    val isPermissionGranted: Boolean = false,
    val overlayMode: OverlayMode = OverlayMode.DISABLED,
    val opacity: Float = 0.85f,
    val showInRecents: Boolean = true,
    val pinToNotification: Boolean = false
)

@Serializable
enum class OverlayMode(val displayName: String) {
    DISABLED("Disabled"),
    FLOATING_TOOLBAR("Floating Toolbar"),        // Mini toolbar over other apps
    QUICK_DRAW("Quick Draw"),                     // Quick drawing pad overlay
    COLOR_PICKER("Color Picker"),                  // Pick colors from any app
    REFERENCE_VIEWER("Reference Viewer"),          // Float reference image over canvas
    TIMER("Animation Timer"),                      // Animation timer overlay
    CUSTOM("Custom")
}
