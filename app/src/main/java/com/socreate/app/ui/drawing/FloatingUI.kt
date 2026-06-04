package com.socreate.app.ui.drawing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.socreate.app.core.model.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Floating UI System - All UI elements float above canvas
 * No toolbars, no backgrounds, fully draggable
 */
@Composable
fun FloatingUI(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Floating action buttons
    FloatingButton(
        icon = Icons.Default.Brush,
        label = "Brushes",
        key = "brushes",
        offset = state.panels.brushes,
        isActive = state.panels.brushes.isOpen,
        onClick = {
            onStateChange(state.copy(
                panels = state.panels.copy(
                    brushes = state.panels.brushes.copy(isOpen = !state.panels.brushes.isOpen)
                )
            ))
        },
        onOffsetChange = { newOffset ->
            onStateChange(state.copy(
                panels = state.panels.copy(
                    brushes = state.panels.brushes.copy(offsetX = newOffset.x, offsetY = newOffset.y)
                )
            ))
        }
    )
    
    FloatingButton(
        icon = Icons.Default.Layers,
        label = "Layers",
        key = "layers",
        offset = state.panels.layers,
        isActive = state.panels.layers.isOpen,
        onClick = {
            onStateChange(state.copy(
                panels = state.panels.copy(
                    layers = state.panels.layers.copy(isOpen = !state.panels.layers.isOpen)
                )
            ))
        },
        onOffsetChange = { newOffset ->
            onStateChange(state.copy(
                panels = state.panels.copy(
                    layers = state.panels.layers.copy(offsetX = newOffset.x, offsetY = newOffset.y)
                )
            ))
        }
    )
    
    FloatingButton(
        icon = Icons.Default.Movie,
        label = "Timeline",
        key = "timeline",
        offset = state.panels.timeline,
        isActive = state.panels.timeline.isOpen,
        onClick = {
            onStateChange(state.copy(
                panels = state.panels.copy(
                    timeline = state.panels.timeline.copy(isOpen = !state.panels.timeline.isOpen)
                )
            ))
        },
        onOffsetChange = { newOffset ->
            onStateChange(state.copy(
                panels = state.panels.copy(
                    timeline = state.panels.timeline.copy(offsetX = newOffset.x, offsetY = newOffset.y)
                )
            ))
        }
    )
    
    FloatingButton(
        icon = Icons.Default.Folder,
        label = "Gallery",
        key = "gallery",
        offset = state.panels.gallery,
        isActive = state.panels.gallery.isOpen,
        onClick = {
            onStateChange(state.copy(
                panels = state.panels.copy(
                    gallery = state.panels.gallery.copy(isOpen = !state.panels.gallery.isOpen)
                )
            ))
        },
        onOffsetChange = { newOffset ->
            onStateChange(state.copy(
                panels = state.panels.copy(
                    gallery = state.panels.gallery.copy(offsetX = newOffset.x, offsetY = newOffset.y)
                )
            ))
        }
    )
    
    FloatingButton(
        icon = Icons.Default.Settings,
        label = "Settings",
        key = "settings",
        offset = state.panels.settings,
        isActive = state.panels.settings.isOpen,
        onClick = {
            onStateChange(state.copy(
                panels = state.panels.copy(
                    settings = state.panels.settings.copy(isOpen = !state.panels.settings.isOpen)
                )
            ))
        },
        onOffsetChange = { newOffset ->
            onStateChange(state.copy(
                panels = state.panels.copy(
                    settings = state.panels.settings.copy(offsetX = newOffset.x, offsetY = newOffset.y)
                )
            ))
        }
    )
    
    FloatingButton(
        icon = Icons.Default.Visibility,
        label = "Onion",
        key = "onion",
        offset = state.panels.onionSkin,
        isActive = state.panels.onionSkin.isOpen,
        onClick = {
            onStateChange(state.copy(
                panels = state.panels.copy(
                    onionSkin = state.panels.onionSkin.copy(isOpen = !state.panels.onionSkin.isOpen)
                )
            ))
        },
        onOffsetChange = { newOffset ->
            onStateChange(state.copy(
                panels = state.panels.copy(
                    onionSkin = state.panels.onionSkin.copy(offsetX = newOffset.x, offsetY = newOffset.y)
                )
            ))
        }
    )
    
    FloatingButton(
        icon = Icons.Default.Repeat,
        label = "Symmetry",
        key = "symmetry",
        offset = state.panels.symmetry,
        isActive = state.panels.symmetry.isOpen,
        onClick = {
            onStateChange(state.copy(
                panels = state.panels.copy(
                    symmetry = state.panels.symmetry.copy(isOpen = !state.panels.symmetry.isOpen)
                )
            ))
        },
        onOffsetChange = { newOffset ->
            onStateChange(state.copy(
                panels = state.panels.copy(
                    symmetry = state.panels.symmetry.copy(offsetX = newOffset.x, offsetY = newOffset.y)
                )
            ))
        }
    )
    
    // Action buttons
    FloatingActionButton(
        onClick = { /* Undo */ },
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(12.dp),
        containerColor = Color(0xFF333333).copy(alpha = 0.85f),
        contentColor = Color.White
    ) {
        Icon(Icons.Default.Undo, "Undo", modifier = Modifier.padding(12.dp))
    }
    
    FloatingActionButton(
        onClick = { /* Redo */ },
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(12.dp)
            .offset(y = (-70).dp),
        containerColor = Color(0xFF333333).copy(alpha = 0.85f),
        contentColor = Color.White
    ) {
        Icon(Icons.Default.Redo, "Redo", modifier = Modifier.padding(12.dp))
    }
    
    FloatingActionButton(
        onClick = { /* New Canvas */ },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp),
        containerColor = Color(0xFFFF6B35),
        contentColor = Color.White
    ) {
        Icon(Icons.Default.Add, "New Canvas", modifier = Modifier.padding(12.dp))
    }
    
    // Panels (open on top of everything)
    PanelContainer(
        state = state,
        onStateChange = onStateChange
    )
}

