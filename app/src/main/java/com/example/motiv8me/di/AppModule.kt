package com.example.motiv8me.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Define the name for the Preferences DataStore file
private const val USER_PREFERENCES_NAME = "motiv8me_settings"

@Module
@InstallIn(SingletonComponent::class) // Provide dependencies for the entire application lifecycle
object AppModule {

    /**
     * Provides a singleton instance of Preferences DataStore.
     *
     * @param applicationContext The application context provided by Hilt.
     * @return A DataStore<Preferences> instance.
     */
    @Provides
    @Singleton // Ensure only one instance of DataStore is created
    fun providePreferencesDataStore(@ApplicationContext applicationContext: Context): DataStore<Preferences> {
        // Use PreferenceDataStoreFactory to create the DataStore instance.
        // This ensures it's created correctly within the Hilt dependency graph.
        return PreferenceDataStoreFactory.create(
            // migrations = listOf( ... ) // Add migrations if needed later
            produceFile = { applicationContext.preferencesDataStoreFile(USER_PREFERENCES_NAME) }
        )
        // Note: The alternative `preferencesDataStore` delegate by Kotlin should NOT be used
        // directly within a Hilt module's @Provides function, as it relies on context extension
        // properties which can behave unexpectedly with Hilt's lifecycle management.
        // Using the Factory is the recommended approach for Hilt integration.
    }

    // Add other application-wide providers here if needed later
    // e.g., Retrofit instance, Room database, etc.

}