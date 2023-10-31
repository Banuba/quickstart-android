package com.banuba.sdk.example.effect_player_realtime_preview

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.banuba.sdk.example.effect_player_realtime_preview.camera.CameraFragment


class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val singlePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCameraFragment()
            } else {
                openCameraPermissionFragment()
                if (!shouldShowRequestPermissionRationale(CAMERA)) {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                        )
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        if (cameraPermissionGranted()) {
            openCameraFragment()
        } else {
            askForCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()

        if (cameraPermissionGranted()
            && supportFragmentManager.fragments.any { it is PermissionNotGrantedFragment }
        ) {
            openCameraFragment()
        }
    }

    fun askForCameraPermission() {
        singlePermission.launch(CAMERA)
    }

    private fun cameraPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun openCameraPermissionFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, PermissionNotGrantedFragment())
            .commit()
    }

    private fun openCameraFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, CameraFragment())
            .commit()
    }
}