/**
 * Draggable floating button
 */
@Composable
fun FloatingButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    key: String,
    offset: PanelVisibility,
    isActive: Boolean,
    onClick: () -> Unit,
    onOffsetChange: (Offset) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset(offset.offsetX, offset.offsetY)) }
    
    Box(
        modifier = Modifier
            .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                        dragOffset = Offset(offset.offsetX, offset.offsetY)
                    },
                    onDrag = { change, dragAmount ->
                        dragOffset += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        isDragging = false
                        onOffsetChange(dragOffset)
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = Offset(offset.offsetX, offset.offsetY)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (!isDragging) onClick() }
                )
            }
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = if (isActive) Color(0xFFFF6B35) else Color(0xFF333333).copy(alpha = 0.85f),
            contentColor = Color.White,
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(icon, label, modifier = Modifier.size(22.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 8.dp)
            }
        }
    }
}

/**
 * Panel container - holds all floating panels
 */
@Composable
fun PanelContainer(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    // Brushes Panel
    AnimatedVisibility(
        visible = state.panels.brushes.isOpen,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingPanel(
            key = "brushes",
            title = "Brushes",
            onClose = {
                onStateChange(state.copy(
                    panels = state.panels.copy(
                        brushes = state.panels.brushes.copy(isOpen = false)
                    )
                ))
            },
            defaultOffset = Offset(120f, 100f)
        ) {
            BrushPanelContent(
                state = state,
                onStateChange = onStateChange
            )
        }
    }
    
    // Layers Panel
    AnimatedVisibility(
        visible = state.panels.layers.isOpen,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingPanel(
            key = "layers",
            title = "Layers",
            onClose = {
                onStateChange(state.copy(
                    panels = state.panels.copy(
                        layers = state.panels.layers.copy(isOpen = false)
                    )
                ))
            },
            defaultOffset = Offset(120f, 250f)
        ) {
            LayersPanelContent(
                state = state,
                onStateChange = onStateChange
            )
        }
    }
    
    // Timeline Panel
    AnimatedVisibility(
        visible = state.panels.timeline.isOpen,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingPanel(
            key = "timeline",
            title = "Timeline",
            onClose = {
                onStateChange(state.copy(
                    panels = state.panels.copy(
                        timeline = state.panels.timeline.copy(isOpen = false)
                    )
                ))
            },
            defaultOffset = Offset(200f, 500f)
        ) {
            TimelinePanelContent(
                state = state,
                onStateChange = onStateChange
            )
        }
    }
    
    // Gallery Panel
    AnimatedVisibility(
        visible = state.panels.gallery.isOpen,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingPanel(
            key = "gallery",
            title = "Gallery",
            onClose = {
                onStateChange(state.copy(
                    panels = state.panels.copy(
                        gallery = state.panels.gallery.copy(isOpen = false)
                    )
                ))
            },
            defaultOffset = Offset(300f, 100f)
        ) {
            GalleryPanelContent(
                state = state,
                onStateChange = onStateChange
            )
        }
    }
    
    // Settings Panel
    AnimatedVisibility(
        visible = state.panels.settings.isOpen,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingPanel(
            key = "settings",
            title = "Settings",
            onClose = {
                onStateChange(state.copy(
                    panels = state.panels.copy(
                        settings = state.panels.settings.copy(isOpen = false)
                    )
                ))
            },
            defaultOffset = Offset(400f, 100f)
        ) {
            SettingsPanelContent(
                state = state,
                onStateChange = onStateChange
            )
        }
    }
    
    // Onion Skin Panel
    AnimatedVisibility(
        visible = state.panels.onionSkin.isOpen,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingPanel(
            key = "onion",
            title = "Onion Skinning",
            onClose = {
                onStateChange(state.copy(
                    panels = state.panels.copy(
                        onionSkin = state.panels.onionSkin.copy(isOpen = false)
                    )
                ))
            },
            defaultOffset = Offset(500f, 150f)
        ) {
            OnionSkinPanelContent(
                state = state,
                onStateChange = onStateChange
            )
        }
    }
    
    // Symmetry Panel
    AnimatedVisibility(
        visible = state.panels.symmetry.isOpen,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingPanel(
            key = "symmetry",
            title = "Symmetry",
            onClose = {
                onStateChange(state.copy(
                    panels = state.panels.copy(
                        symmetry = state.panels.symmetry.copy(isOpen = false)
                    )
                ))
            },
            defaultOffset = Offset(600f, 200f)
        ) {
            SymmetryPanelContent(
                state = state,
                onStateChange = onStateChange
            )
        }
    }
}

