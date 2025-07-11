package com.example.motiv8me.data.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.motiv8me.domain.permission.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PermissionManager {

    private val _permissionStatus = MutableStateFlow(getInitialPermissionStatus())
    override val permissionStatus: StateFlow<Map<String, Boolean>> = _permissionStatus.asStateFlow()

    private fun getInitialPermissionStatus(): Map<String, Boolean> {
        return mapOf(
            Manifest.permission.SET_WALLPAPER to checkPermission(Manifest.permission.SET_WALLPAPER),
            Manifest.permission.POST_NOTIFICATIONS to checkPermission(Manifest.permission.POST_NOTIFICATIONS)
        )
    }

    override fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(permission: String) {
        // This method is typically handled by the Activity/Fragment
        // and its result is then propagated back to the ViewModel.
        // For simplicity, we'll just update the status based on current check.
        _permissionStatus.value = _permissionStatus.value.toMutableMap().apply {
            this[permission] = checkPermission(permission)
        }
    }
}