package com.socreate.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.socreate.app.core.model.DrawingState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "socreate_settings")

/**
 * Repository for canvas data persistence
 * Handles auto-save, crash recovery, and state management
 */
class CanvasRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val dataStore = context.dataStore
    
    companion object {
        private const val KEY_LAST_STATE = "last_drawing_state"
        private const val KEY_AUTO_SAVE_INTERVAL = "auto_save_interval_ms"
        private const val KEY_CANVAS_DIR = "canvases"
    }
    
    /**
     * Save current drawing state
     */
    suspend fun saveCanvas(state: DrawingState): Result<Unit> {
        return try {
            val stateJson = json.encodeToString(state)
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(KEY_LAST_STATE)] = stateJson
                preferences[longPreferencesKey("last_saved_at")] = System.currentTimeMillis()
            }
            
            // Also save to file for crash recovery
            val canvasDir = File(context.filesDir, KEY_CANVAS_DIR)
            if (!canvasDir.exists()) canvasDir.mkdirs()
            
            val stateFile = File(canvasDir, "current_state.json")
            stateFile.writeText(stateJson)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load last saved drawing state
     */
    suspend fun loadLastCanvas(): DrawingState? {
        return try {
            // Try DataStore first
            val preferences = dataStore.data.first()
            val stateJson = preferences[stringPreferencesKey(KEY_LAST_STATE)]
            
            if (stateJson != null) {
                json.decodeFromString<DrawingState>(stateJson)
            } else {
                // Fallback to file
                val canvasDir = File(context.filesDir, KEY_CANVAS_DIR)
                val stateFile = File(canvasDir, "current_state.json")
                if (stateFile.exists()) {
                    json.decodeFromString<DrawingState>(stateFile.readText())
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get auto-save settings
     */
    fun getAutoSaveSettings(): Flow<AutoSaveSettings> {
        return dataStore.data.map { preferences ->
            AutoSaveSettings(
                enabled = preferences[booleanPreferencesKey("auto_save_enabled")] ?: true,
                intervalMs = preferences[longPreferencesKey(KEY_AUTO_SAVE_INTERVAL)] ?: 30000
            )
        }
    }
    
    /**
     * Update auto-save settings
     */
    suspend fun updateAutoSaveSettings(enabled: Boolean, intervalMs: Long) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("auto_save_enabled")] = enabled
            preferences[longPreferencesKey(KEY_AUTO_SAVE_INTERVAL)] = intervalMs
        }
    }
    
    /**
     * Clear all saved data
     */
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
        val canvasDir = File(context.filesDir, KEY_CANVAS_DIR)
        canvasDir.deleteRecursively()
    }
}

data class AutoSaveSettings(
    val enabled: Boolean = true,
    val intervalMs: Long = 30000
)
