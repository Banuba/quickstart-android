package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


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


class FileUtils {

    companion object {

        @JvmStatic
        fun copyFromAssetsToFile(context: Context, filename: String): File {

            val file = File(context.getExternalFilesDir(null), filename)
            val dir = file.parentFile
            dir?.mkdirs()
            context.assets.open(filename).copyTo(FileOutputStream(file))
            return file
        }
    }
}