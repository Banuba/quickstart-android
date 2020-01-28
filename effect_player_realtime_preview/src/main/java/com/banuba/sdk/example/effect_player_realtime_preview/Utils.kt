package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast


fun Context.requireAllPermissionsGranted(
        permissions: Array<String>,
        results: IntArray): Boolean {
    val notGrantedPermissionIndex = results.indexOfFirst { result ->
        result != PackageManager.PERMISSION_GRANTED
    }

    return if (notGrantedPermissionIndex != -1) {
        val notGrantedPermission = permissions[notGrantedPermissionIndex]
        Toast.makeText(applicationContext,
                """Not all permissions granted. Please grant $notGrantedPermission permission.""",
                Toast.LENGTH_LONG)
                .show()

        false
    } else {
        true
    }
}