/**
 * Base floating panel with drag support
 */
@Composable
fun FloatingPanel(
    key: String,
    title: String,
    onClose: () -> Unit,
    defaultOffset: Offset,
    width: Dp = 280.dp,
    maxHeight: Dp = 500.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    var panelOffset by remember { mutableStateOf(defaultOffset) }
    var isDragging by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .offset { IntOffset(panelOffset.x.roundToInt(), panelOffset.y.roundToInt()) }
            .width(width)
            .heightIn(max = maxHeight)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        panelOffset += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        isDragging = false
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A).copy(alpha = 0.95f)
        )
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White.copy(alpha = 0.7f))
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun BrushPanelContent(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Brush Type", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
        }
        items(BrushType.values()) { brushType ->
            BrushTypeItem(
                type = brushType,
                isActive = state.brush.type == brushType,
                onClick = {
                    onStateChange(state.copy(
                        brush = state.brush.copy(type = brushType)
                    ))
                }
            )
        }
        item {
            SliderSetting(
                label = "Size",
                value = state.brush.size,
                onValueChange = { 
                    onStateChange(state.copy(
                        brush = state.brush.copy(size = it)
                    ))
                },
                range = 1f..100f
            )
        }
        item {
            SliderSetting(
                label = "Opacity",
                value = state.brush.opacity,
                onValueChange = { 
                    onStateChange(state.copy(
                        brush = state.brush.copy(opacity = it)
                    ))
                },
                range = 0f..100f
            )
        }
        item {
            SliderSetting(
                label = "Hardness",
                value = state.brush.hardness,
                onValueChange = { 
                    onStateChange(state.copy(
                        brush = state.brush.copy(hardness = it)
                    ))
                },
                range = 0f..100f
            )
        }
    }
}

