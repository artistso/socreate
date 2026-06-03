package com.socreate.app

import com.google.common.truth.Truth.assertThat
import com.socreate.app.core.model.*
import org.junit.Test

/**
 * Tests for new features:
 * - Color theme system
 * - Enhanced onion skin
 * - Layer outlining
 * - Multi-frame selection (Aseprite/Resprite)
 * - On-screen modifier keys
 * - Puppet mesh tools
 * - Frame tags
 * - Crash report models
 * - User account models
 * - YouTube sharing config
 */
class NewFeatureTests {

    // ─── Color Theme System ──────────────────────────────────────────────────

    @Test
    fun `default dark theme has correct accent color`() {
        val theme = ThemePresets.DEFAULT_DARK
        assertThat(theme.name).isEqualTo("SoCreate Dark")
        assertThat(theme.isBuiltIn).isTrue()
        assertThat(theme.isDarkMode).isTrue()
    }

    @Test
    fun `all built-in themes are unique by id`() {
        val ids = ThemePresets.ALL.map { it.id }
        assertThat(ids).hasSize(ThemePresets.ALL.size)
        assertThat(ids.toSet()).hasSize(ThemePresets.ALL.size)
    }

    @Test
    fun `getById returns default for unknown id`() {
        val theme = ThemePresets.getById("nonexistent_theme")
        assertThat(theme.id).isEqualTo("default_dark")
    }

    @Test
    fun `getById returns correct theme`() {
        val theme = ThemePresets.getById("amoled_black")
        assertThat(theme.name).isEqualTo("AMOLED Black")
    }

    @Test
    fun `paper white theme is light mode`() {
        val theme = ThemePresets.PAPER_WHITE
        assertThat(theme.isDarkMode).isFalse()
    }

    @Test
    fun `theme colors contain valid RGBA values`() {
        for (theme in ThemePresets.ALL) {
            val c = theme.colors.primary
            assertThat(c.red).isAtLeast(0f)
            assertThat(c.green).isAtLeast(0f)
            assertThat(c.blue).isAtLeast(0f)
            assertThat(c.alpha).isAtLeast(0f)
        }
    }

    // ─── Enhanced Onion Skin ─────────────────────────────────────────────────

    @Test
    fun `onion skin config defaults to classic mode`() {
        val config = OnionSkinConfig()
        assertThat(config.mode).isEqualTo(OnionSkinMode.CLASSIC)
        assertThat(config.isEnabled).isFalse()
    }

    @Test
    fun `onion skin has all blend modes`() {
        assertThat(OnionBlendMode.entries).hasSize(9)
    }

    @Test
    fun `onion skin has all effect overlays`() {
        assertThat(OnionEffectOverlay.entries).hasSize(8)
    }

    @Test
    fun `onion skin opacity curve linear fades correctly`() {
        val config = OnionSkinConfig(
            isEnabled = true,
            framesBefore = 4,
            opacityBefore = 0.4f,
            fadeWithDistance = true,
            opacityCurve = OnionOpacityCurve.LINEAR
        )
        val renderer = com.socreate.app.engine.renderer.OnionSkinRenderer()

        val opacity1 = renderer.calculateFrameOpacity(-1, config)
        val opacity4 = renderer.calculateFrameOpacity(-4, config)

        assertThat(opacity1).isGreaterThan(opacity4)
        assertThat(opacity1).isGreaterThan(0f)
    }

    @Test
    fun `onion skin step curve only shows first frame`() {
        val config = OnionSkinConfig(
            isEnabled = true,
            framesBefore = 4,
            opacityBefore = 0.4f,
            opacityCurve = OnionOpacityCurve.STEP,
            fadeWithDistance = true
        )
        val renderer = com.socreate.app.engine.renderer.OnionSkinRenderer()

        val opacity1 = renderer.calculateFrameOpacity(-1, config)
        val opacity2 = renderer.calculateFrameOpacity(-2, config)

        assertThat(opacity1).isGreaterThan(0f)
        assertThat(opacity2).isEqualTo(0f)
    }

    @Test
    fun `onion skin disabled returns zero opacity`() {
        val config = OnionSkinConfig(isEnabled = false)
        val renderer = com.socreate.app.engine.renderer.OnionSkinRenderer()

        val opacity = renderer.calculateFrameOpacity(-1, config)
        assertThat(opacity).isEqualTo(0f)
    }

