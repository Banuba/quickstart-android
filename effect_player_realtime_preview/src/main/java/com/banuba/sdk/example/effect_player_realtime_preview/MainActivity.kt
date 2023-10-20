package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        recordVideoButton.setOnClickListener {
            startActivity(Intent(applicationContext, VideoRecordingActivity::class.java))
        }

        videoMaskButton.setOnClickListener {
            startActivity(Intent(applicationContext, VideoMaskActivity::class.java))
        }

        imageButton.setOnClickListener {
            startActivity(Intent(applicationContext, ImageActivity::class.java))
        }

    }
}
