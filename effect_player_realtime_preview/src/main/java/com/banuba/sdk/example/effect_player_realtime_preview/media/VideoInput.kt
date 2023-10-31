package com.banuba.sdk.example.effect_player_realtime_preview.media

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo.CodecCapabilities
import android.net.Uri
import android.util.Size
import com.banuba.sdk.effect_player.FrameProcessor
import com.banuba.sdk.effect_player.ProcessorConfiguration
import com.banuba.sdk.input.IInput
import com.banuba.sdk.internal.utils.CameraUtils
import com.banuba.sdk.types.FrameData
import com.banuba.sdk.types.FullImageData
import java.io.File
import java.nio.ByteBuffer
import java.util.Objects
import java.util.concurrent.TimeUnit

class VideoInput(private val context: Context): IInput {

    interface IVideoFrameStatus {
        fun onError(throwable: Throwable)
        fun onFrameDecoded(frameTimeNanos: Long)
        fun onFinished()
    }

    private val frameProcessor by lazy(LazyThreadSafetyMode.NONE) {
        val config = Objects.requireNonNull(ProcessorConfiguration.create()!!)
        config.setUseOfflineMode(false)
        config.setUseFutureFilter(false)
        config.setUseFutureInterpolate(false)
        Objects.requireNonNull(FrameProcessor.createVideoProcessor(config))!!
    }

    private var timestamp: Long = 0

    private var callback: IVideoFrameStatus? = null
    private var videoExtractor: VideoExtractor? = null

    fun processVideoFile(inputFile: File, videoDecodeStatusCallback: IVideoFrameStatus) {
        callback = videoDecodeStatusCallback
        try {
            val startExtractionNanoTime = System.nanoTime()
            videoExtractor = VideoExtractor.getVideoDecoder(context, Uri.fromFile(inputFile)) {
                    rotation: Int, w: Int, h: Int, stride: Int, sliceHeight: Int, fmt: Int, buf: ByteBuffer, bufInfo: MediaCodec.BufferInfo ->
                @SuppressLint("RestrictedApi") val camOrient = CameraUtils.degreesToCameraOrientation(rotation)
                val orient = FullImageData.Orientation(camOrient, false, 0)
                val fullImageData = createFullImageData(fmt, buf, w, h, stride, sliceHeight, orient)
                val frameTimeNanos = TimeUnit.MICROSECONDS.toNanos(bufInfo.presentationTimeUs)
                val frameData = Objects.requireNonNull(FrameData.create())!!
                frameData.addFullImg(fullImageData)
                frameProcessor.push(frameData)
                timestamp = frameTimeNanos + startExtractionNanoTime
                callback!!.onFrameDecoded(timestamp)
            }
            videoExtractor!!.play()
            stopProcessing()
        } catch (throwable: Throwable) {
            videoExtractor!!.stop()
            videoExtractor = null
            callback!!.onError(throwable)
        }
    }

    private fun createFullImageData(fmt: Int, buf: ByteBuffer, w: Int, h: Int, stride: Int, sliceHeight: Int, orient: FullImageData.Orientation):
            FullImageData {
        val isYUV420SemiPlanarFormat = fmt == CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        val isYUV420PlanarFormat = fmt == CodecCapabilities.COLOR_FormatYUV420Planar
        if (!isYUV420SemiPlanarFormat && !isYUV420PlanarFormat) {
            throw RuntimeException("Unknown color format = " + fmt + " (0x" + Integer.toHexString(fmt) + ")")
        }
        val uvStride = if (isYUV420SemiPlanarFormat) stride else stride / 2
        val yPxStride = 1
        val uvPxStride = if (isYUV420SemiPlanarFormat) 2 else 1
        buf.position(0)
        val y = buf.slice()
        buf.position(stride * sliceHeight)
        val u = buf.slice()
        buf.position(if (isYUV420SemiPlanarFormat) stride * sliceHeight + 1 else 5 * stride * sliceHeight / 4)
        val v = buf.slice()
        return FullImageData(Size(w, h), y, u, v, stride, uvStride, uvStride, yPxStride, uvPxStride, uvPxStride, orient)
    }

    fun stopProcessing() {
        if (videoExtractor != null) {
            videoExtractor!!.stop()
            videoExtractor = null
            callback!!.onFinished()
        }
    }

    override fun frameProcessor(): FrameProcessor = frameProcessor

    override fun getFrameTimeNanos(): Long = timestamp

    override fun attach() {}
    override fun detach() {}
}
