package com.banuba.sdk.example.effect_player_realtime_preview

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.banuba.sdk.entity.ContentRatioParams
import com.banuba.sdk.entity.RecordedVideoInfo
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.manager.IEventCallback
import com.banuba.sdk.types.Data
import kotlinx.android.synthetic.main.activity_camera_preview.surfaceView
import kotlinx.android.synthetic.main.activity_video_recording.*
import java.io.File

/**
 * Sample activity that shows how to record video with Banuba SDK.
 * Specify custom options in [BanubaSdkManager.startVideoRecording] to record video you need.
 *
 * NOTE:
 * Applied masks are recorded as well.
 */
class VideoRecordingActivity : AppCompatActivity() {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        )

        private const val REQUEST_CODE_VIDEO_RECORDING_PERMISSION = 1002

        private const val TAG = "VideoRecordingActivity"

        private val HD_SIZE = Size(720, 1280)
    }

    private val banubaSdkManager by lazy(LazyThreadSafetyMode.NONE) {
        BanubaSdkManager(applicationContext).apply {
            setCallback(banubaEventCallback)
        }
    }

    private val banubaEventCallback = object : IEventCallback {
        override fun onVideoRecordingFinished(videoInfo: RecordedVideoInfo) {
            Log.d(TAG, "Video recording finished. Recorded file = ${videoInfo.filePath}," +
                    "duration = ${videoInfo.recordedLength}")
        }

        override fun onVideoRecordingStatusChange(isStarted: Boolean) {
            Log.d(TAG, "Video recording status changed. isRecording = $isStarted")
        }

        override fun onCameraOpenError(e: Throwable) {
            // Implement custom error handling here
        }

        override fun onImageProcessed(imageBitmpa: Bitmap) {}

        override fun onEditingModeFaceFound(faceFound: Boolean) {}

        override fun onHQPhotoReady(photoBitmap: Bitmap) {}

        override fun onEditedImageReady(imageBitmap: Bitmap) {}

        override fun onFrameRendered(data: Data, width: Int, height: Int) {}

        override fun onScreenshotReady(screenshotBitmap: Bitmap) {}

        override fun onCameraStatus(isOpen: Boolean) {}
    }

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_recording)

        recordActionButton.setOnClickListener {
            isRecording = !isRecording

            updateUiState()

            if (isRecording) {
                banubaSdkManager.startVideoRecording(
                        generateVideoFilePath(),
                        recordAudio(),
                        ContentRatioParams(HD_SIZE.width, HD_SIZE.height, false),
                        1f // speed
                )
            } else {
                banubaSdkManager.stopVideoRecording()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        banubaSdkManager.attachSurface(surfaceView)

        if (allPermissionsGranted()) {
            banubaSdkManager.openCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_VIDEO_RECORDING_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            results: IntArray
    ) {
        if (requireAllPermissionsGranted(permissions, results)) {
            banubaSdkManager.openCamera()
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        banubaSdkManager.effectPlayer.playbackPlay()
    }

    override fun onPause() {
        super.onPause()
        banubaSdkManager.effectPlayer.playbackPause()
    }

    override fun onStop() {
        super.onStop()
        banubaSdkManager.releaseSurface()
        banubaSdkManager.closeCamera()
    }

    private fun recordAudio() = recordAudioSwitch.isChecked

    private fun updateUiState() {
        recordActionButton.text = if (isRecording) {
            getString(R.string.stop)
        } else {
            getString(R.string.start)
        }

        recordAudioSwitch.visibility = if (isRecording) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun generateVideoFilePath(): String = File(applicationContext.filesDir,
            "banuba_video_${System.currentTimeMillis()}.mp4").absolutePath
}