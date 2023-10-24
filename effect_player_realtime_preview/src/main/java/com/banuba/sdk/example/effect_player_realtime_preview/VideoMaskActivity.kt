package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.banuba.sdk.example.effect_player_realtime_preview.media.VideoInput
import com.banuba.sdk.example.effect_player_realtime_preview.media.VideoInput.IVideoFrameStatus
import com.banuba.sdk.output.VideoOutput
import com.banuba.sdk.player.Player
import kotlinx.android.synthetic.main.activity_mask_video.btn_open_mask_video
import kotlinx.android.synthetic.main.activity_mask_video.progress
import java.io.File
import kotlin.concurrent.thread

class VideoMaskActivity : AppCompatActivity() {

    companion object {
        private const val MASK_NAME = "effects/TrollGrandma"
        private const val VIDEO_FILE = "face_video_720p.mp4"
    }

    private val player by lazy(LazyThreadSafetyMode.NONE) {
        Player()
    }

    private val videoInput by lazy(LazyThreadSafetyMode.NONE) {
        VideoInput(this)
    }

    private val videoOutput by lazy(LazyThreadSafetyMode.NONE) {
        VideoOutput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mask_video)

        val outputFile = File(getExternalFilesDir(null), "$VIDEO_FILE.mask.mp4")

        player.setRenderMode(Player.RenderMode.MANUAL)
        player.load(MASK_NAME)
        player.use(videoInput, videoOutput)
        videoOutput.startRecording(outputFile)

        btn_open_mask_video.setOnClickListener {

            val uri = FileProvider.getUriForFile(
                this, this.packageName, outputFile
            )
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setDataAndType(uri, "video/mp4")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        }

        thread(start = true) {
            val inputFile = FileUtils.copyFromAssetsToFile(this, VIDEO_FILE)
            videoInput.processVideoFile(inputFile, object : IVideoFrameStatus {
                override fun onError(throwable: Throwable) {
                    throw throwable;
                }

                override fun onFrameDecoded(frameTimeNanos: Long) {
                    player.render()
                }

                override fun onFinished() {
                    videoOutput.stopRecordingAndWaitForFinish();
                }
            })

            videoOutput.close()

            progress.visibility = View.INVISIBLE
            runOnUiThread {
                btn_open_mask_video.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        videoOutput.close()
        player.close()
        super.onDestroy()
    }
}