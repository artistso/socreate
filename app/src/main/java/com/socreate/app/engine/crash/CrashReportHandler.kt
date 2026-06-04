package com.socreate.app.engine.crash

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.socreate.app.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles crash reporting with strict user ownership of all data.
 *
 * Privacy model:
 * ─────────────
 * - ALL crash data is stored locally on the user's device
 * - The developer (Soquarky/AdventuresInDrawing) NEVER automatically receives crash data
 * - Users can OPT-IN to share crash reports
 * - When sharing, users post to THEIR OWN GitHub repository and send an email
 *   to soquarky@artistso.com with a description and link
 * - The email includes: device model, issue description, GitHub link
 * - The developer only sees what the user explicitly chooses to send
 *
 * Storage format:
 * - Each crash is stored as a JSON file in app private storage
 * - Files are named: crash_{timestamp}_{type}.json
 * - Maximum 50 local reports (configurable)
 *
 * Sharing workflow:
 * 1. User views the crash report in the app
 * 2. User adds steps to reproduce and notes
 * 3. User clicks "Post to GitHub" → creates an issue in their own repo
 * 4. User clicks "Email Developer" → opens email with report body
 * 5. Email is addressed to soquarky@artistso.com
 * 6. Subject includes device model and error type
 */
class CrashReportHandler(private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

    private val crashDir: File by lazy {
        File(context.filesDir, "crash_reports").apply { mkdirs() }
    }

    /**
     * Store a crash report locally on the user's device.
     * This is the ONLY automatic action — just save the data.
     */
    suspend fun storeCrashReport(report: CrashReport) = withContext(Dispatchers.IO) {
        val filename = "crash_${dateFormat.format(Date(report.timestamp))}_${report.crashType.name.lowercase()}.json"
        val file = File(crashDir, filename)
        FileWriter(file).use { writer ->
            writer.write(json.encodeToString(CrashReport.serializer(), report))
        }

        // Enforce max report count
        enforceMaxReports(50)
    }

    /**
     * Read all locally stored crash reports.
     */
    suspend fun loadAllReports(): List<CrashReport> = withContext(Dispatchers.IO) {
        crashDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull { file ->
                try {
                    json.decodeFromString(CrashReport.serializer(), file.readText())
                } catch (e: Exception) { null }
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    /**
     * Delete a specific crash report from local storage.
     */
    suspend fun deleteReport(reportId: String) = withContext(Dispatchers.IO) {
        crashDir.listFiles()?.forEach { file ->
            try {
                val report = json.decodeFromString(CrashReport.serializer(), file.readText())
                if (report.id == reportId) file.delete()
            } catch (_: Exception) { }
        }
    }

    /**
     * Clear all local crash reports.
     */
    suspend fun clearAllReports() = withContext(Dispatchers.IO) {
        crashDir.deleteRecursively()
        crashDir.mkdirs()
    }

    /**
     * Create an email intent for sending a crash report to the developer.
     *
     * The email is pre-filled with:
     * - Device model and Android version
     * - Exception details and stack trace
     * - User's notes and steps to reproduce
     * - GitHub issue link (if posted)
     *
     * The user can edit everything before sending.
     * Destination: soquarky@artistso.com
     */
    fun createEmailIntent(report: CrashReport): Intent {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("soquarky@artistso.com"))
            putExtra(Intent.EXTRA_SUBJECT, report.toEmailSubject())
            putExtra(Intent.EXTRA_TEXT, report.toEmailBody())
        }
        return Intent.createChooser(emailIntent, "Send Crash Report via Email")
    }

    /**
     * Create a GitHub issue intent (opens GitHub in browser).
     * The user must have a GitHub account and repository configured.
     */
    fun createGitHubIssueIntent(config: CrashReportConfig, report: CrashReport): Intent {
        val baseUrl = if (config.githubUsername.isNotBlank() && config.githubRepoName.isNotBlank()) {
            "https://github.com/${config.githubUsername}/${config.githubRepoName}/issues/new"
        } else {
            "https://github.com/new"  // Prompt to create repo
        }

        val url = Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("title", report.toEmailSubject())
            .appendQueryParameter("body", report.toGitHubIssueBody())
            .build()

        return Intent(Intent.ACTION_VIEW, url)
    }

    /**
     * Create a crash report from an uncaught exception.
     */
    fun createReportFromException(
        thread: Thread,
        throwable: Throwable,
        canvasWidth: Int = 0,
        canvasHeight: Int = 0,
        layerCount: Int = 0,
        frameCount: Int = 0
    ): CrashReport {
        val stackTrace = throwable.stackTraceToString()

        return CrashReport(
            timestamp = System.currentTimeMillis(),
            crashType = classifyCrash(throwable),
            threadName = thread.name,
            exceptionClass = throwable.javaClass.name,
            exceptionMessage = throwable.message ?: "",
            stackTrace = stackTrace,
            deviceModel = Build.MODEL,
            deviceManufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            appVersionName = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE,
            isTablet = isTablet(),
            screenResolution = getScreenResolution(),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            layerCount = layerCount,
            frameCount = frameCount
        )
    }

    private fun classifyCrash(throwable: Throwable): CrashType {
        return when {
            throwable is OutOfMemoryError -> CrashType.OUT_OF_MEMORY
            throwable.javaClass.name.contains("OpenGL") || throwable.javaClass.name.contains("GLES") -> CrashType.GPU_ERROR
            throwable.javaClass.name.contains("FileNotFoundException") ||
                throwable.javaClass.name.contains("IOException") -> CrashType.FILE_IO_ERROR
            throwable.javaClass.name.contains("SQLite") -> CrashType.CORRUPTION_ERROR
            else -> CrashType.RUNTIME_EXCEPTION
        }
    }

    private fun enforceMaxReports(maxCount: Int) {
        val files = crashDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.sortedBy { it.lastModified() }
            ?: return

        if (files.size > maxCount) {
            files.take(files.size - maxCount).forEach { it.delete() }
        }
    }

    private fun isTablet(): Boolean {
        val config = context.resources.configuration
        return config.smallestScreenWidthDp >= 600
    }

    private fun getScreenResolution(): String {
        val metrics = context.resources.displayMetrics
        return "${metrics.widthPixels}x${metrics.heightPixels}"
    }

    companion object {
        /** Build config values — populated by Gradle */
        private object BuildConfig {
            const val VERSION_NAME = "1.1.0"
            const val VERSION_CODE = 2
        }
    }
}
