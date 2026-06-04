package com.socreate.app.ui.drawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.socreate.app.core.model.DrawingState
import com.socreate.app.ui.theme.SoCreateTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Canvas-first design
 * No back button, no toolbar backgrounds
 * Everything floats above the canvas
 */
@AndroidEntryPoint
class SoCreateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            SoCreateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SoCreateScaffold()
                }
            }
        }
    }
}

/**
 * Main scaffold - canvas fills entire screen, UI floats above
 */
@androidx.compose.runtime.Composable
fun SoCreateScaffold() {
    var state by androidx.compose.runtime.remember { mutableStateOf(DrawingState.initial()) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color.White)
    ) {
        // Canvas takes full screen
        DrawingCanvas(
            modifier = Modifier.fillMaxSize(),
            state = state
        )
        
        // Floating UI overlay
        FloatingUI(
            state = state,
            onStateChange = { state = it }
        )
    }
}
