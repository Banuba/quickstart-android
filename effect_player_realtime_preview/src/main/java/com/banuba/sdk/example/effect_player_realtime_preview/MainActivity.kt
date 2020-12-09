package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.banuba.sdk.example.effect_player_realtime_preview.arcloud.ArCloudMasksActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openCameraButton.setOnClickListener {
            startActivity(Intent(applicationContext, CameraPreviewActivity::class.java))
        }

        applyMaskButton.setOnClickListener {
            startActivity(Intent(applicationContext, MaskActivity::class.java))
        }

        applyArCloudMasksButton.setOnClickListener {
            startActivity(Intent(applicationContext, ArCloudMasksActivity::class.java))
        }

        recordVideoButton.setOnClickListener {
            startActivity(Intent(applicationContext, VideoRecordingActivity::class.java))
        }
    }
}
