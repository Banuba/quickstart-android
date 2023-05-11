package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.banuba.sdk.example.effect_player_realtime_preview.media.MaskProcessor
import kotlinx.android.synthetic.main.activity_mask_video.*
import java.io.File
import kotlin.concurrent.thread

class VideoMaskActivity : AppCompatActivity() {

    companion object {
        private const val MASK_NAME = "effects/TrollGrandma"
        private const val VIDEO_FILE = "face_video_720p.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mask_video)

        val outputFile = File(getExternalFilesDir(null), "$VIDEO_FILE.mask.mp4")

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
            // New thread because MaskProcessor creates Open GL ES 3.0 Context

            val inputFile = FileUtils.copyFromAssetsToFile(this, VIDEO_FILE)
            MaskProcessor(this, inputFile, outputFile, MASK_NAME).process()

            progress.visibility = View.INVISIBLE
            runOnUiThread {
                btn_open_mask_video.isEnabled = true
            }
        }

    }

}