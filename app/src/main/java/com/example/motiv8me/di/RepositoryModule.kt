package com.example.motiv8me.di

import com.example.motiv8me.data.repository.SettingsRepositoryImpl
import com.example.motiv8me.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module responsible for binding repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class) // Repositories often live for the app's lifecycle
abstract class RepositoryModule { // Use 'abstract class' when using @Binds

    /**
     * Binds the SettingsRepository interface to its SettingsRepositoryImpl implementation.
     * Hilt will automatically provide the SettingsRepositoryImpl instance because it has
     * an @Inject constructor and its dependencies (DataStore) are provided elsewhere (AppModule).
     *
     * @param settingsRepositoryImpl The concrete implementation of the repository.
     * @return An instance conforming to the SettingsRepository interface.
     */
    @Binds
    @Singleton // The scope here should match the scope of the implementation (SettingsRepositoryImpl is @Singleton)
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindPermissionManager(
        permissionManagerImpl: com.example.motiv8me.data.permission.PermissionManagerImpl
    ): com.example.motiv8me.domain.permission.PermissionManager

    // Add bindings for other repositories here if created later
    // e.g., @Binds abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

}