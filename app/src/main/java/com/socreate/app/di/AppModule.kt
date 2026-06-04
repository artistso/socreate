package com.socreate.app.di

import android.content.Context
import com.socreate.app.data.repository.CanvasRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection Module
 * Provides singleton instances for application-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideCanvasRepository(
        @ApplicationContext context: Context
    ): CanvasRepository {
        return CanvasRepository(context)
    }
}
