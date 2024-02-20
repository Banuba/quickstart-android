package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.banuba.sdk.input.CameraDevice
import com.banuba.sdk.input.CameraInput
import com.banuba.sdk.output.SurfaceOutput
import com.banuba.sdk.output.VideoOutput
import com.banuba.sdk.player.Player
import kotlinx.android.synthetic.main.activity_camera_preview.surfaceView
import kotlinx.android.synthetic.main.activity_video_recording.*
import java.io.File


/**
 * Sample activity that shows how to record video with Banuba SDK PlayerAPI.
 *
 * NOTE:
 * Applied masks are recorded as well.
 */
class VideoRecordingActivity : AppCompatActivity(R.layout.activity_video_recording) {
    // The player executes the main pipeline
    private val player by lazy(LazyThreadSafetyMode.NONE) {
        Player()
    }

    // This camera device will pass frames to the CameraInput
    private val cameraDevice by lazy(LazyThreadSafetyMode.NONE) {
        CameraDevice(requireNotNull(this.applicationContext), this@VideoRecordingActivity)
    }

    // The result will be displayed on the surface
    private val surfaceOutput by lazy(LazyThreadSafetyMode.NONE) {
        SurfaceOutput(surfaceView.holder)
    }

    // Also the result will be recorded to the video file
    private val videoOutput by lazy(LazyThreadSafetyMode.NONE) {
        VideoOutput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set layer will take input frames from and where the player will display the result
        player.use(CameraInput(cameraDevice), arrayOf(surfaceOutput, videoOutput))

        var stateVideoButton = 0
        var videoFileName = File("")
        recordActionButton.setOnClickListener {
            if (stateVideoButton == 0) { // Start recording
                // Set the option if you need to record audio from the microphone
                videoOutput.recordAudioFromMicrophone(recordAudioSwitch.isChecked)
                // Start recording to the video file
                videoFileName = generateOutputVideoFile()
                videoOutput.startRecording(videoFileName)

                recordActionButton.text = getString(R.string.stop)
                recordAudioSwitch.visibility = View.GONE


            } else if (stateVideoButton == 1) { // Stop recording
                // Stop recording and wait for finish
                videoOutput.stopRecordingAndWaitForFinish()

                recordActionButton.text = getString(R.string.play)
                recordAudioSwitch.visibility = View.GONE
            } else { // Play video
                showVideo(videoFileName)

                recordActionButton.text = getString(R.string.start)
                recordAudioSwitch.visibility = View.VISIBLE
            }

            stateVideoButton = (stateVideoButton + 1) % 3
        }
    }

    override fun onStart() {
        super.onStart()
        // We start the camera and then player starts taking frames
        cameraDevice.start()
    }

    override fun onResume() {
        super.onResume()
        // Running the player
        player.play()
    }

    override fun onPause() {
        super.onPause()
        // Pause the player when activity is inactive
        player.pause()
    }

    override fun onStop() {
        // After this method, the camera will stop capturing frames and transmitting them to player
        cameraDevice.stop()
        super.onStop()
    }

    override fun onDestroy() {
        // After you are done using the player, you must free all resources by calling close() method
        player.close()
        cameraDevice.close()
        surfaceOutput.close()
        videoOutput.close()
        super.onDestroy()
    }

    fun showVideo(videoFile: File) {
        val uri = FileProvider.getUriForFile(this, this.packageName, videoFile)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setDataAndType(uri, "video/mp4")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }

    private fun generateOutputVideoFile(): File = File(getExternalFilesDir(null),
            "banuba_video_${System.currentTimeMillis()}.mp4")
}
