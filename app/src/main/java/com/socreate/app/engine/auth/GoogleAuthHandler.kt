package com.socreate.app.engine.auth

import android.content.Context
import android.content.Intent
import com.socreate.app.core.model.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

/**
 * Google Sign-In and YouTube sharing handler.
 *
 * ─── Privacy Model ──────────────────────────────────────────────────────
 * This class handles Google Sign-In with STRICT privacy guarantees:
 *
 * 1. The user connects DIRECTLY to Google — no credentials pass through
 *    the developer's servers, apps, or any third party
 * 2. The developer (Soquarky / AdventuresInDrawing) NEVER sees, stores,
 *    or processes the user's Google email, password, tokens, or any
 *    account data
 * 3. The Google Sign-In token stays on the user's device
 * 4. The account info (name, email, photo) is stored ONLY in local app
 *    state on the user's device and is NEVER transmitted to the developer
 * 5. Google account data is used SOLELY for:
 *    - YouTube video uploading (user-initiated, animations only)
 *    - Future: Cloud backup to user's OWN Google Drive
 *    - Future: Google Play Games achievements
 *
 * Google Sign-In is OPTIONAL — the app is fully functional without it.
 * Only required for YouTube sharing feature.
 *
 * ─── YouTube Sharing ────────────────────────────────────────────────────
 * - Animations ONLY — static images cannot be shared to YouTube
 * - User must be signed in to Google
 * - User must grant YouTube upload permission
 * - User configures title, description, privacy, tags BEFORE uploading
 * - Default description includes SoCreate branding and artistso.com link
 * - Export resolution configurable (480p to 4K)
 * - Optional watermark
 */
class GoogleAuthHandler(private val context: Context) {

    companion object {
        /**
         * YouTube upload scope.
         * Only requested when user explicitly tries to share to YouTube.
         * NOT requested at sign-in time to minimize permissions.
         */
        val YOUTUBE_SCOPE = Scope("https://www.googleapis.com/auth/youtube.upload")

        /** Basic profile scope — name and photo only */
        val PROFILE_SCOPE = Scope(Scopes.PROFILE)

        /** Email scope — for YouTube channel identification */
        val EMAIL_SCOPE = Scope(Scopes.EMAIL)
    }

    /**
     * Create the Google Sign-In client.
     *
     * Only requests basic profile (name, photo) and email.
     * YouTube scope is requested separately when needed.
     */
    fun createSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Create sign-in client with YouTube upload scope.
     * Called when user wants to share to YouTube.
     */
    fun createYouTubeSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(YOUTUBE_SCOPE)
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get the sign-in intent to launch the Google Sign-In flow.
     */
    fun getSignInIntent(): Intent {
        return createSignInClient().signInIntent
    }

    /**
     * Get sign-in intent with YouTube scope.
     */
    fun getYouTubeSignInIntent(): Intent {
        return createYouTubeSignInClient().signInIntent
    }

    /**
     * Handle the sign-in result from onActivityResult.
     *
     * Returns UserAccount with account info — this data stays on-device ONLY.
     * The developer never receives this data.
     *
     * @param data The intent data from onActivityResult
     * @return UserAccount if sign-in succeeded, null if failed/cancelled
     */
    fun handleSignInResult(data: Intent?): UserAccount? {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java) ?: return null

            return UserAccount(
                isSignedIn = true,
                googleAccountId = account.id ?: "",
                displayName = account.displayName ?: "",
                email = account.email ?: "",
                profilePhotoUrl = account.photoUrl?.toString() ?: "",
                youtubeChannelId = "",  // Fetched separately if needed
                youtubeConnected = false,
                permissions = UserPermissions(
                    hasGoogleSignIn = true,
                    hasYouTubeAccess = false  // Requires separate scope grant
                ),
                firstLoginTimestamp = System.currentTimeMillis(),
                lastLoginTimestamp = System.currentTimeMillis()
            )
        } catch (e: ApiException) {
            return null
        }
    }

    /**
     * Check if the user is currently signed in.
     */
    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    /**
     * Check if the user has granted YouTube upload permission.
     */
    fun hasYouTubeAccess(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(account, YOUTUBE_SCOPE)
    }

    /**
     * Get the current signed-in account info (local only).
     */
    fun getCurrentAccount(): UserAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null

        val hasYouTube = GoogleSignIn.hasPermissions(account, YOUTUBE_SCOPE)

        return UserAccount(
            isSignedIn = true,
            googleAccountId = account.id ?: "",
            displayName = account.displayName ?: "",
            email = account.email ?: "",
            profilePhotoUrl = account.photoUrl?.toString() ?: "",
            youtubeConnected = hasYouTube,
            permissions = UserPermissions(
                hasGoogleSignIn = true,
                hasYouTubeAccess = hasYouTube
            ),
            lastLoginTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Sign out the user.
     * This removes the Google account association from the app.
     * Does NOT affect the user's Google account in any way.
     */
    fun signOut(client: GoogleSignInClient, onComplete: () -> Unit) {
        client.signOut().addOnCompleteListener { onComplete() }
    }

    /**
     * Prepare a YouTube share configuration with defaults.
     * The user can customize before uploading.
     */
    fun createDefaultYouTubeConfig(projectName: String): YouTubeShareConfig {
        return YouTubeShareConfig(
            isAvailable = true,
            videoTitle = projectName,
            videoDescription = buildString {
                appendLine("Created with SoCreate — Animate Your Imagination")
                appendLine()
                appendLine("Learn art & animation: https://artistso.com")
                appendLine("Get SoCreate: https://soquarky.click")
                appendLine()
                appendLine("#SoCreate #Animation #DigitalArt #ArtAnimation #Soquarky")
            },
            tags = listOf("SoCreate", "Animation", "DigitalArt", "ArtAnimation", "Soquarky"),
            privacy = YouTubePrivacy.UNLISTED,  // Default to unlisted for safety
            exportResolution = YouTubeResolution.FULL_HD,
            exportFrameRate = 24
        )
    }
}
