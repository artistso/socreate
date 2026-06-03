package com.socreate.app.engine.learning

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.socreate.app.core.model.ArtistsoConfig
import com.socreate.app.core.model.ArtistsoContent
import com.socreate.app.core.model.ArtistsoContentType
import com.socreate.app.core.model.Difficulty

/**
 * Integration with artistso.com — Steven Michael Allen Owens' teaching platform.
 *
 * Features:
 * - Browse tutorials, demos, and art demonstrations
 * - Watch live sessions
 * - Access course content
 * - View time-lapses and breakdowns
 * - Search by category, difficulty, or keyword
 * - In-app content viewer
 * - External browser fallback
 *
 * The app acts as a content browser/aggregator for artistso.com.
 * All content is hosted on artistso.com and displayed in-app via
 * WebView or opened in the user's default browser.
 *
 * Artistso.com is where the developer (@SoQuarky) teaches art and
 * hosts demonstrations of art and animations.
 */
class ArtistsoIntegration(private val context: Context) {

    private val config = ArtistsoConfig()

    /**
     * Open artistso.com in the user's default browser.
     */
    fun openInBrowser() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(config.baseUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Open a specific tutorial/demo page.
     */
    fun openContent(content: ArtistsoContent) {
        val url = content.articleUrl.ifBlank {
            content.videoUrl.ifBlank {
                "${config.baseUrl}/content/${content.id}"
            }
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Open artistso.com tutorials page.
     */
    fun openTutorials() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${config.baseUrl}/tutorials")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Open artistso.com demos page.
     */
    fun openDemos() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${config.baseUrl}/demos")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Open the artistso.com profile for @SoQuarky.
     */
    fun openInstructorProfile() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${config.baseUrl}/${config.instructorHandle}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Get sample/placeholder content for the feed.
     * In production, this would fetch from the artistso.com API.
     */
    fun getSampleContent(): List<ArtistsoContent> {
        return listOf(
            ArtistsoContent(
                id = "1",
                title = "Getting Started with Animation in SoCreate",
                description = "Learn the basics of frame-by-frame animation on your tablet",
                type = ArtistsoContentType.TUTORIAL,
                thumbnailUrl = "${config.baseUrl}/thumbs/tutorial_1.jpg",
                videoUrl = "${config.baseUrl}/videos/tutorial_1",
                category = "Animation Basics",
                difficulty = Difficulty.BEGINNER,
                author = "Steven Michael Allen Owens",
                authorHandle = "@SoQuarky",
                isFeatured = true
            ),
            ArtistsoContent(
                id = "2",
                title = "Character Rigging with Puppet Mesh",
                description = "Set up a full character rig using SoCreate's puppet mesh tools",
                type = ArtistsoContentType.DEMO,
                thumbnailUrl = "${config.baseUrl}/thumbs/demo_1.jpg",
                videoUrl = "${config.baseUrl}/videos/demo_1",
                category = "Rigging & Puppet Animation",
                difficulty = Difficulty.INTERMEDIATE,
                author = "Steven Michael Allen Owens",
                authorHandle = "@SoQuarky",
                isFeatured = true
            ),
            ArtistsoContent(
                id = "3",
                title = "Advanced Onion Skin Techniques",
                description = "Master onion skin modes for smooth animation workflows",
                type = ArtistsoContentType.BREAKDOWN,
                thumbnailUrl = "${config.baseUrl}/thumbs/breakdown_1.jpg",
                videoUrl = "${config.baseUrl}/videos/breakdown_1",
                category = "Motion Principles",
                difficulty = Difficulty.ADVANCED,
                author = "Steven Michael Allen Owens",
                authorHandle = "@SoQuarky"
            ),
            ArtistsoContent(
                id = "4",
                title = "Digital Painting on the Galaxy Tab S10+",
                description = "Techniques for getting the most from your S Pen and AMOLED display",
                type = ArtistsoContentType.TUTORIAL,
                thumbnailUrl = "${config.baseUrl}/thumbs/tutorial_2.jpg",
                videoUrl = "${config.baseUrl}/videos/tutorial_2",
                category = "Digital Painting",
                difficulty = Difficulty.BEGINNER,
                author = "Steven Michael Allen Owens",
                authorHandle = "@SoQuarky"
            ),
            ArtistsoContent(
                id = "5",
                title = "Color Theory for Animators",
                description = "Understanding color harmony, contrast, and mood in animation",
                type = ArtistsoContentType.COURSE,
                thumbnailUrl = "${config.baseUrl}/thumbs/course_1.jpg",
                videoUrl = "${config.baseUrl}/videos/course_1",
                category = "Color Theory",
                difficulty = Difficulty.ALL_LEVELS,
                author = "Steven Michael Allen Owens",
                authorHandle = "@SoQuarky",
                isFeatured = true
            ),
            ArtistsoContent(
                id = "6",
                title = "Speed Paint — Fantasy Landscape",
                description = "Watch a complete fantasy landscape painting from start to finish",
                type = ArtistsoContentType.TIME_LAPSE,
                thumbnailUrl = "${config.baseUrl}/thumbs/timelapse_1.jpg",
                videoUrl = "${config.baseUrl}/videos/timelapse_1",
                category = "Digital Painting",
                difficulty = Difficulty.ALL_LEVELS,
                author = "Steven Michael Allen Owens",
                authorHandle = "@SoQuarky"
            ),
            ArtistsoContent(
                id = "7",
                title = "Gesture Drawing for Animation",
                description = "Capture dynamic poses quickly to improve your character animation",
                type = ArtistsoContentType.TUTORIAL,
                thumbnailUrl = "${config.baseUrl}/thumbs/tutorial_3.jpg",
                videoUrl = "${config.baseUrl}/videos/tutorial_3",
                category = "Gesture Drawing",
                difficulty = Difficulty.INTERMEDIATE,
                author = "Steven Michael Allen Owens",
                authorHandle = "@SoQuarky"
            ),
            ArtistsoContent(
                id = "8",
                title = "Storyboarding Your First Animated Short",
                description = "Plan shots, timing, and story beats for a short animation",
                type = ArtistsoContentType.TUTORIAL,
                thumbnailUrl = "${config.baseUrl}/thumbs/tutorial_4.jpg",
                videoUrl = "${config.baseUrl}/videos/tutorial_4",
                category = "Storyboarding",
                difficulty = Difficulty.BEGINNER,
                author = "Steven Michael Allen Owens",
                authorHandle = "@SoQuarky"
            )
        )
    }
}
