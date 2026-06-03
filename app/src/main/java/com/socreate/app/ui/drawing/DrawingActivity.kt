package com.socreate.app.ui.drawing

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.socreate.app.R
import com.socreate.app.core.model.*
import com.socreate.app.engine.canvas.CanvasRenderer
import kotlinx.coroutines.launch

/**
 * Main drawing Activity — optimized for Samsung Galaxy Tab S10+.
 *
 * Tab S10+ specific features:
 * - 120Hz rendering with Choreographer frame scheduling
 * - Full S Pen integration (pressure, tilt, hover, eraser, button)
 * - Motion prediction for zero-latency drawing
 * - Display P3 wide gamut on AMOLED
 * - Samsung DeX seamless window resize
 * - Edge-to-edge with display cutout safe areas
 * - IP68 — app works in wet conditions with S Pen
 *
 * Layout: Full-screen immersive canvas with floating tool panels.
 */
class DrawingActivity : AppCompatActivity() {

    private lateinit var viewModel: DrawingViewModel
    private lateinit var canvasView: DrawingCanvasView
    private lateinit var canvasRenderer: CanvasRenderer

    // Toolbar
    private lateinit var toolBar: LinearLayout
    private lateinit var btnBrush: ImageButton
    private lateinit var btnEraser: ImageButton
    private lateinit var btnSmudge: ImageButton
    private lateinit var btnSelect: ImageButton
    private lateinit var btnEyedropper: ImageButton
    private lateinit var btnLayers: ImageButton
    private lateinit var btnUndo: ImageButton
    private lateinit var btnRedo: ImageButton
    private lateinit var btnBrushPicker: ImageButton
    private lateinit var btnColorPicker: View
    private lateinit var btnSettings: ImageButton

    // Sliders
    private lateinit var sliderBar: LinearLayout
    private lateinit var sizeSlider: SeekBar
    private lateinit var opacitySlider: SeekBar
    private lateinit var sizeLabel: TextView
    private lateinit var opacityLabel: TextView

    // Panels
    private lateinit var brushPickerPanel: FrameLayout
    private lateinit var colorPickerPanel: FrameLayout
    private lateinit var layerPanel: FrameLayout
    private lateinit var quickMenuPanel: FrameLayout

    // System UI
    private var isFullscreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableImmersiveMode()
        enableEdgeToEdge()

        // Get canvas size from intent (or default to Tab S10+ native)
        val canvasWidth = intent.getIntExtra("canvas_width", TabS10Plus.SCREEN_WIDTH)
        val canvasHeight = intent.getIntExtra("canvas_height", TabS10Plus.SCREEN_HEIGHT)

        setContentView(R.layout.activity_drawing)

        viewModel = DrawingViewModel(application)