@Composable
fun BrushTypeItem(
    type: BrushType,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFFFF6B35).copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = type.name.replace('_', ' '),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) Color(0xFFFF6B35) else Color.White
        )
    }
}

@Composable
fun LayersPanelContent(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Layers", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            IconButton(
                onClick = {
                    val newLayer = LayerState(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Layer ${state.layers.size + 1}",
                        color = Color(0xFF4ECDC4)
                    )
                    onStateChange(state.copy(
                        layers = state.layers + newLayer,
                        activeLayerId = newLayer.id
                    ))
                }
            ) {
                Icon(Icons.Default.Add, "Add Layer", tint = Color.White)
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.layers) { layer ->
                LayerItem(
                    layer = layer,
                    isActive = layer.id == state.activeLayerId,
                    onSelect = { onStateChange(state.copy(activeLayerId = layer.id)) },
                    onToggleVisibility = {
                        onStateChange(state.copy(
                            layers = state.layers.map { l ->
                                if (l.id == layer.id) l.copy(isVisible = !l.isVisible) else l
                            }
                        ))
                    }
                )
            }
        }
    }
}

@Composable
fun LayerItem(
    layer: LayerState,
    isActive: Boolean,
    onSelect: () -> Unit,
    onToggleVisibility: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFFFF6B35).copy(alpha = 0.15f) else Color(0xFF333333).copy(alpha = 0.5f))
            .clickable(onClick = onSelect)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color swatch
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(layer.color),
        ) {}
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(layer.name, style = MaterialTheme.typography.bodySmall, color = Color.White)
            Text("${layer.opacity.toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
        }
        
        IconButton(onClick = onToggleVisibility) {
            Icon(
                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle visibility",
                tint = if (layer.isVisible) Color(0xFF4ECDC4) else Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun TimelinePanelContent(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    Column {
        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        onStateChange(state.copy(
                            timeline = state.timeline.copy(isPlaying = !state.timeline.isPlaying)
                        ))
                    }
                ) {
                    Icon(
                        imageVector = if (state.timeline.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* Stop */ }) {
                    Icon(Icons.Default.Stop, "Stop", tint = Color.White)
                }
                IconButton(onClick = { /* Add frame */ }) {
                    Icon(Icons.Default.Add, "Add Frame", tint = Color.White)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FPS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(4.dp))
                var fpsText by remember { mutableStateOf(state.timeline.fps.toString()) }
                OutlinedTextField(
                    value = fpsText,
                    onValueChange = { 
                        fpsText = it
                        it.toIntOrNull()?.let { fps ->
                            onStateChange(state.copy(
                                timeline = state.timeline.copy(fps = fps.coerceIn(1, 60))
                            ))
                        }
                    },
                    modifier = Modifier.width(60.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
        
        // Frames
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.timeline.frames.size) { index ->
                FrameItem(
                    index = index,
                    isActive = index == state.timeline.currentFrameIndex,
                    isKeyframe = state.timeline.frames.getOrNull(index)?.isKeyframe ?: false,
                    onClick = {
                        onStateChange(state.copy(
                            timeline = state.timeline.copy(currentFrameIndex = index)
                        ))
                    }
                )
            }
        }
    }
}

@Composable
fun FrameItem(
    index: Int,
    isActive: Boolean,
    isKeyframe: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp, 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isActive) Color(0xFF4ECDC4).copy(alpha = 0.3f)
                else Color(0xFF333333).copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) Color(0xFF4ECDC4) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f)
        )
        if (isKeyframe) {
            Icon(
                Icons.Default.Key,
                contentDescription = "Keyframe",
                tint = Color(0xFFFFD700),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(10.dp)
            )
        }
    }
}

