package com.socreate.app.core.model

/**
 * Pure state reducer for the Drawing screen.
 * Transforms DrawingState based on DrawingIntent with no side effects.
 * This is the single source of truth for all state changes.
 */
object DrawingReducer : Reducer<DrawingState, DrawingIntent> {

    private const val MAX_RECENT_COLORS = 30
    private const val MIN_ZOOM = 0.1f
    private const val MAX_ZOOM = 64f

    override fun reduce(currentState: DrawingState, intent: DrawingIntent): StateTransition<DrawingState> {
        return when (intent) {
            // ─── Stroke Drawing ───────────────────────────────────────────────

            is DrawingIntent.StrokeStarted -> {
                val (canvasX, canvasY) = currentState.screenToCanvas(intent.x, intent.y)
                val stroke = Stroke(
                    brushId = currentState.activeBrushId,
                    brushProperties = currentState.activeBrushProperties,
                    color = if (currentState.activeTool == DrawingTool.ERASER) {
                        SoCreateColor.TRANSPARENT
                    } else {
                        currentState.activeColor
                    },
                    blendMode = currentState.activeBrushProperties.blendMode,
                    layerId = currentState.layerStack.activeLayerId ?: ""
                ).addPoint(intent.point.copy(x = canvasX, y = canvasY))

                StateTransition(
                    currentState.copy(
                        currentStroke = stroke,
                        isDrawing = true
                    )
                )
            }

            is DrawingIntent.StrokeMoved -> {
                val stroke = currentState.currentStroke ?: return StateTransition(currentState)
                val canvasPoints = intent.points.map { point ->
                    val (cx, cy) = currentState.screenToCanvas(point.x, point.y)
                    point.copy(x = cx, y = cy)
                }
                StateTransition(
                    currentState.copy(
                        currentStroke = stroke.copy(
                            points = stroke.points + canvasPoints
                        )
                    )
                )
            }

            is DrawingIntent.StrokeEnded -> {
                val stroke = currentState.currentStroke ?: return StateTransition(currentState)
                val finalStroke = if (intent.finalPoint != null) {
                    val (cx, cy) = currentState.screenToCanvas(intent.finalPoint.x, intent.finalPoint.y)
                    stroke.addPoint(intent.finalPoint.copy(x = cx, y = cy))
                } else stroke

                // Add color to recents
                val updatedRecents = buildList {
                    add(finalStroke.color)
                    addAll(currentState.recentColors.filter { it != finalStroke.color })
                }.take(MAX_RECENT_COLORS)

                StateTransition(
                    currentState.copy(
                        currentStroke = null,
                        isDrawing = false,
                        canUndo = true,
                        canRedo = false,
                        recentColors = updatedRecents
                    ),
                    listOf(DrawingEffect.HapticFeedback(0.3f))
                )
            }

            is DrawingIntent.StrokeCancelled -> {
                StateTransition(
                    currentState.copy(
                        currentStroke = null,
                        isDrawing = false
                    )
                )
            }

            // ─── Tool Selection ───────────────────────────────────────────────

            is DrawingIntent.SelectTool -> StateTransition(
                currentState.copy(activeTool = intent.tool)
            )

            is DrawingIntent.SelectBrush -> StateTransition(
                currentState.copy(
                    activeBrushId = intent.brushId,
                    showBrushPicker = false
                )
            )

            is DrawingIntent.SetBrushSize -> StateTransition(
                currentState.copy(brushSize = intent.size.coerceIn(1f, 500f))
            )

            is DrawingIntent.SetBrushOpacity -> StateTransition(
                currentState.copy(brushOpacity = intent.opacity.coerceIn(0.01f, 1f))
            )

            is DrawingIntent.SetBrushSmoothing -> StateTransition(
                currentState.copy(brushSmoothing = intent.smoothing.coerceIn(0f, 1f))
            )

            is DrawingIntent.SetBrushBlendMode -> StateTransition(
                currentState.copy(
                    activeBrushProperties = currentState.activeBrushProperties.copy(
                        blendMode = intent.blendMode
                    )
                )
            )

            is DrawingIntent.UpdateBrushProperties -> StateTransition(
                currentState.copy(activeBrushProperties = intent.properties)
            )

            // ─── Color ────────────────────────────────────────────────────────

            is DrawingIntent.SetColor -> StateTransition(
                currentState.copy(activeColor = intent.color)
            )

            is DrawingIntent.PickColorFromCanvas -> StateTransition(
                currentState,
                listOf(DrawingEffect.ColorPicked(currentState.activeColor))
            )

            is DrawingIntent.SetColorHarmony -> StateTransition(
                currentState.copy(colorHarmony = intent.harmony)
            )

            is DrawingIntent.AddRecentColor -> {
                val updated = buildList {
                    add(intent.color)
                    addAll(currentState.recentColors.filter { it != intent.color })
                }.take(MAX_RECENT_COLORS)
                StateTransition(currentState.copy(recentColors = updated))
            }

            is DrawingIntent.ClearRecentColors -> StateTransition(
                currentState.copy(recentColors = emptyList())
            )

            // ─── Layers ──────────────────────────────────────────────────────

            is DrawingIntent.AddLayer -> {
                val newIndex = currentState.layerStack.layers.size
                val newLayer = Layer(
                    name = "Layer ${newIndex + 1}",
                    type = LayerType.RASTER,
                    sortOrder = newIndex
                )
                StateTransition(
                    currentState.copy(
                        layerStack = currentState.layerStack.addLayer(newLayer, newIndex)
                    )
                )
            }

            is DrawingIntent.DeleteLayer -> {
                if (currentState.layerStack.layers.size <= 1) {
                    return StateTransition(
                        currentState,
                        listOf(DrawingEffect.ShowToast("Cannot delete the last layer"))
                    )
                }
                StateTransition(
                    currentState.copy(
                        layerStack = currentState.layerStack.removeLayer(intent.layerId)
                    )
                )
            }

            is DrawingIntent.SelectLayer -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.setActiveLayer(intent.layerId)
                )
            )

