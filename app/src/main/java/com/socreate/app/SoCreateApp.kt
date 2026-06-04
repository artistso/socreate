package com.socreate.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * SoCreate Application class
 * Initializes Hilt DI and global configuration
 * 
 * Developed by Steven Michael Allen Owens (@SoQuarky)
 * An AdventuresInDrawing production
 */
@HiltAndroidApp
class SoCreateApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // App is initialized via Hilt
        // All engines are lazily initialized when needed
    }
}