    // ─── Layer Outlining ─────────────────────────────────────────────────────

    @Test
    fun `layer outline config defaults to all visible mode`() {
        val config = LayerOutlineConfig()
        assertThat(config.mode).isEqualTo(LayerOutlineMode.ALL_VISIBLE)
        assertThat(config.isEnabled).isFalse()
    }

    @Test
    fun `layer outline active layer color differs from default`() {
        val config = LayerOutlineConfig()
        assertThat(config.activeLayerColor).isNotEqualTo(config.color)
    }

    @Test
    fun `layer outline dash patterns are valid`() {
        val renderer = com.socreate.app.engine.renderer.LayerOutlineRenderer()
        for (pattern in OutlineDashPattern.entries) {
            val intervals = renderer.getDashIntervals(pattern)
            if (pattern == OutlineDashPattern.SOLID) {
                assertThat(intervals).isEmpty()
            } else {
                assertThat(intervals).isNotEmpty()
            }
        }
    }

    // ─── Multi-Frame Selection ───────────────────────────────────────────────

    @Test
    fun `multi-frame selection starts empty`() {
        val sel = MultiFrameSelection()
        assertThat(sel.isEmpty).isTrue()
        assertThat(sel.count).isEqualTo(0)
        assertThat(sel.isActive).isFalse()
    }

    @Test
    fun `selecting frames adds them`() {
        val sel = MultiFrameSelection()
            .selectFrame(0)
            .selectFrame(2)
            .selectFrame(4)

        assertThat(sel.count).isEqualTo(3)
        assertThat(sel.isActive).isTrue()
        assertThat(sel.selectedFrameIndices).containsExactly(0, 2, 4)
    }

    @Test
    fun `deselecting frame removes it`() {
        val sel = MultiFrameSelection()
            .selectFrame(0)
            .selectFrame(2)
            .deselectFrame(0)

        assertThat(sel.count).isEqualTo(1)
        assertThat(sel.selectedFrameIndices).containsExactly(2)
    }

    @Test
    fun `range selection selects inclusive range`() {
        val sel = MultiFrameSelection()
            .selectRange(2, 6)

        assertThat(sel.count).isEqualTo(5)
        assertThat(sel.selectedFrameIndices).containsExactly(2, 3, 4, 5, 6)
        assertThat(sel.isRange).isTrue()
    }

    @Test
    fun `reversed range selection works`() {
        val sel = MultiFrameSelection()
            .selectRange(6, 2)

        assertThat(sel.count).isEqualTo(5)
        assertThat(sel.selectedFrameIndices).containsExactly(2, 3, 4, 5, 6)
    }

    @Test
    fun `select all works`() {
        val sel = MultiFrameSelection().selectAll(10)

        assertThat(sel.count).isEqualTo(10)
    }

    @Test
    fun `clear selection empties everything`() {
        val sel = MultiFrameSelection()
            .selectAll(10)
            .clear()

        assertThat(sel.isEmpty).isTrue()
        assertThat(sel.isActive).isFalse()
    }

    @Test
    fun `toggle frame adds if not present`() {
        val sel = MultiFrameSelection().toggleFrame(3)

        assertThat(sel.selectedFrameIndices).contains(3)
    }

    @Test
    fun `toggle frame removes if present`() {
        val sel = MultiFrameSelection()
            .selectFrame(3)
            .toggleFrame(3)

        assertThat(sel.selectedFrameIndices).doesNotContain(3)
    }

    // ─── On-Screen Modifier Keys ─────────────────────────────────────────────

    @Test
    fun `modifier state defaults to nothing pressed`() {
        val state = ModifierKeyState()
        assertThat(state.isCtrlPressed).isFalse()
        assertThat(state.isShiftPressed).isFalse()
        assertThat(state.isAltPressed).isFalse()
        assertThat(state.isMultiSelect).isFalse()
    }

    @Test
    fun `ctrl pressed enables multi-select`() {
        val state = ModifierKeyState(isCtrlPressed = true)
        assertThat(state.isMultiSelect).isTrue()
        assertThat(state.isToggleSelect).isTrue()
    }

    @Test
    fun `shift pressed enables range select`() {
        val state = ModifierKeyState(isShiftPressed = true)
        assertThat(state.isRangeSelect).isTrue()
        assertThat(state.isAddToSelection).isTrue()
    }

