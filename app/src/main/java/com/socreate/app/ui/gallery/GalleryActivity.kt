package com.socreate.app.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.socreate.app.R
import com.socreate.app.core.model.Canvas
import com.socreate.app.core.model.TabS10Plus
import com.socreate.app.ui.drawing.DrawingActivity

/**
 * Gallery Activity — optimized for Samsung Galaxy Tab S10+.
 *
 * Shows recent projects and canvas presets tuned for the Tab S10+ display.
 */
class GalleryActivity : AppCompatActivity() {

    private lateinit var projectsGrid: GridView
    private lateinit var btnNewProject: Button
    private lateinit var btnImport: Button
    private lateinit var btnSettings: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge for Tab S10+
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_gallery)

        projectsGrid = findViewById(R.id.projectsGrid)
        btnNewProject = findViewById(R.id.btnNewProject)
        btnImport = findViewById(R.id.btnImport)
        btnSettings = findViewById(R.id.btnSettings)

        btnNewProject.setOnClickListener { openNewProject() }
        btnImport.setOnClickListener { /* TODO: Import */ }
        btnSettings.setOnClickListener { /* TODO: Settings */ }
    }

    /**
     * Open a new project with Tab S10+ native resolution.
     */
    private fun openNewProject() {
        val intent = Intent(this, DrawingActivity::class.java).apply {
            putExtra("canvas_width", TabS10Plus.SCREEN_WIDTH)
            putExtra("canvas_height", TabS10Plus.SCREEN_HEIGHT)
        }
        startActivity(intent)
    }
}