            is DrawingIntent.SetLayerOpacity -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.updateLayer(intent.layerId) {
                        it.withOpacity(intent.opacity)
                    }
                )
            )

            is DrawingIntent.SetLayerBlendMode -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.updateLayer(intent.layerId) {
                        it.withBlendMode(intent.blendMode)
                    }
                )
            )

            is DrawingIntent.SetLayerVisibility -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.updateLayer(intent.layerId) {
                        it.withVisible(intent.isVisible)
                    }
                )
            )

            is DrawingIntent.SetLayerLocked -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.updateLayer(intent.layerId) {
                        it.withLocked(intent.isLocked)
                    }
                )
            )

            is DrawingIntent.RenameLayer -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.updateLayer(intent.layerId) {
                        it.withName(intent.name)
                    }
                )
            )

            is DrawingIntent.MoveLayer -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.moveLayer(intent.fromIndex, intent.toIndex)
                )
            )

            is DrawingIntent.MergeLayerDown -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.mergeDown(intent.layerId)
                )
            )

            is DrawingIntent.DuplicateLayer -> {
                val active = currentState.layerStack.activeLayer ?: return StateTransition(currentState)
                val duplicate = active.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "${active.name} Copy",
                    sortOrder = active.sortOrder + 1
                )
                StateTransition(
                    currentState.copy(
                        layerStack = currentState.layerStack.addLayer(
                            duplicate,
                            currentState.layerStack.layers.indexOf(active) + 1
                        )
                    )
                )
            }

            is DrawingIntent.SetClippingMask -> StateTransition(
                currentState.copy(
                    layerStack = currentState.layerStack.updateLayer(intent.layerId) {
                        it.copy(isClippingMask = intent.isClipping)
                    }
                )
            )

            // ─── Canvas Navigation ────────────────────────────────────────────

            is DrawingIntent.Zoom -> {
                val newZoom = (currentState.zoom * intent.factor).coerceIn(MIN_ZOOM, MAX_ZOOM)
                val newPanX = intent.pivotX - (intent.pivotX - currentState.panX) * (newZoom / currentState.zoom)
                val newPanY = intent.pivotY - (intent.pivotY - currentState.panY) * (newZoom / currentState.zoom)
                StateTransition(currentState.copy(zoom = newZoom, panX = newPanX, panY = newPanY))
            }

            is DrawingIntent.Pan -> StateTransition(
                currentState.copy(
                    panX = currentState.panX + intent.deltaX,
                    panY = currentState.panY + intent.deltaY
                )
            )

            is DrawingIntent.SetViewTransform -> StateTransition(
                currentState.copy(
                    zoom = intent.zoom.coerceIn(MIN_ZOOM, MAX_ZOOM),
                    panX = intent.panX,
                    panY = intent.panY
                )
            )

            is DrawingIntent.FitToScreen -> {
                // Calculate zoom to fit canvas in viewport
                // This will be refined once we know actual viewport size
                StateTransition(currentState.copy(zoom = 1f, panX = 0f, panY = 0f))
            }

            is DrawingIntent.ResetZoom -> StateTransition(
                currentState.copy(zoom = 1f, panX = 0f, panY = 0f, rotation = 0f)
            )

            DrawingIntent.ZoomIn -> StateTransition(
                currentState.copy(zoom = (currentState.zoom * 1.5f).coerceIn(MIN_ZOOM, MAX_ZOOM))
            )

            DrawingIntent.ZoomOut -> StateTransition(
                currentState.copy(zoom = (currentState.zoom / 1.5f).coerceIn(MIN_ZOOM, MAX_ZOOM))
            )

            // ─── Undo/Redo ────────────────────────────────────────────────────

            DrawingIntent.Undo -> StateTransition(
                currentState.copy(
                    canUndo = currentState.undoCount > 1,
                    canRedo = true,
                    undoCount = (currentState.undoCount - 1).coerceAtLeast(0),
                    redoCount = currentState.redoCount + 1
                )
            )

            DrawingIntent.Redo -> StateTransition(
                currentState.copy(
                    canUndo = true,
                    canRedo = currentState.redoCount > 1,
                    undoCount = currentState.undoCount + 1,
                    redoCount = (currentState.redoCount - 1).coerceAtLeast(0)
                )
            )

            // ─── Selection & Transform ────────────────────────────────────────

            is DrawingIntent.StartSelection -> StateTransition(
                currentState.copy(
                    selection = Selection(
                        bounds = Bounds(intent.startX, intent.startY, 0f, 0f),
                        type = intent.type
                    )
                )
            )

            is DrawingIntent.UpdateSelection -> {
                val sel = currentState.selection ?: return StateTransition(currentState)
                val newBounds = Bounds(
                    x = minOf(sel.bounds.x, intent.currentX),
                    y = minOf(sel.bounds.y, intent.currentY),
                    width = kotlin.math.abs(intent.currentX - sel.bounds.x),
                    height = kotlin.math.abs(intent.currentY - sel.bounds.y)
                )
                StateTransition(currentState.copy(selection = sel.copy(bounds = newBounds)))
            }

            is DrawingIntent.ConfirmSelection -> StateTransition(
                currentState.copy(selection = currentState.selection?.copy(isActive = true))
            )

            DrawingIntent.ClearSelection -> StateTransition(
                currentState.copy(selection = null)
            )

            is DrawingIntent.StartTransform -> StateTransition(
                currentState.copy(isTransforming = true, selection = intent.selection)
            )

            DrawingIntent.ConfirmTransform -> StateTransition(
                currentState.copy(isTransforming = false, selection = null, canUndo = true, canRedo = false)
            )

            DrawingIntent.CancelTransform -> StateTransition(
                currentState.copy(isTransforming = false)
            )

            // ─── Quick Actions ────────────────────────────────────────────────

            is DrawingIntent.ShowQuickMenu -> StateTransition(
                currentState.copy(
                    showQuickMenu = true,
                    quickMenuPosition = intent.x to intent.y
                )
            )

            DrawingIntent.HideQuickMenu -> StateTransition(
                currentState.copy(showQuickMenu = false)
            )

            // ─── UI Toggles ──────────────────────────────────────────────────

            DrawingIntent.ToggleBrushPicker -> StateTransition(
                currentState.copy(
                    showBrushPicker = !currentState.showBrushPicker,
                    showColorPicker = false,
                    showLayerPanel = false
                )
            )

            DrawingIntent.ToggleColorPicker -> StateTransition(
                currentState.copy(
                    showColorPicker = !currentState.showColorPicker,
                    showBrushPicker = false,
                    showLayerPanel = false
                )
            )

            DrawingIntent.ToggleLayerPanel -> StateTransition(
                currentState.copy(
                    showLayerPanel = !currentState.showLayerPanel,
                    showBrushPicker = false,
                    showColorPicker = false
                )
            )

            DrawingIntent.ToggleFullscreen -> StateTransition(
                currentState.copy(isFullscreen = !currentState.isFullscreen)
            )

            DrawingIntent.ToggleRulers -> StateTransition(
                currentState.copy(showRulers = !currentState.showRulers)
            )

            DrawingIntent.ToggleGrid -> StateTransition(
                currentState.copy(showGrid = !currentState.showGrid)
            )

            DrawingIntent.HideAllPanels -> StateTransition(
                currentState.copy(
                    showBrushPicker = false,
                    showColorPicker = false,
                    showLayerPanel = false,
                    showQuickMenu = false
                )
            )

            // ─── Project ─────────────────────────────────────────────────────

            DrawingIntent.SaveProject -> StateTransition(
                currentState.copy(isSaving = true),
                listOf(DrawingEffect.ProjectSaved)
            )

            DrawingIntent.ExportImage -> StateTransition(
                currentState,
                listOf(DrawingEffect.ShowExportDialog("PNG"))
            )

            is DrawingIntent.LoadProject -> StateTransition(currentState)
            is DrawingIntent.RenameProject -> StateTransition(currentState)

            // ─── Animation (stubs for future) ─────────────────────────────────

            is DrawingIntent.SetFrame -> StateTransition(
                currentState.copy(currentFrame = intent.frame)
            )

            DrawingIntent.PlayAnimation -> StateTransition(
                currentState.copy(isPlayingAnimation = true)
            )

            DrawingIntent.StopAnimation -> StateTransition(
                currentState.copy(isPlayingAnimation = false)
            )

            DrawingIntent.AddFrame,
            DrawingIntent.DuplicateFrame,
            DrawingIntent.DeleteFrame,
            is DrawingIntent.SetFrameRate,
            DrawingIntent.ToggleOnionSkinning,
            is DrawingIntent.FloodFill,
            DrawingIntent.CopySelection,
            DrawingIntent.CutSelection,
            DrawingIntent.PasteClipboard -> StateTransition(currentState)
        }
    }

    // ─── Advanced Intent Handling ────────────────────────────────────────────

    fun reduceAdvanced(currentState: DrawingState, intent: AdvancedIntent): StateTransition<DrawingState> {
        return when (intent) {
            // ─── Color Theme System ──────────────────────────────────────
            is AdvancedIntent.SetTheme -> {
                val theme = ThemePresets.getById(intent.themeId)
                StateTransition(currentState.copy(activeTheme = theme))
            }
            is AdvancedIntent.SetCustomTheme -> StateTransition(
                currentState.copy(activeTheme = intent.theme)
            )
            is AdvancedIntent.ExportTheme -> StateTransition(currentState)
            is AdvancedIntent.ImportTheme -> StateTransition(currentState)
            is AdvancedIntent.ShowThemePicker -> StateTransition(currentState)
            is AdvancedIntent.CycleTheme -> {
                val currentIdx = currentState.availableThemes.indexOf(currentState.activeTheme)
                val nextIdx = (currentIdx + 1) % currentState.availableThemes.size
                StateTransition(currentState.copy(activeTheme = currentState.availableThemes[nextIdx]))
            }

            // ─── Enhanced Onion Skin ─────────────────────────────────────
            is AdvancedIntent.SetOnionSkinConfig -> StateTransition(
                currentState.copy(enhancedOnionSkin = intent.config)
            )
            is AdvancedIntent.SetOnionSkinMode -> StateTransition(
                currentState.copy(enhancedOnionSkin = currentState.enhancedOnionSkin.copy(mode = intent.mode))
            )
            is AdvancedIntent.SetOnionBlendMode -> StateTransition(
                currentState.copy(enhancedOnionSkin = currentState.enhancedOnionSkin.copy(blendMode = intent.mode))
            )
            is AdvancedIntent.SetOnionEffectOverlay -> StateTransition(
                currentState.copy(enhancedOnionSkin = currentState.enhancedOnionSkin.copy(effectOverlay = intent.overlay))
            )
            is AdvancedIntent.SetOnionFramesBefore -> StateTransition(
                currentState.copy(enhancedOnionSkin = currentState.enhancedOnionSkin.copy(framesBefore = intent.count.coerceIn(0, 10)))
            )
            is AdvancedIntent.SetOnionFramesAfter -> StateTransition(
                currentState.copy(enhancedOnionSkin = currentState.enhancedOnionSkin.copy(framesAfter = intent.count.coerceIn(0, 10)))
            )
            is AdvancedIntent.ToggleOnionMotionTrail -> StateTransition(
                currentState.copy(enhancedOnionSkin = currentState.enhancedOnionSkin.copy(showMotionTrail = intent.isEnabled))
            )
            is AdvancedIntent.ToggleOnionGhostFrames -> StateTransition(
                currentState.copy(enhancedOnionSkin = currentState.enhancedOnionSkin.copy(showGhostFrames = intent.isEnabled))
            )

            // ─── Layer Outlining ────────────────────────────────────────
            is AdvancedIntent.SetLayerOutlineConfig -> StateTransition(
                currentState.copy(layerOutlineConfig = intent.config)
            )
            is AdvancedIntent.ToggleLayerOutline -> StateTransition(
                currentState.copy(layerOutlineConfig = currentState.layerOutlineConfig.copy(isEnabled = intent.isEnabled))
            )
            is AdvancedIntent.SetLayerOutlineMode -> StateTransition(
                currentState.copy(layerOutlineConfig = currentState.layerOutlineConfig.copy(mode = intent.mode))
            )
            is AdvancedIntent.SetLayerOutlineColor -> StateTransition(
                currentState.copy(layerOutlineConfig = currentState.layerOutlineConfig.copy(color = intent.color))
            )
            is AdvancedIntent.SetOutlineDashPattern -> StateTransition(
                currentState.copy(layerOutlineConfig = currentState.layerOutlineConfig.copy(dashPattern = intent.pattern))
            )

            // ─── Multi-Frame Selection ──────────────────────────────────
            is AdvancedIntent.SelectFrame -> StateTransition(
                currentState.copy(multiFrameSelection = currentState.multiFrameSelection.selectFrame(intent.frameIndex))
            )
            is AdvancedIntent.DeselectFrame -> StateTransition(
                currentState.copy(multiFrameSelection = currentState.multiFrameSelection.deselectFrame(intent.frameIndex))
            )
            is AdvancedIntent.ToggleFrameSelection -> StateTransition(
                currentState.copy(multiFrameSelection = currentState.multiFrameSelection.toggleFrame(intent.frameIndex))
            )
            is AdvancedIntent.SelectFrameRange -> StateTransition(
                currentState.copy(multiFrameSelection = currentState.multiFrameSelection.selectRange(intent.start, intent.end))
            )
            is AdvancedIntent.SelectAllFrames -> StateTransition(
                currentState.copy(multiFrameSelection = currentState.multiFrameSelection.selectAll(intent.totalFrames))
            )
            is AdvancedIntent.ClearFrameSelection -> StateTransition(
                currentState.copy(multiFrameSelection = currentState.multiFrameSelection.clear())
            )
            is AdvancedIntent.ApplyBatchAction -> StateTransition(
                currentState.copy(multiFrameSelection = currentState.multiFrameSelection.copy(lastAction = intent.action))
            )

            // ─── On-Screen Modifier Keys ────────────────────────────────
            is AdvancedIntent.SetCtrlPressed -> StateTransition(
                currentState.copy(modifierKeys = currentState.modifierKeys.copy(isCtrlPressed = intent.isPressed))
            )
            is AdvancedIntent.SetShiftPressed -> StateTransition(
                currentState.copy(modifierKeys = currentState.modifierKeys.copy(isShiftPressed = intent.isPressed))
            )
            is AdvancedIntent.SetAltPressed -> StateTransition(
                currentState.copy(modifierKeys = currentState.modifierKeys.copy(isAltPressed = intent.isPressed))
            )
            is AdvancedIntent.SetMetaPressed -> StateTransition(
                currentState.copy(modifierKeys = currentState.modifierKeys.copy(isMetaPressed = intent.isPressed))
            )
            is AdvancedIntent.SetModifierButtonPosition -> StateTransition(
                currentState.copy(modifierKeys = currentState.modifierKeys.copy(buttonPosition = intent.position))
            )
            is AdvancedIntent.ToggleOnScreenModifiers -> StateTransition(
                currentState.copy(modifierKeys = currentState.modifierKeys.copy(showOnScreenButtons = intent.isVisible))
            )

            // ─── Frame Tags ─────────────────────────────────────────────
            is AdvancedIntent.AddFrameTag -> StateTransition(
                currentState.copy(frameTags = currentState.frameTags + intent.tag)
            )
            is AdvancedIntent.RemoveFrameTag -> StateTransition(
                currentState.copy(frameTags = currentState.frameTags.filter { it.id != intent.tagId })
            )
            is AdvancedIntent.RenameFrameTag -> StateTransition(
                currentState.copy(frameTags = currentState.frameTags.map {
                    if (it.id == intent.tagId) it.copy(name = intent.name) else it
                })
            )
            is AdvancedIntent.SetFrameColorCode -> StateTransition(
                currentState.copy(frameColorCodes = currentState.frameColorCodes + (intent.frameIndex to intent.color))
            )
            is AdvancedIntent.ClearFrameColorCode -> StateTransition(
                currentState.copy(frameColorCodes = currentState.frameColorCodes - intent.frameIndex)
            )

            // ─── Puppet Mesh Tools ──────────────────────────────────────
            is AdvancedIntent.GenerateMesh -> StateTransition(
                currentState.copy(meshGenConfig = intent.config)
            )
            is AdvancedIntent.SetMeshTool -> StateTransition(
                currentState.copy(meshTool = intent.tool)
            )
            is AdvancedIntent.AddMeshPin -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                val newPin = MeshPin(
                    originalX = intent.x, originalY = intent.y,
                    currentX = intent.x, currentY = intent.y,
                    type = intent.type, name = intent.name
                )
                StateTransition(currentState.copy(
                    activePuppetMesh = mesh.copy(pins = mesh.pins + newPin)
                ))
            }
            is AdvancedIntent.RemoveMeshPin -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                StateTransition(currentState.copy(
                    activePuppetMesh = mesh.copy(pins = mesh.pins.filter { it.id != intent.pinId })
                ))
            }
            is AdvancedIntent.MoveMeshPin -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                StateTransition(currentState.copy(
                    activePuppetMesh = mesh.copy(
                        pins = mesh.pins.map {
                            if (it.id == intent.pinId) it.copy(currentX = intent.x, currentY = intent.y)
                            else it
                        }
                    )
                ))
            }
            is AdvancedIntent.AddMeshBone -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                val newBone = MeshBone(
                    startPinId = intent.startPinId,
                    endPinId = intent.endPinId,
                    name = intent.name
                )
                StateTransition(currentState.copy(
                    activePuppetMesh = mesh.copy(bones = mesh.bones + newBone)
                ))
            }
            is AdvancedIntent.RemoveMeshBone -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                StateTransition(currentState.copy(
                    activePuppetMesh = mesh.copy(bones = mesh.bones.filter { it.id != intent.boneId })
                ))
            }
            is AdvancedIntent.SetMeshDensity -> StateTransition(
                currentState.copy(meshGenConfig = currentState.meshGenConfig.copy(density = intent.density))
            )
            is AdvancedIntent.LoadRigPreset -> StateTransition(currentState)
            is AdvancedIntent.SaveMeshPose -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                val pose = MeshPose(
                    name = intent.name,
                    meshId = mesh.id,
                    pinPositions = mesh.pins.associate { it.id to PinPose(it.id, it.currentX, it.currentY, it.rotation) }
                )
                StateTransition(currentState.copy(meshPoses = currentState.meshPoses + pose))
            }
            is AdvancedIntent.LoadMeshPose -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                val pose = currentState.meshPoses.find { it.id == intent.poseId } ?: return StateTransition(currentState)
                val updatedPins = mesh.pins.map { pin ->
                    pose.pinPositions[pin.id]?.let { p ->
                        pin.copy(currentX = p.x, currentY = p.y, rotation = p.rotation)
                    } ?: pin
                }
                StateTransition(currentState.copy(
                    activePuppetMesh = mesh.copy(pins = updatedPins)
                ))
            }
            is AdvancedIntent.DeleteMeshPose -> StateTransition(
                currentState.copy(meshPoses = currentState.meshPoses.filter { it.id != intent.poseId })
            )
            is AdvancedIntent.SetPinInfluenceRadius -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                StateTransition(currentState.copy(
                    activePuppetMesh = mesh.copy(
                        pins = mesh.pins.map {
                            if (it.id == intent.pinId) it.copy(influenceRadius = intent.radius)
                            else it
                        }
                    )
                ))
            }
            is AdvancedIntent.SetBoneIK -> {
                val mesh = currentState.activePuppetMesh ?: return StateTransition(currentState)
                StateTransition(currentState.copy(
                    activePuppetMesh = mesh.copy(
                        bones = mesh.bones.map {
                            if (it.id == intent.boneId) it.copy(ikEnabled = intent.enabled)
                            else it
                        }
                    )
                ))
            }
            is AdvancedIntent.ApplyRigPresetToMesh -> StateTransition(currentState)
            is AdvancedIntent.ConfirmMesh -> StateTransition(currentState)
            is AdvancedIntent.CancelMesh -> StateTransition(
                currentState.copy(activePuppetMesh = null, meshTool = MeshTool.SELECT)
            )
            is AdvancedIntent.ClearMesh -> StateTransition(
                currentState.copy(activePuppetMesh = null, meshPoses = emptyList())
            )

            // ─── Google Sign-In ─────────────────────────────────────────
            is AdvancedIntent.RequestGoogleSignIn -> StateTransition(currentState)
            is AdvancedIntent.SignOutGoogle -> StateTransition(
                currentState.copy(
                    userAccount = UserAccount(),
                    youtubeShareConfig = currentState.youtubeShareConfig.copy(isAvailable = false)
                )
            )
            is AdvancedIntent.GoogleSignInResult -> StateTransition(
                currentState.copy(
                    userAccount = currentState.userAccount.copy(
                        isSignedIn = true,
                        googleAccountId = intent.accountId,
                        displayName = intent.displayName,
                        email = intent.email,
                        profilePhotoUrl = intent.photoUrl,
                        permissions = currentState.userAccount.permissions.copy(hasGoogleSignIn = true),
                        lastLoginTimestamp = System.currentTimeMillis()
                    )
                )
            )
            is AdvancedIntent.SetYouTubeConnected -> StateTransition(
                currentState.copy(
                    userAccount = currentState.userAccount.copy(
                        youtubeConnected = intent.isConnected,
                        permissions = currentState.userAccount.permissions.copy(hasYouTubeAccess = intent.isConnected)
                    ),
                    youtubeShareConfig = currentState.youtubeShareConfig.copy(isAvailable = intent.isConnected)
                )
            )

            // ─── YouTube Sharing ────────────────────────────────────────
            is AdvancedIntent.ShareToYouTube -> StateTransition(
                currentState.copy(youtubeShareConfig = intent.config),
                listOf(DrawingEffect.ShowToast("Preparing animation for YouTube..."))
            )
            is AdvancedIntent.SetYouTubeTitle -> StateTransition(
                currentState.copy(youtubeShareConfig = currentState.youtubeShareConfig.copy(videoTitle = intent.title))
            )
            is AdvancedIntent.SetYouTubeDescription -> StateTransition(
                currentState.copy(youtubeShareConfig = currentState.youtubeShareConfig.copy(videoDescription = intent.description))
            )
            is AdvancedIntent.SetYouTubePrivacy -> StateTransition(
                currentState.copy(youtubeShareConfig = currentState.youtubeShareConfig.copy(privacy = intent.privacy))
            )
            is AdvancedIntent.SetYouTubeResolution -> StateTransition(
                currentState.copy(youtubeShareConfig = currentState.youtubeShareConfig.copy(exportResolution = intent.resolution))
            )
            is AdvancedIntent.SetYouTubeTags -> StateTransition(
                currentState.copy(youtubeShareConfig = currentState.youtubeShareConfig.copy(tags = intent.tags))
            )
            is AdvancedIntent.PrepareAnimationForYouTube -> StateTransition(
                currentState.copy(isExporting = true),
                listOf(DrawingEffect.ShowToast("Rendering animation for YouTube..."))
            )
            is AdvancedIntent.CancelYouTubeShare -> StateTransition(
                currentState.copy(isExporting = false)
            )

            // ─── Screen Overlay ─────────────────────────────────────────
            is AdvancedIntent.RequestOverlayPermission -> StateTransition(currentState)
            is AdvancedIntent.SetOverlayMode -> StateTransition(
                currentState.copy(overlayConfig = currentState.overlayConfig.copy(overlayMode = intent.mode))
            )
            is AdvancedIntent.SetOverlayConfig -> StateTransition(
                currentState.copy(overlayConfig = intent.config)
            )
            is AdvancedIntent.ToggleOverlay -> {
                val newMode = if (currentState.overlayConfig.overlayMode == OverlayMode.DISABLED)
                    OverlayMode.FLOATING_TOOLBAR else OverlayMode.DISABLED
                StateTransition(currentState.copy(
                    overlayConfig = currentState.overlayConfig.copy(overlayMode = newMode)
                ))
            }

            // ─── Crash Reporting ────────────────────────────────────────
            is AdvancedIntent.StoreCrashReport -> {
                val reports = (currentState.localCrashReports + intent.report)
                    .takeLast(50) // Keep last 50
                StateTransition(currentState.copy(localCrashReports = reports))
            }
            is AdvancedIntent.DeleteCrashReport -> StateTransition(
                currentState.copy(localCrashReports = currentState.localCrashReports.filter { it.id != intent.reportId })
            )
            is AdvancedIntent.ClearAllCrashReports -> StateTransition(
                currentState.copy(localCrashReports = emptyList())
            )
            is AdvancedIntent.AcceptCrashDataOwnership -> StateTransition(
                currentState.copy(
                    userAccount = currentState.userAccount.copy(
                        crashReportConfig = currentState.userAccount.crashReportConfig.copy(
                            crashDataOwnershipNoticeAccepted = intent.accepted
                        )
                    )
                )
            )
            is AdvancedIntent.SetCrashReportConfig -> StateTransition(
                currentState.copy(userAccount = currentState.userAccount.copy(crashReportConfig = intent.config))
            )
            is AdvancedIntent.PostCrashToGithub -> {
                val report = currentState.localCrashReports.find { it.id == intent.reportId }
                    ?: return StateTransition(currentState)
                val updated = report.copy(githubIssueUrl = "https://github.com/${currentState.userAccount.crashReportConfig.githubUsername}/${currentState.userAccount.crashReportConfig.githubRepoName}/issues/1")
                StateTransition(
                    currentState.copy(
                        localCrashReports = currentState.localCrashReports.map {
                            if (it.id == intent.reportId) updated else it
                        }
                    )
                )
            }
            is AdvancedIntent.EmailCrashReport -> {
                val report = currentState.localCrashReports.find { it.id == intent.reportId }
                    ?: return StateTransition(currentState)
                val updated = report.copy(emailSent = true, emailTimestamp = System.currentTimeMillis())
                StateTransition(
                    currentState.copy(
                        localCrashReports = currentState.localCrashReports.map {
                            if (it.id == intent.reportId) updated else it
                        }
                    )
                )
            }
            is AdvancedIntent.EditCrashReportNotes -> {
                StateTransition(
                    currentState.copy(
                        localCrashReports = currentState.localCrashReports.map {
                            if (it.id == intent.reportId) it.copy(userNotes = intent.notes) else it
                        }
                    )
                )
            }
            is AdvancedIntent.EditCrashReportSteps -> StateTransition(
                currentState.copy(
                    localCrashReports = currentState.localCrashReports.map {
                        if (it.id == intent.reportId) it.copy(stepsToReproduce = intent.steps) else it
                    }
                )
            )
            is AdvancedIntent.SetGitHubCredentials -> StateTransition(
                currentState.copy(
                    userAccount = currentState.userAccount.copy(
                        crashReportConfig = currentState.userAccount.crashReportConfig.copy(
                            githubUsername = intent.username,
                            githubRepoName = intent.repoName,
                            githubToken = intent.token
                        )
                    )
                )
            )

            // ─── Artistso.com Integration ──────────────────────────────
            is AdvancedIntent.LoadArtistsoContent -> StateTransition(currentState)
            is AdvancedIntent.SetArtistsoConfig -> StateTransition(
                currentState.copy(artistsoConfig = intent.config)
            )
            is AdvancedIntent.OpenArtistsoTutorial -> StateTransition(currentState)
            is AdvancedIntent.OpenArtistsoDemo -> StateTransition(currentState)
            is AdvancedIntent.OpenArtistsoInBrowser -> StateTransition(currentState)
            is AdvancedIntent.RefreshArtistsoFeed -> StateTransition(currentState)
            is AdvancedIntent.SearchArtistsoContent -> StateTransition(currentState)

            // ─── Original Advanced Intents (delegated below) ────────────
            else -> reduceOriginalAdvanced(currentState, intent)
        }
    }

    /**
     * Handle the original AdvancedIntent cases from v1.0
     */
    private fun reduceOriginalAdvanced(currentState: DrawingState, intent: AdvancedIntent): StateTransition<DrawingState> {
        return when (intent) {
            // Symmetry
            is AdvancedIntent.SetSymmetry -> StateTransition(currentState.copy(symmetryConfig = intent.config))
            is AdvancedIntent.ToggleSymmetry -> StateTransition(
                currentState.copy(symmetryConfig = currentState.symmetryConfig.copy(
                    isEnabled = !currentState.symmetryConfig.isEnabled,
                    type = intent.type
                ))
            )
            is AdvancedIntent.CycleSymmetryType -> {
                val types = SymmetryType.entries
                val currentIdx = types.indexOf(currentState.symmetryConfig.type)
                val nextType = types[(currentIdx + 1) % types.size]
                StateTransition(currentState.copy(symmetryConfig = currentState.symmetryConfig.copy(type = nextType)))
            }

            // Reference Images
            is AdvancedIntent.AddReferenceImage -> StateTransition(
                currentState.copy(referenceImages = currentState.referenceImages + ReferenceImage(filePath = intent.filePath, name = intent.name))
            )
            is AdvancedIntent.RemoveReferenceImage -> StateTransition(
                currentState.copy(referenceImages = currentState.referenceImages.filter { it.id != intent.id })
            )
            is AdvancedIntent.MoveReferenceImage -> StateTransition(
                currentState.copy(referenceImages = currentState.referenceImages.map {
                    if (it.id == intent.id) it.copy(offsetX = intent.x, offsetY = intent.y) else it
                })
            )
            is AdvancedIntent.ScaleReferenceImage -> StateTransition(
                currentState.copy(referenceImages = currentState.referenceImages.map {
                    if (it.id == intent.id) it.copy(scale = intent.scale) else it
                })
            )
            is AdvancedIntent.SetReferenceOpacity -> StateTransition(
                currentState.copy(referenceImages = currentState.referenceImages.map {
                    if (it.id == intent.id) it.copy(opacity = intent.opacity) else it
                })
            )
            is AdvancedIntent.ToggleReferenceVisibility -> StateTransition(
                currentState.copy(referenceImages = currentState.referenceImages.map {
                    if (it.id == intent.id) it.copy(isVisible = !it.isVisible) else it
                })
            )

            // Audio
            is AdvancedIntent.ImportAudio -> StateTransition(
                currentState.copy(audioTracks = currentState.audioTracks + AudioTrack(filePath = intent.filePath, name = intent.name))
            )
            is AdvancedIntent.RemoveAudio -> StateTransition(
                currentState.copy(audioTracks = currentState.audioTracks.filter { it.id != intent.trackId })
            )
            is AdvancedIntent.SetAudioStartFrame -> StateTransition(
                currentState.copy(audioTracks = currentState.audioTracks.map {
                    if (it.id == intent.trackId) it.copy(startFrame = intent.frame) else it
                })
            )
            is AdvancedIntent.SetAudioVolume -> StateTransition(
                currentState.copy(audioTracks = currentState.audioTracks.map {
                    if (it.id == intent.trackId) it.copy(volume = intent.volume) else it
                })
            )
            is AdvancedIntent.ToggleAudioMute -> StateTransition(
                currentState.copy(audioTracks = currentState.audioTracks.map {
                    if (it.id == intent.trackId) it.copy(isMuted = !it.isMuted) else it
                })
            )
            is AdvancedIntent.TrimAudio -> StateTransition(
                currentState.copy(audioTracks = currentState.audioTracks.map {
                    if (it.id == intent.trackId) it.copy(trimStartMs = intent.startMs, trimEndMs = intent.endMs) else it
                })
            )
            AdvancedIntent.RecordVoice, AdvancedIntent.StopRecording -> StateTransition(currentState)

            // Animation Frames
            is AdvancedIntent.SetFrameDuration, is AdvancedIntent.DuplicateFrameToPosition,
            is AdvancedIntent.CopyFrameAcrossTracks, is AdvancedIntent.LabelFrame,
            is AdvancedIntent.SetFrameAsKeyframe -> StateTransition(currentState)

            // Smart Shapes
            is AdvancedIntent.ActivateSmartShape -> StateTransition(
                currentState.copy(activeSmartShape = SmartShape(type = intent.shape))
            )
            is AdvancedIntent.ConfirmSmartShape -> StateTransition(
                currentState.copy(activeSmartShape = null, smartShapeResult = null)
            )
            is AdvancedIntent.CancelSmartShape -> StateTransition(
                currentState.copy(activeSmartShape = null, smartShapeResult = null)
            )
            AdvancedIntent.CycleSmartShape -> {
                val types = SmartShapeType.entries
                val currentType = currentState.activeSmartShape?.type ?: SmartShapeType.FREEHAND
                val nextType = types[(types.indexOf(currentType) + 1) % types.size]
                StateTransition(currentState.copy(activeSmartShape = SmartShape(type = nextType)))
            }

            // Puppet Warp
            is AdvancedIntent.StartPuppetWarp -> StateTransition(
                currentState.copy(activePuppetWarp = PuppetWarp(meshBounds = intent.selectionBounds))
            )
            is AdvancedIntent.AddWarpPin -> {
                val warp = currentState.activePuppetWarp ?: return StateTransition(currentState)
                val newPin = WarpPin(originalX = intent.x, originalY = intent.y, currentX = intent.x, currentY = intent.y)
                StateTransition(currentState.copy(activePuppetWarp = warp.copy(pins = warp.pins + newPin)))
            }
            is AdvancedIntent.MoveWarpPin -> {
                val warp = currentState.activePuppetWarp ?: return StateTransition(currentState)
                StateTransition(currentState.copy(
                    activePuppetWarp = warp.copy(pins = warp.pins.map {
                        if (it.id == intent.pinId) it.copy(currentX = intent.x, currentY = intent.y) else it
                    })
                ))
            }
            is AdvancedIntent.RemoveWarpPin -> {
                val warp = currentState.activePuppetWarp ?: return StateTransition(currentState)
                StateTransition(currentState.copy(activePuppetWarp = warp.copy(pins = warp.pins.filter { it.id != intent.pinId })))
            }
            is AdvancedIntent.SetPinFixed -> {
                val warp = currentState.activePuppetWarp ?: return StateTransition(currentState)
                StateTransition(currentState.copy(
                    activePuppetWarp = warp.copy(pins = warp.pins.map {
                        if (it.id == intent.pinId) it.copy(isFixed = intent.isFixed) else it
                    })
                ))
            }
            AdvancedIntent.ConfirmPuppetWarp -> StateTransition(currentState.copy(activePuppetWarp = null))
            AdvancedIntent.CancelPuppetWarp -> StateTransition(currentState.copy(activePuppetWarp = null))

            // Auto-Save
            is AdvancedIntent.SetAutoSaveConfig -> StateTransition(currentState.copy(autoSaveConfig = intent.config))
            AdvancedIntent.TriggerManualSave -> StateTransition(currentState.copy(isSaving = true))
            is AdvancedIntent.RecoverFromCrash, AdvancedIntent.DismissRecovery -> StateTransition(currentState)

            // Brush Patterns
            is AdvancedIntent.CreateBrushPattern, is AdvancedIntent.ImportBrushPattern,
            is AdvancedIntent.ApplyBrushPattern -> StateTransition(currentState)

            // Gestures
            is AdvancedIntent.SetGestureConfig -> StateTransition(currentState.copy(gestureConfig = intent.config))
            is AdvancedIntent.TwoFingerUndo -> StateTransition(currentState, listOf(DrawingEffect.HapticFeedback(0.3f)))
            is AdvancedIntent.ThreeFingerRedo -> StateTransition(currentState, listOf(DrawingEffect.HapticFeedback(0.3f)))
            is AdvancedIntent.LongPressEyedropper -> StateTransition(currentState)

            // Numeric Input
            is AdvancedIntent.ShowNumericInput -> StateTransition(
                currentState.copy(showNumericInput = true, numericInputTarget = intent.target)
            )
            is AdvancedIntent.ApplyNumericInput -> StateTransition(
                currentState.copy(showNumericInput = false, numericInputTarget = null)
            )
            AdvancedIntent.DismissNumericInput -> StateTransition(
                currentState.copy(showNumericInput = false, numericInputTarget = null)
            )

            // Timelapse
            is AdvancedIntent.StartTimelapse -> StateTransition(
                currentState.copy(timelapseConfig = currentState.timelapseConfig.copy(isRecording = true))
            )
            is AdvancedIntent.PauseTimelapse -> StateTransition(currentState)
            is AdvancedIntent.ResumeTimelapse -> StateTransition(currentState)
            AdvancedIntent.StopTimelapse -> StateTransition(
                currentState.copy(timelapseConfig = currentState.timelapseConfig.copy(isRecording = false))
            )
            is AdvancedIntent.ExportTimelapse -> StateTransition(currentState)

            // Extended Canvas
            is AdvancedIntent.SetExtendedCanvas -> StateTransition(currentState.copy(extendedCanvas = intent.config))
            AdvancedIntent.ToggleCanvasBounds -> {
                val ec = currentState.extendedCanvas
                if (ec != null) StateTransition(currentState.copy(extendedCanvas = ec.copy(showBounds = !ec.showBounds)))
                else StateTransition(currentState)
            }

            // Velocity
            is AdvancedIntent.SetVelocitySettings -> StateTransition(currentState.copy(velocitySettings = intent.settings))
            is AdvancedIntent.ToggleVelocity -> StateTransition(
                currentState.copy(velocitySettings = currentState.velocitySettings.copy(isEnabled = intent.isEnabled))
            )

            // Liquify
            is AdvancedIntent.SetLiquifyMode -> StateTransition(
                currentState.copy(liquifySettings = currentState.liquifySettings.copy(mode = intent.mode))
            )
            is AdvancedIntent.SetLiquifyBrushSize -> StateTransition(
                currentState.copy(liquifySettings = currentState.liquifySettings.copy(brushSize = intent.size))
            )
            is AdvancedIntent.SetLiquifyPressure -> StateTransition(
                currentState.copy(liquifySettings = currentState.liquifySettings.copy(pressure = intent.pressure))
            )
            AdvancedIntent.ConfirmLiquify -> StateTransition(currentState.copy(isLiquifying = false))
            AdvancedIntent.CancelLiquify -> StateTransition(currentState.copy(isLiquifying = false))

            // Fill
            is AdvancedIntent.SetFillMode -> StateTransition(
                currentState.copy(fillSettings = currentState.fillSettings.copy(mode = intent.mode))
            )
            is AdvancedIntent.ExecuteFill -> StateTransition(currentState)
            is AdvancedIntent.SetFillTolerance -> StateTransition(
                currentState.copy(fillSettings = currentState.fillSettings.copy(tolerance = intent.tolerance))
            )

            // Shading Assist
            is AdvancedIntent.SetShadingAssist -> StateTransition(currentState.copy(shadingAssist = intent.config))
            AdvancedIntent.ApplyShadingAssist -> StateTransition(currentState)
            is AdvancedIntent.SetShadingLightAngle -> StateTransition(
                currentState.copy(shadingAssist = currentState.shadingAssist.copy(lightAngle = intent.angle))
            )

            // Color Match
            is AdvancedIntent.SetColorMatchReference -> StateTransition(currentState)
            is AdvancedIntent.ApplyColorMatch -> StateTransition(currentState)

            // Floating Panels
            is AdvancedIntent.ShowFloatingPanel -> {
                val newPanel = FloatingPanel(type = intent.type)
                StateTransition(currentState.copy(floatingPanels = currentState.floatingPanels + newPanel))
            }
            is AdvancedIntent.MoveFloatingPanel -> StateTransition(
                currentState.copy(floatingPanels = currentState.floatingPanels.map {
                    if (it.id == intent.panelId) it.copy(x = intent.x, y = intent.y) else it
                })
            )
            is AdvancedIntent.ResizeFloatingPanel -> StateTransition(
                currentState.copy(floatingPanels = currentState.floatingPanels.map {
                    if (it.id == intent.panelId) it.copy(width = intent.width, height = intent.height) else it
                })
            )
            is AdvancedIntent.TogglePanelCollapsed -> StateTransition(
                currentState.copy(floatingPanels = currentState.floatingPanels.map {
                    if (it.id == intent.panelId) it.copy(isCollapsed = !it.isCollapsed) else it
                })
            )
            AdvancedIntent.HideAllFloatingPanels -> StateTransition(
                currentState.copy(floatingPanels = emptyList())
            )

            else -> StateTransition(currentState)
        }
    }
}