    @Test
    fun `ctrl+alt enables subtract from selection`() {
        val state = ModifierKeyState(isCtrlPressed = true, isAltPressed = true)
        assertThat(state.isSubtractFromSelection).isTrue()
    }

    // ─── Puppet Mesh Tools ───────────────────────────────────────────────────

    @Test
    fun `puppet mesh starts empty`() {
        val mesh = PuppetMesh()
        assertThat(mesh.vertexCount).isEqualTo(0)
        assertThat(mesh.triangleCount).isEqualTo(0)
        assertThat(mesh.pinCount).isEqualTo(0)
        assertThat(mesh.boneCount).isEqualTo(0)
    }

    @Test
    fun `mesh density enum has all values`() {
        assertThat(MeshDensity.entries).hasSize(6)
        assertThat(MeshDensity.VERY_COARSE.divisions).isEqualTo(4)
        assertThat(MeshDensity.ULTRA.divisions).isEqualTo(48)
    }

    @Test
    fun `mesh engine generates correct vertex count`() {
        val engine = com.socreate.app.engine.puppet.MeshEngine()
        val bounds = Bounds(0f, 0f, 100f, 100f)
        val config = MeshGenConfig(density = MeshDensity.COARSE)

        val mesh = engine.generateMesh(bounds, config)

        // COARSE = 8 divisions → 9x9 grid = 81 vertices
        assertThat(mesh.vertexCount).isEqualTo(81)
        // 2 triangles per cell, 8x8 = 64 cells → 128 triangles
        assertThat(mesh.triangleCount).isEqualTo(128)
    }

    @Test
    fun `mesh engine generates correct edge count`() {
        val engine = com.socreate.app.engine.puppet.MeshEngine()
        val bounds = Bounds(0f, 0f, 100f, 100f)
        val config = MeshGenConfig(density = MeshDensity.VERY_COARSE)  // 4 divisions

        val mesh = engine.generateMesh(bounds, config)

        // VERY_COARSE = 4 divisions → 5x5 grid
        // Horizontal edges: 5 * 4 = 20
        // Vertical edges: 4 * 5 = 20
        // Diagonal edges: 4 * 4 = 16
        // Total = 56
        assertThat(mesh.edges.size).isEqualTo(56)
    }

    @Test
    fun `mesh vertex has original and current positions`() {
        val vertex = MeshVertex(originalX = 10f, originalY = 20f)
        assertThat(vertex.currentX).isEqualTo(10f)
        assertThat(vertex.currentY).isEqualTo(20f)
    }

    @Test
    fun `mesh pin types are distinct`() {
        assertThat(PinType.entries).hasSize(6)
    }

    @Test
    fun `rig presets have correct counts`() {
        assertThat(RigPresets.HUMANOID.pinPositions).hasSize(23)
        assertThat(RigPresets.HUMANOID.boneConnections).hasSize(16)
        assertThat(RigPresets.FACE.pinPositions).hasSize(18)
        assertThat(RigPresets.HAND.pinPositions).hasSize(17)
        assertThat(RigPresets.QUADRUPED.pinPositions).hasSize(19)
    }

    @Test
    fun `mesh pose interpolation at t=0 returns from pose`() {
        val engine = com.socreate.app.engine.puppet.MeshEngine()
        val pinId = "pin1"
        val from = MeshPose(
            meshId = "mesh1",
            pinPositions = mapOf(pinId to PinPose(pinId, 10f, 20f, 0f, 1f))
        )
        val to = MeshPose(
            meshId = "mesh1",
            pinPositions = mapOf(pinId to PinPose(pinId, 30f, 40f, 90f, 2f))
        )

        val result = engine.interpolatePoses(from, to, 0f)
        assertThat(result[pinId]?.x).isEqualTo(10f)
        assertThat(result[pinId]?.y).isEqualTo(20f)
    }

    @Test
    fun `mesh pose interpolation at t=1 returns to pose`() {
        val engine = com.socreate.app.engine.puppet.MeshEngine()
        val pinId = "pin1"
        val from = MeshPose(
            meshId = "mesh1",
            pinPositions = mapOf(pinId to PinPose(pinId, 10f, 20f, 0f, 1f))
        )
        val to = MeshPose(
            meshId = "mesh1",
            pinPositions = mapOf(pinId to PinPose(pinId, 30f, 40f, 90f, 2f))
        )

        val result = engine.interpolatePoses(from, to, 1f)
        assertThat(result[pinId]?.x).isEqualTo(30f)
        assertThat(result[pinId]?.y).isEqualTo(40f)
    }

