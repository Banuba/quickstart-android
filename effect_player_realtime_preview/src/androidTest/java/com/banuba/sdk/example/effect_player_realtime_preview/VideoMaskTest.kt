package com.banuba.sdk.example.effect_player_realtime_preview

import androidx.test.platform.app.InstrumentationRegistry
import com.banuba.sdk.example.effect_player_realtime_preview.media.MaskProcessor
import org.junit.Test
import java.io.File

class VideoMaskTest {

    @Test
    @Throws(Exception::class)
    fun test720p() {
        makeMaskOnVideo("face_video_720p.mp4")
    }

    @Test
    @Throws(Exception::class)
    fun test1080p() {
        makeMaskOnVideo("face_video_1080p.mp4")
    }

    @Test
    @Throws(Exception::class)
    fun test1080p_r90() {
        makeMaskOnVideo("face_video_1080p_r90.mp4")
    }

    @Test
    @Throws(Exception::class)
    fun test1080p_r180() {
        makeMaskOnVideo("face_video_1080p_r180.mp4")
    }

    @Test
    @Throws(Exception::class)
    fun test1080p_r270() {
        makeMaskOnVideo("face_video_1080p_r270.mp4")
    }


    private fun makeMaskOnVideo(input: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val inputFile = FileUtils.copyFromAssetsToFile(context, input)
        val outputFile = File(context.getExternalFilesDir(null), "$input.mask.mp4")
        MaskProcessor(context, inputFile, outputFile, "UnluckyWitch").process()
    }


}