package com.banuba.sdk.example.effect_player_realtime_preview

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.applyMaskButton
import kotlinx.android.synthetic.main.activity_main.openCameraButton
import kotlinx.android.synthetic.main.activity_main.recordVideoButton
import kotlinx.android.synthetic.main.activity_main.videoMaskButton

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )

        const val REQUEST_CODE_PERMISSIONS = 1000
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openCameraButton.setOnClickListener {
            startActivity(Intent(applicationContext, CameraPreviewActivity::class.java))
        }

        applyMaskButton.setOnClickListener {
            startActivity(Intent(applicationContext, MaskActivity::class.java))
        }

        recordVideoButton.setOnClickListener {
            startActivity(Intent(applicationContext, VideoRecordingActivity::class.java))
        }

        videoMaskButton.setOnClickListener {
            startActivity(Intent(applicationContext, VideoMaskActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (!allPermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, results: IntArray) {
        if (!requireAllPermissionsGranted(permissions, results)) {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, results)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.requireAllPermissionsGranted(permissions: Array<String>, results: IntArray): Boolean {
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