    @Test
    fun `mesh pose interpolation at t=0_5 returns midpoint`() {
        val engine = com.socreate.app.engine.puppet.MeshEngine()
        val pinId = "pin1"
        val from = MeshPose(
            meshId = "mesh1",
            pinPositions = mapOf(pinId to PinPose(pinId, 0f, 0f, 0f, 1f))
        )
        val to = MeshPose(
            meshId = "mesh1",
            pinPositions = mapOf(pinId to PinPose(pinId, 100f, 200f, 180f, 3f))
        )

        val result = engine.interpolatePoses(from, to, 0.5f)
        assertThat(result[pinId]?.x).isEqualTo(50f)
        assertThat(result[pinId]?.y).isEqualTo(100f)
        assertThat(result[pinId]?.rotation).isEqualTo(90f)
        assertThat(result[pinId]?.scale).isEqualTo(2f)
    }

    @Test
    fun `nearest pin finds closest pin`() {
        val mesh = PuppetMesh(
            pins = listOf(
                MeshPin(id = "p1", originalX = 0f, originalY = 0f, currentX = 0f, currentY = 0f),
                MeshPin(id = "p2", originalX = 100f, originalY = 100f, currentX = 100f, currentY = 100f),
                MeshPin(id = "p3", originalX = 50f, originalY = 50f, currentX = 50f, currentY = 50f)
            )
        )

        val nearest = mesh.nearestPin(5f, 5f)
        assertThat(nearest?.id).isEqualTo("p1")

        val nearest2 = mesh.nearestPin(95f, 95f)
        assertThat(nearest2?.id).isEqualTo("p2")
    }

    // ─── Frame Tags ──────────────────────────────────────────────────────────

    @Test
    fun `frame tag has start and end`() {
        val tag = FrameTag(name = "Walk Cycle", startFrame = 0, endFrame = 12)
        assertThat(tag.name).isEqualTo("Walk Cycle")
        assertThat(tag.startFrame).isEqualTo(0)
        assertThat(tag.endFrame).isEqualTo(12)
    }

    @Test
    fun `frame color enum has all colors`() {
        assertThat(FrameColor.entries).hasSize(10)
    }

    // ─── Crash Report Models ─────────────────────────────────────────────────

    @Test
    fun `crash report email body contains device info`() {
        val report = CrashReport(
            deviceModel = "Galaxy Tab S10+",
            deviceManufacturer = "Samsung",
            androidVersion = "14",
            apiLevel = 34,
            exceptionClass = "NullPointerException",
            exceptionMessage = "Canvas was null"
        )
        val body = report.toEmailBody()

        assertThat(body).contains("Galaxy Tab S10+")
        assertThat(body).contains("Samsung")
        assertThat(body).contains("NullPointerException")
        assertThat(body).contains("crash data owned by the user")
    }

    @Test
    fun `crash report github issue is valid markdown`() {
        val report = CrashReport(
            deviceModel = "Galaxy Tab S10+",
            deviceManufacturer = "Samsung",
            androidVersion = "14",
            exceptionClass = "RuntimeException"
        )
        val body = report.toGitHubIssueBody()

        assertThat(body).contains("## SoCreate Crash Report")
        assertThat(body).contains("Galaxy Tab S10+")
        assertThat(body).contains("RuntimeException")
    }

    @Test
    fun `crash report email subject includes device and error`() {
        val report = CrashReport(
            deviceModel = "Galaxy Tab S10+",
            exceptionClass = "java.lang.NullPointerException"
        )
        val subject = report.toEmailSubject()

        assertThat(subject).contains("[SoCreate Crash]")
        assertThat(subject).contains("NullPointerException")
        assertThat(subject).contains("Galaxy Tab S10+")
    }

    @Test
    fun `crash config defaults to opt-in only`() {
        val config = CrashReportConfig()
        assertThat(config.autoCollectCrashData).isFalse()
        assertThat(config.storeCrashDataLocally).isTrue()
        assertThat(config.reportEmail).isEqualTo("soquarky@artistso.com")
    }

