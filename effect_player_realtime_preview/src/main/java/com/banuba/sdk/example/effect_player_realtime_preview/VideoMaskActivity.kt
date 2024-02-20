package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Context
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.banuba.sdk.input.VideoInput
import com.banuba.sdk.output.VideoOutput
import com.banuba.sdk.player.Player
import com.banuba.sdk.video.IAudioDataProvider
import kotlinx.android.synthetic.main.activity_mask_video.btn_open_original
import kotlinx.android.synthetic.main.activity_mask_video.btn_video_action
import kotlinx.android.synthetic.main.activity_mask_video.progress
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.LinkedList

class VideoMaskActivity : AppCompatActivity(R.layout.activity_mask_video) {
    companion object {
        private const val MASK_NAME = "effects/TrollGrandma"
        private const val VIDEO_FILE = "face_video_720p.mp4"
    }

    // The player executes the main pipeline
    private val player by lazy(LazyThreadSafetyMode.NONE) {
        Player()
    }

    // This video input will pass frames from the video file to the player
    private val videoInput by lazy(LazyThreadSafetyMode.NONE) {
        VideoInput()
    }

    // The result will be recorded to the file
    private val videoOutput by lazy(LazyThreadSafetyMode.NONE) {
        VideoOutput()
    }

    private var isVideoReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Synchronized effect loading
        player.load(MASK_NAME)

        // Set layer will take input frames from and where the player will display the result
        player.use(videoInput, videoOutput)

        val inputFile = copyFromAssetsToFile(VIDEO_FILE)
        val outputFile = File(getExternalFilesDir(null), "$VIDEO_FILE.mask.mp4")

        btn_open_original.setOnClickListener {
            showVideo(inputFile)
        }

        btn_video_action.setOnClickListener {
            if (isVideoReady) {
                showVideo(outputFile)
            } else {
                processVideo(inputFile, outputFile)
            }
        }
    }

    override fun onDestroy() {
        videoOutput.close()
        player.close()
        super.onDestroy()
    }

    fun processVideo(inputFile: File, outputFile: File) {
        // Set callback for video processing
        videoInput.processVideoFile(inputFile, object : VideoInput.IVideoFrameStatus {
            override fun onStart() {
                runOnUiThread {
                    progress.visibility = View.VISIBLE
                    btn_video_action.isEnabled = false
                    btn_video_action.text = getString(R.string.wait_for_processing)
                }

                // Set the appropriate rendering mode. For video processing it should be MANUAL
                player.setRenderMode(Player.RenderMode.MANUAL)

                // We transfer audio data from the input file to the output file without changes.
                // If audio data is not needed, simply remove this call.
                videoOutput.startRecording(outputFile, object : IAudioDataProvider {
                    override fun getAudioFormat(): MediaFormat? = videoInput.audioFormat
                    override fun getAudioData(): LinkedList<Pair<MediaCodec.BufferInfo?, ByteBuffer?>?>? = videoInput.audioData
                })
            }

            override fun onFrame() {
                // Processing the next frame upon request.
                player.render()
            }

            override fun onError(throwable: Throwable) {
                throw throwable;
            }

            override fun onFinish() {
                // We are waiting for the video file to finish recording.
                videoOutput.stopRecordingAndWaitForFinish()

                // Switch the rendering mode to the previous one.
                player.setRenderMode(Player.RenderMode.LOOP)

                runOnUiThread {
                    progress.visibility = View.INVISIBLE
                    btn_video_action.isEnabled = true
                    btn_video_action.text = getString(R.string.open_video)
                    isVideoReady = true
                }
            }
        })
    }

    fun showVideo(videoFile: File) {
        val uri = FileProvider.getUriForFile(this, this.packageName, videoFile)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setDataAndType(uri, "video/mp4")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }

    fun copyFromAssetsToFile(filename: String): File {
        val file = File(getExternalFilesDir(null), filename)
        file.parentFile?.mkdirs()
        assets.open(filename).copyTo(FileOutputStream(file))
        return file
    }
}