        initViews()
        initCanvas(canvasWidth, canvasHeight)
        initToolbar()
        initSliders()
        initPanels()
        observeState()
    }

    private fun initViews() {
        canvasView = findViewById(R.id.canvasView)
        toolBar = findViewById(R.id.toolBar)
        sliderBar = findViewById(R.id.sliderBar)

        btnBrush = findViewById(R.id.btnBrush)
        btnEraser = findViewById(R.id.btnEraser)
        btnSmudge = findViewById(R.id.btnSmudge)
        btnSelect = findViewById(R.id.btnSelect)
        btnEyedropper = findViewById(R.id.btnEyedropper)
        btnLayers = findViewById(R.id.btnLayers)
        btnUndo = findViewById(R.id.btnUndo)
        btnRedo = findViewById(R.id.btnRedo)
        btnBrushPicker = findViewById(R.id.btnBrushPicker)
        btnColorPicker = findViewById(R.id.colorPreview)
        btnSettings = findViewById(R.id.btnSettings)

        sizeSlider = findViewById(R.id.sizeSlider)
        opacitySlider = findViewById(R.id.opacitySlider)
        sizeLabel = findViewById(R.id.sizeLabel)
        opacityLabel = findViewById(R.id.opacityLabel)

        brushPickerPanel = findViewById(R.id.brushPickerPanel)
        colorPickerPanel = findViewById(R.id.colorPickerPanel)
        layerPanel = findViewById(R.id.layerPanel)
        quickMenuPanel = findViewById(R.id.quickMenuPanel)
    }

    private fun initCanvas(width: Int, height: Int) {
        val canvas = Canvas(
            width = width,
            height = height,
            dpi = TabS10Plus.DPI,
            colorProfile = ColorProfile.DISPLAY_P3
        )

        canvasRenderer = CanvasRenderer(this, canvas)
        canvasView.setRenderer(canvasRenderer)
        canvasView.onIntent = { intent ->
            viewModel.acceptIntent(intent)
        }
    }

    private fun initToolbar() {
        btnBrush.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.BRUSH))
            updateToolSelection(DrawingTool.BRUSH)
        }
        btnEraser.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.ERASER))
            updateToolSelection(DrawingTool.ERASER)
        }
        btnSmudge.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.SMUDGE))
            updateToolSelection(DrawingTool.SMUDGE)
        }
        btnSelect.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.SELECT))
            updateToolSelection(DrawingTool.SELECT)
        }
        btnEyedropper.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.EYEDROPPER))
            updateToolSelection(DrawingTool.EYEDROPPER)
        }
        btnLayers.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.ToggleLayerPanel)
        }
        btnUndo.setOnClickListener { viewModel.acceptIntent(DrawingIntent.Undo) }
        btnRedo.setOnClickListener { viewModel.acceptIntent(DrawingIntent.Redo) }
        btnBrushPicker.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.ToggleBrushPicker)
        }
        btnColorPicker.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.ToggleColorPicker)
        }
    }

    private fun initSliders() {
        sizeSlider.max = 500
        sizeSlider.progress = 10
        sizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.acceptIntent(DrawingIntent.SetBrushSize(progress.toFloat()))
                    sizeLabel.text = "${progress}px"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        opacitySlider.max = 100
        opacitySlider.progress = 100
        opacitySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.acceptIntent(DrawingIntent.SetBrushOpacity(progress / 100f))
                    opacityLabel.text = "$progress%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initPanels() {
        canvasView.setOnClickListener {
            viewModel.acceptIntent(DrawingIntent.HideAllPanels)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state -> canvasView.setState(state); updateUI(state) }
        }
        lifecycleScope.launch {
            viewModel.effects.collect { effect -> handleEffect(effect) }
        }
    }

    private fun updateUI(state: DrawingState) {
        btnUndo.isEnabled = state.canUndo
        btnRedo.isEnabled = state.canRedo
        btnUndo.alpha = if (state.canUndo) 1f else 0.3f
        btnRedo.alpha = if (state.canRedo) 1f else 0.3f

        state.activeColor.toArgb().let { color ->
            (btnColorPicker.background as? GradientDrawable)?.setColor(color)
        }

        if (!sizeSlider.isPressed) {
            sizeSlider.progress = state.brushSize.toInt()
            sizeLabel.text = "${state.brushSize.toInt()}px"
        }
        if (!opacitySlider.isPressed) {
            opacitySlider.progress = (state.brushOpacity * 100).toInt()
            opacityLabel.text = "${(state.brushOpacity * 100).toInt()}%"
        }

        updateToolSelection(state.activeTool)

        brushPickerPanel.visibility = if (state.showBrushPicker) View.VISIBLE else View.GONE
        colorPickerPanel.visibility = if (state.showColorPicker) View.VISIBLE else View.GONE
        layerPanel.visibility = if (state.showLayerPanel) View.VISIBLE else View.GONE

        // Update hover cursor size for S Pen preview
        canvasView.hoverCursorRadius = state.brushSize * state.zoom / 2f
    }

    private fun updateToolSelection(tool: DrawingTool) {
        btnBrush.alpha = if (tool == DrawingTool.BRUSH) 1f else 0.5f
        btnEraser.alpha = if (tool == DrawingTool.ERASER) 1f else 0.5f
        btnSmudge.alpha = if (tool == DrawingTool.SMUDGE) 1f else 0.5f
        btnSelect.alpha = if (tool == DrawingTool.SELECT) 1f else 0.5f
        btnEyedropper.alpha = if (tool == DrawingTool.EYEDROPPER) 1f else 0.5f
    }

    private fun handleEffect(effect: DrawingEffect) {
        when (effect) {
            is DrawingEffect.ShowToast -> Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            is DrawingEffect.ProjectSaved -> Toast.makeText(this, "Project saved", Toast.LENGTH_SHORT).show()
            is DrawingEffect.HapticFeedback -> canvasView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            else -> {}
        }
    }

    // ─── System UI (Tab S10+ Optimized) ─────────────────────────────────────

    /**
     * Enable full immersive mode for Tab S10+.
     * Hides status bar and navigation bar for maximum canvas area.
     */
    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
    }

    /**
     * Enable edge-to-edge rendering on Tab S10+.
     * Content draws behind system bars and into display cutout area.
     */
    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Draw into the camera cutout area (Tab S10+ landscape)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }

        // Make system bars transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    private fun disableImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
    }

    // ─── Keyboard Shortcuts (DeX + Physical Keyboard) ───────────────────────

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_Z -> {
                if (event.isCtrlPressed && event.isShiftPressed) {
                    viewModel.acceptIntent(DrawingIntent.Redo); true
                } else if (event.isCtrlPressed) {
                    viewModel.acceptIntent(DrawingIntent.Undo); true
                } else false
            }
            KeyEvent.KEYCODE_B -> { viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.BRUSH)); true }
            KeyEvent.KEYCODE_E -> { viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.ERASER)); true }
            KeyEvent.KEYCODE_L -> { viewModel.acceptIntent(DrawingIntent.ToggleLayerPanel); true }
            KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_EQUALS -> {
                if (event.isCtrlPressed) { viewModel.acceptIntent(DrawingIntent.ZoomIn); true } else false
            }
            KeyEvent.KEYCODE_MINUS -> {
                if (event.isCtrlPressed) { viewModel.acceptIntent(DrawingIntent.ZoomOut); true } else false
            }
            KeyEvent.KEYCODE_0 -> {
                if (event.isCtrlPressed) { viewModel.acceptIntent(DrawingIntent.ResetZoom); true } else false
            }
            KeyEvent.KEYCODE_S -> {
                if (event.isCtrlPressed) { viewModel.acceptIntent(DrawingIntent.SaveProject); true } else false
            }
            // S Pen button shortcut — toggle eraser while held
            KeyEvent.KEYCODE_BUTTON_2 -> {
                viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.ERASER))
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        // S Pen button released — restore brush
        if (keyCode == KeyEvent.KEYCODE_BUTTON_2) {
            viewModel.acceptIntent(DrawingIntent.SelectTool(DrawingTool.BRUSH))
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isFullscreen) enableImmersiveMode()
    }

    /**
     * Handle Samsung DeX configuration changes without restarting.
     */
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // DeX mode switch or window resize — re-initialize canvas viewport
        canvasRenderer.release()
        canvasView.setRenderer(canvasRenderer)
    }
}