    @Test
    fun `crash types enum has all values`() {
        assertThat(CrashType.entries).hasSize(9)
    }

    // ─── User Account Models ─────────────────────────────────────────────────

    @Test
    fun `user account defaults to not signed in`() {
        val account = UserAccount()
        assertThat(account.isSignedIn).isFalse()
        assertThat(account.permissions.hasGoogleSignIn).isFalse()
        assertThat(account.permissions.hasYouTubeAccess).isFalse()
    }

    @Test
    fun `user preferences have sensible defaults`() {
        val prefs = UserPreferences()
        assertThat(prefs.selectedThemeId).isEqualTo("default_dark")
        assertThat(prefs.useOnScreenModifiers).isTrue()
        assertThat(prefs.crashReportingOptIn).isFalse()
        assertThat(prefs.palmRejection).isTrue()
    }

    // ─── YouTube Sharing ─────────────────────────────────────────────────────

    @Test
    fun `youtube share config defaults to unlisted`() {
        val config = YouTubeShareConfig()
        assertThat(config.privacy).isEqualTo(YouTubePrivacy.UNLISTED)
        assertThat(config.isAvailable).isFalse()
        assertThat(config.exportFrameRate).isEqualTo(24)
    }

    @Test
    fun `youtube resolutions go up to 4K`() {
        assertThat(YouTubeResolution.FOUR_K.width).isEqualTo(3840)
        assertThat(YouTubeResolution.FOUR_K.height).isEqualTo(2160)
    }

    // ─── Screen Overlay ──────────────────────────────────────────────────────

    @Test
    fun `overlay config defaults to disabled`() {
        val config = OverlayConfig()
        assertThat(config.isPermissionGranted).isFalse()
        assertThat(config.overlayMode).isEqualTo(OverlayMode.DISABLED)
    }

    @Test
    fun `overlay modes include floating toolbar`() {
        assertThat(OverlayMode.entries).hasSize(7)
        assertThat(OverlayMode.FLOATING_TOOLBAR.displayName).isEqualTo("Floating Toolbar")
    }

    // ─── Artistso Integration ────────────────────────────────────────────────

    @Test
    fun `artistso config has correct base url`() {
        val config = ArtistsoConfig()
        assertThat(config.baseUrl).isEqualTo("https://artistso.com")
        assertThat(config.instructorHandle).isEqualTo("@SoQuarky")
    }

    @Test
    fun `artistso content types include all`() {
        assertThat(ArtistsoContentType.entries).hasSize(8)
    }

    @Test
    fun `difficulty levels include all`() {
        assertThat(Difficulty.entries).hasSize(5)
    }

    // ─── DrawingState New Fields ─────────────────────────────────────────────

    @Test
    fun `drawing state includes all new feature fields`() {
        val state = DrawingState()

        // Theme
        assertThat(state.activeTheme).isNotNull()
        assertThat(state.availableThemes).hasSize(8)

        // Onion skin
        assertThat(state.enhancedOnionSkin).isNotNull()

        // Layer outline
        assertThat(state.layerOutlineConfig).isNotNull()

        // Multi-frame selection
        assertThat(state.multiFrameSelection.isEmpty).isTrue()

        // Modifier keys
        assertThat(state.modifierKeys.showOnScreenButtons).isTrue()

        // Puppet mesh
        assertThat(state.activePuppetMesh).isNull()

        // User account
        assertThat(state.userAccount.isSignedIn).isFalse()

        // YouTube
        assertThat(state.youtubeShareConfig.isAvailable).isFalse()

        // Artistso
        assertThat(state.artistsoConfig.isEnabled).isTrue()

        // Overlay
        assertThat(state.overlayConfig.overlayMode).isEqualTo(OverlayMode.DISABLED)

        // Crash reports
        assertThat(state.localCrashReports).isEmpty()
    }

    // ─── Advanced Reducer Tests ──────────────────────────────────────────────

    @Test
    fun `reducer handles SetTheme intent`() {
        val reducer = DrawingReducer()
        val state = DrawingState()

        val result = reducer.reduceAdvanced(state, AdvancedIntent.SetTheme("amoled_black"))
        assertThat(result.newState.activeTheme.id).isEqualTo("amoled_black")
    }

    @Test
    fun `reducer handles CycleTheme intent`() {
        val reducer = DrawingReducer()
        val state = DrawingState()

        val result = reducer.reduceAdvanced(state, AdvancedIntent.CycleTheme)
        assertThat(result.newState.activeTheme.id).isEqualTo("midnight_ocean")
    }

