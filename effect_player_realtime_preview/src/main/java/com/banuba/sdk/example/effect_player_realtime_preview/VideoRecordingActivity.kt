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
import com.banuba.sdk.input.CameraDevice
import com.banuba.sdk.input.CameraDeviceConfigurator
import com.banuba.sdk.input.CameraInput
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.manager.IEventCallback
import com.banuba.sdk.output.SurfaceOutput
import com.banuba.sdk.output.VideoOutput
import com.banuba.sdk.player.Player
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
    }

    private val player by lazy(LazyThreadSafetyMode.NONE) {
        Player()
    }

    private val cameraDevice by lazy(LazyThreadSafetyMode.NONE) {
        CameraDevice(requireNotNull(this.applicationContext), this@VideoRecordingActivity)
    }

    private val surfaceOutput by lazy(LazyThreadSafetyMode.NONE) {
        SurfaceOutput(surfaceView.holder)
    }

    private val videoOutput by lazy(LazyThreadSafetyMode.NONE) {
        VideoOutput()
    }

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_recording)

        player.use(CameraInput(cameraDevice))
        player.addOutput(surfaceOutput)
        player.addOutput(videoOutput)

        recordActionButton.setOnClickListener {
            isRecording = !isRecording

            updateUiState()

            if (isRecording) {
                videoOutput.recordAudioFromMicrophone(recordAudio())
                videoOutput.startRecording(File(generateVideoFilePath()))
            } else {
                videoOutput.stopRecordingAndWaitForFinish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (allPermissionsGranted()) {
            cameraDevice.start()
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
            cameraDevice.start()
        } else {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, results)
    }

    override fun onResume() {
        super.onResume()
        player.play()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onStop() {
        cameraDevice.stop()

        if (isRecording) {
            isRecording = false
            updateUiState()
            videoOutput.stopRecordingAndWaitForFinish()
        }
        super.onStop()
    }

    override fun onDestroy() {
        player.close()
        cameraDevice.close()
        surfaceOutput.close()
        videoOutput.close()
        super.onDestroy()
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