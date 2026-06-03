package com.socreate.app.ui.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.socreate.app.core.model.ModifierButtonPosition
import com.socreate.app.core.model.ModifierKeyState

/**
 * On-screen modifier key buttons (Ctrl, Shift, Alt/Cmd).
 *
 * Inspired by Aseprite/Resprite — allows artists using tablets without
 * physical keyboards to access multi-select and batch editing features.
 *
 * Touch behavior:
 * - Tap to toggle on/off
 * - Hold and drag to apply temporarily (release = off)
 * - Visual feedback with highlight when active
 *
 * Layout: Three buttons in a row (Ctrl | Shift | Alt)
 * Position: Configurable via ModifierButtonPosition
 */
class ModifierButtonBar(
    context: Context,
    private var onModifierChanged: (ModifierKeyState) -> Unit
) : View(context) {

    private var state = ModifierKeyState()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val activeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f * resources.displayMetrics.density
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val buttonRects = mutableListOf<RectF>()
    private val buttonLabels = listOf("Ctrl", "Shift", "Alt")
    private val buttonStates = mutableListOf(false, false, false)

    private var buttonWidth = 44f * resources.displayMetrics.density
    private var buttonHeight = 36f * resources.displayMetrics.density
    private var buttonSpacing = 4f * resources.displayMetrics.density
    private var cornerRadius = 8f * resources.displayMetrics.density

    // Colors from theme
    private var bgColor = 0x2A2640E0.toInt()
    private var activeColor = 0xE6E94560.toInt()
    private var textColor = 0xFFFFFFFF.toInt()
    private var activeTextColor = 0xFFFFFFFF.toInt()

    init {
        updateButtonRects()
    }

    private fun updateButtonRects() {
        buttonRects.clear()
        var left = paddingLeft.toFloat()
        for (i in buttonLabels.indices) {
            buttonRects.add(RectF(
                left,
                paddingTop.toFloat(),
                left + buttonWidth,
                paddingTop + buttonHeight
            ))
            left += buttonWidth + buttonSpacing
        }

        val totalWidth = (buttonWidth * buttonLabels.size + buttonSpacing * (buttonLabels.size - 1) + paddingLeft + paddingRight).toInt()
        val totalHeight = (buttonHeight + paddingTop + paddingBottom).toInt()
        setMeasuredDimension(totalWidth, totalHeight)
    }

    fun setThemeColors(bg: Int, activeBg: Int, text: Int, activeText: Int) {
        bgColor = bg
        activeColor = activeBg
        textColor = text
        activeTextColor = activeText
        invalidate()
    }

    fun setState(newState: ModifierKeyState) {
        state = newState
        buttonStates[0] = state.isCtrlPressed
        buttonStates[1] = state.isShiftPressed
        buttonStates[2] = state.isAltPressed
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth = (buttonWidth * buttonLabels.size +
                buttonSpacing * (buttonLabels.size - 1) +
                paddingLeft + paddingRight).toInt()
        val totalHeight = (buttonHeight + paddingTop + paddingBottom).toInt()
        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!state.showOnScreenButtons) return

        for (i in buttonRects.indices) {
            val rect = buttonRects[i]
            val isActive = buttonStates[i]

            bgPaint.color = if (isActive) activeColor else bgColor
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

            textPaint.color = if (isActive) activeTextColor else textColor
            val textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(buttonLabels[i], rect.centerX(), textY, textPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.x
                val y = event.y
                for (i in buttonRects.indices) {
                    if (buttonRects[i].contains(x, y)) {
                        buttonStates[i] = !buttonStates[i]
                        val newState = state.copy(
                            isCtrlPressed = buttonStates[0],
                            isShiftPressed = buttonStates[1],
                            isAltPressed = buttonStates[2]
                        )
                        state = newState
                        onModifierChanged(newState)
                        invalidate()
                        performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