    @Test
    fun `reducer handles modifier key intents`() {
        val reducer = DrawingReducer()
        val state = DrawingState()

        val result = reducer.reduceAdvanced(state, AdvancedIntent.SetCtrlPressed(true))
        assertThat(result.newState.modifierKeys.isCtrlPressed).isTrue()

        val result2 = reducer.reduceAdvanced(result.newState, AdvancedIntent.SetShiftPressed(true))
        assertThat(result2.newState.modifierKeys.isShiftPressed).isTrue()
    }

    @Test
    fun `reducer handles multi-frame selection intents`() {
        val reducer = DrawingReducer()
        val state = DrawingState()

        val result = reducer.reduceAdvanced(state, AdvancedIntent.SelectFrame(0))
        assertThat(result.newState.multiFrameSelection.selectedFrameIndices).contains(0)

        val result2 = reducer.reduceAdvanced(result.newState, AdvancedIntent.SelectFrameRange(2, 5))
        assertThat(result2.newState.multiFrameSelection.count).isEqualTo(4)

        val result3 = reducer.reduceAdvanced(result2.newState, AdvancedIntent.ClearFrameSelection)
        assertThat(result3.newState.multiFrameSelection.isEmpty).isTrue()
    }

    @Test
    fun `reducer handles mesh tool intents`() {
        val reducer = DrawingReducer()
        val state = DrawingState()

        val result = reducer.reduceAdvanced(state, AdvancedIntent.SetMeshTool(MeshTool.ADD_PIN))
        assertThat(result.newState.meshTool).isEqualTo(MeshTool.ADD_PIN)
    }

    @Test
    fun `reducer handles Google sign-out`() {
        val reducer = DrawingReducer()
        val state = DrawingState(
            userAccount = UserAccount(isSignedIn = true, displayName = "Test User"),
            youtubeShareConfig = YouTubeShareConfig(isAvailable = true)
        )

        val result = reducer.reduceAdvanced(state, AdvancedIntent.SignOutGoogle)
        assertThat(result.newState.userAccount.isSignedIn).isFalse()
        assertThat(result.newState.youtubeShareConfig.isAvailable).isFalse()
    }

    @Test
    fun `reducer handles onion skin mode change`() {
        val reducer = DrawingReducer()
        val state = DrawingState()

        val result = reducer.reduceAdvanced(state, AdvancedIntent.SetOnionSkinMode(OnionSkinMode.BLUEPRINT))
        assertThat(result.newState.enhancedOnionSkin.mode).isEqualTo(OnionSkinMode.BLUEPRINT)
    }

    @Test
    fun `reducer handles layer outline toggle`() {
        val reducer = DrawingReducer()
        val state = DrawingState()

        val result = reducer.reduceAdvanced(state, AdvancedIntent.ToggleLayerOutline(true))
        assertThat(result.newState.layerOutlineConfig.isEnabled).isTrue()
    }

    @Test
    fun `reducer handles crash report storage`() {
        val reducer = DrawingReducer()
        val state = DrawingState()
        val report = CrashReport(exceptionClass = "TestException")

        val result = reducer.reduceAdvanced(state, AdvancedIntent.StoreCrashReport(report))
        assertThat(result.newState.localCrashReports).hasSize(1)
        assertThat(result.newState.localCrashReports[0].exceptionClass).isEqualTo("TestException")
    }

    @Test
    fun `reducer handles crash report deletion`() {
        val reducer = DrawingReducer()
        val report = CrashReport(id = "r1", exceptionClass = "Test")
        val state = DrawingState(localCrashReports = listOf(report))

        val result = reducer.reduceAdvanced(state, AdvancedIntent.DeleteCrashReport("r1"))
        assertThat(result.newState.localCrashReports).isEmpty()
    }

    @Test
    fun `reducer enforces max 50 crash reports`() {
        val reducer = DrawingReducer()
        val reports = (1..55).map { CrashReport(id = "r$it") }
        val state = DrawingState(localCrashReports = reports)

        val result = reducer.reduceAdvanced(state, AdvancedIntent.StoreCrashReport(CrashReport(id = "new")))
        assertThat(result.newState.localCrashReports).hasSize(50)
    }
}
