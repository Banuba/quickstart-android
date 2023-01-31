package com.banuba.sdk.example.effect_player_realtime_preview.media

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.net.Uri
import android.opengl.GLES20
import android.util.Size
import com.banuba.sdk.effect_player.*
import com.banuba.sdk.encoding.MediaMuxerWrapperExternalAudio
import com.banuba.sdk.encoding.sync.EncoderSync
import com.banuba.sdk.internal.gl.EglCore
import com.banuba.sdk.internal.gl.OffscreenSurface
import com.banuba.sdk.internal.gl.WindowSurface
import com.banuba.sdk.types.FullImageData
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class MaskProcessor(
    private val context: Context,
    private val inputFile: File,
    private val outputFile: File,
    private val maskName: String,
    private val lengthInSeconds: Int = 10
) {

    fun process() {


        val eglCore = EglCore(null, 0x02)
        val surface = OffscreenSurface(eglCore, 64, 64)
        surface.makeCurrent()

        val uri = Uri.fromFile(inputFile)
        val audioExtractor = AudioExtractor.getAudioExtractor(context, uri)

        val audio = audioExtractor.extractAudioStream(lengthInSeconds)

        val sync = EncoderSync()
        var player: EffectPlayer? = null
        var muxer: MediaMuxerWrapperExternalAudio? = null
        var codecSurface: WindowSurface? = null

        var decoder: VideoExtractor? = null

        val decoderVideoListener: VideoExtractor.DecoderVideoListener =
            object : VideoExtractor.DecoderVideoListener {

                override fun onVideoBufferDecoded(
                    rotation: Int,
                    width: Int, height: Int,
                    stride: Int, sliceHeight: Int,
                    colorFormat: Int,
                    buffer: ByteBuffer,
                    bufferInfo: MediaCodec.BufferInfo
                ) {

                    if (bufferInfo.presentationTimeUs >= TimeUnit.SECONDS.toMicros(lengthInSeconds.toLong())) {
                        decoder?.stop()
                        return
                    }

                    val rw: Int = if (rotation == 90 || rotation == 270) height else width
                    val rh: Int = if (rotation == 90 || rotation == 270) width else height

                    if (player == null) {

                        val config = EffectPlayerConfiguration.create(rw, rh)

                        player = EffectPlayer.create(config)
                        player?.setRenderConsistencyMode(ConsistencyMode.SYNCHRONOUS)
                        player?.surfaceCreated(rw, rh)
                        // player?.effectManager()?.setEffectVolume(0f)
                        player?.playbackPlay()
                        val effectManager = player?.effectManager()
                        effectManager?.load(maskName)

                        muxer = MediaMuxerWrapperExternalAudio(
                            null,
                            null,
                            outputFile.path,
                            sync,
                            0L,
                            1.0f,
                            rw,
                            rh,
                            null,
                            audioExtractor.mediaFormat
                        )
                        muxer?.prepare()
                        codecSurface = WindowSurface(eglCore, muxer?.inputSurface, false)
                        codecSurface?.makeCurrent()
                        muxer?.startRecording()

                        sync.setEncoderReady()
                    }

                    sync.setEncodingStarted()

                    val pl = requireNotNull(player)
                    val mx = requireNotNull(muxer)
                    val cs = requireNotNull(codecSurface)

                    val orientation = FullImageData.Orientation(getCamRotation(rotation), false, 0)
                    val data = createImageByType(
                        colorFormat,
                        buffer,
                        width,
                        height,
                        stride,
                        sliceHeight,
                        orientation
                    )
                    pl.pushFrameWithNumber(data, bufferInfo.presentationTimeUs)

                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
                    GLES20.glViewport(0, 0, rw, rh)
                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

                    var drawResult: Long
                    do {
                        drawResult = pl.draw()
                    } while (drawResult < 0)


                    mx.frameAvailableSoon()
                    val timeVideoMicroSec = bufferInfo.presentationTimeUs
                    val timeNs: Long = TimeUnit.MICROSECONDS.toNanos(timeVideoMicroSec)
                    cs.setPresentationTime(timeNs)
                    cs.swapBuffers()


                    var timeAudioMicroSec: Long
                    do {
                        val pair = audio.peekFirst()
                        if (pair != null) {
                            val audioInfo = pair.first
                            val audioBuffer = pair.second
                            timeAudioMicroSec = audioInfo!!.presentationTimeUs
                            if (mx.writeAudioSampleData(audioBuffer!!, audioInfo)) {
                                audio.removeFirst()
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    } while (timeAudioMicroSec < timeVideoMicroSec)

                    sync.setAudioEncoded()
                    sync.waitForEncodingReady()

                }

                private fun getCamRotation(rotation: Int): CameraOrientation {
                    return when (rotation) {
                        90 -> CameraOrientation.DEG_90
                        180 -> CameraOrientation.DEG_180
                        270 -> CameraOrientation.DEG_270
                        else -> CameraOrientation.DEG_0
                    }
                }
            }

        decoder = VideoExtractor.getVideoDecoder(context, uri, decoderVideoListener)
        decoder.play()

        player?.surfaceDestroyed()

        muxer?.stopRecording()
        muxer?.waitForFinish()

        eglCore.release()

    }


    @Suppress("DEPRECATION")
    private fun createImageByType(
        colorFormat: Int,
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        stride: Int,
        sliceHeight: Int,
        orientation: FullImageData.Orientation
    ): FullImageData {

        if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
            buffer.position(stride * sliceHeight)
            val bufferU = buffer.slice()
            buffer.position(stride * sliceHeight + 1)
            val bufferV = buffer.slice()
            buffer.rewind()
            return FullImageData(
                Size(width, height),
                buffer,
                bufferU,
                bufferV,
                stride,
                stride,
                stride,
                1,
                2,
                2,
                orientation
            )
        } else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
            buffer.position(stride * sliceHeight)
            val bufferU = buffer.slice()
            buffer.position(5 * stride * sliceHeight / 4)
            val bufferV = buffer.slice()
            buffer.rewind()
            return FullImageData(
                Size(width, height),
                buffer,
                bufferU,
                bufferV,
                stride,
                stride / 2,
                stride / 2,
                1,
                1,
                1,
                orientation
            )
        }
        throw RuntimeException(
            "Unknown color format = $colorFormat [0x" + Integer.toHexString(
                colorFormat
            ) + "]"
        )

    }

}