@Composable
fun GalleryPanelContent(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    Column {
        Button(
            onClick = { /* New Canvas */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
        ) {
            Text("+ New Canvas")
        }
        
        if (state.gallery.isEmpty()) {
            Text(
                "No saved canvases",
                modifier = Modifier.padding(16.dp),
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.gallery) { canvas ->
                    GalleryItem(
                        metadata = canvas,
                        onClick = { /* Open canvas */ }
                    )
                }
            }
        }
    }
}

@Composable
fun GalleryItem(
    metadata: CanvasMetadata,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333).copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF444444)),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(metadata.name, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                Text(
                    "${metadata.width}×${metadata.height} • ${metadata.fps}fps • ${metadata.layerCount}L",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun SettingsPanelContent(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsGroup("Input") {
            SettingToggle(
                label = "Palm Rejection",
                checked = state.settings.palmRejection,
                onCheckedChange = {
                    onStateChange(state.copy(
                        settings = state.settings.copy(palmRejection = it)
                    ))
                }
            )
            SettingToggle(
                label = "Stylus Only",
                checked = state.settings.stylusOnly,
                onCheckedChange = {
                    onStateChange(state.copy(
                        settings = state.settings.copy(stylusOnly = it)
                    ))
                }
            )
            SettingToggle(
                label = "Low Latency",
                checked = state.settings.lowLatency,
                onCheckedChange = {
                    onStateChange(state.copy(
                        settings = state.settings.copy(lowLatency = it)
                    ))
                }
            )
        }
        
        SettingsGroup("Export") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { /* Export PNG */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) { Text("PNG", fontSize = 11.dp) }
                Button(
                    onClick = { /* Export JPEG */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) { Text("JPEG", fontSize = 11.dp) }
                Button(
                    onClick = { /* Export GIF */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) { Text("GIF", fontSize = 11.dp) }
            }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@Composable
fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFF6B35),
                uncheckedThumbColor = Color.White.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun OnionSkinPanelContent(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderSetting(
            label = "Previous Frames",
            value = state.onionSkin.previousFrames.toFloat(),
            onValueChange = {
                onStateChange(state.copy(
                    onionSkin = state.onionSkin.copy(previousFrames = it.toInt())
                ))
            },
            range = 0f..10f
        )
        SliderSetting(
            label = "Next Frames",
            value = state.onionSkin.nextFrames.toFloat(),
            onValueChange = {
                onStateChange(state.copy(
                    onionSkin = state.onionSkin.copy(nextFrames = it.toInt())
                ))
            },
            range = 0f..10f
        )
        SliderSetting(
            label = "Opacity",
            value = state.onionSkin.opacity,
            onValueChange = {
                onStateChange(state.copy(
                    onionSkin = state.onionSkin.copy(opacity = it)
                ))
            },
            range = 0f..100f
        )
    }
}

@Composable
fun SymmetryPanelContent(
    state: DrawingState,
    onStateChange: (DrawingState) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SymmetryMode.values().forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (state.symmetry == mode) Color(0xFF9B59B6).copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable {
                        onStateChange(state.copy(symmetry = mode))
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    mode.name.replace('_', ' '),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.symmetry == mode) Color(0xFF9B59B6) else Color.White
                )
            }
        }
    }
}

@Composable
fun SliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            Text(
                value.toInt().toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF6B35),
                activeTrackColor = Color(0xFFFF6B35),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}
