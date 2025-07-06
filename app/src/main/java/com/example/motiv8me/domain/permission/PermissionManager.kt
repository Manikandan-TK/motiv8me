package com.example.motiv8me.domain.permission

import android.Manifest
import kotlinx.coroutines.flow.Flow

interface PermissionManager {
    val permissionStatus: Flow<Map<String, Boolean>>
    fun checkPermission(permission: String): Boolean
    fun requestPermission(permission: String)
}