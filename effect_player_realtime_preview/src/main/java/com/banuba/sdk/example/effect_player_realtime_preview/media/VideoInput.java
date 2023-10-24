package com.banuba.sdk.example.effect_player_realtime_preview.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.net.Uri;
import android.util.Size;

import androidx.annotation.NonNull;

import com.banuba.sdk.effect_player.CameraOrientation;
import com.banuba.sdk.effect_player.FrameProcessor;
import com.banuba.sdk.effect_player.ProcessorConfiguration;
import com.banuba.sdk.input.IInput;
import com.banuba.sdk.internal.utils.CameraUtils;
import com.banuba.sdk.types.FrameData;
import com.banuba.sdk.types.FullImageData;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VideoInput implements IInput {
    public interface IVideoFrameStatus {
        void onError(@NonNull Throwable throwable);
        void onFrameDecoded(long frameTimeNanos);
        void onFinished();
    }

    private final FrameProcessor mFrameProcessor;
    private long mTimestamp = 0;
    private IVideoFrameStatus mCallback;
    private final Context mContext;

    private VideoExtractor mVideoExtractor;

    public VideoInput(@NonNull Context context) {
        final ProcessorConfiguration config = Objects.requireNonNull(ProcessorConfiguration.create());
        config.setUseOfflineMode(false);
        config.setUseFutureFilter(false);
        config.setUseFutureInterpolate(false);
        mFrameProcessor = Objects.requireNonNull(FrameProcessor.createVideoProcessor(config));
        mContext = context;
    }

    public void processVideoFile(@NonNull File inputFile, @NonNull IVideoFrameStatus videoDecodeStatusCallback) {
        mCallback = videoDecodeStatusCallback;

        try {
            final long startExtractionNanoTime = System.nanoTime();
            mVideoExtractor = VideoExtractor.getVideoDecoder(mContext, Uri.fromFile(inputFile),
                (rotation, width, height, stride, sliceHeight, format, buf, bufInfo) -> {
                    @SuppressLint("RestrictedApi") final CameraOrientation cameraOrientation = CameraUtils.degreesToCameraOrientation(rotation);
                    final FullImageData.Orientation orient = new FullImageData.Orientation(cameraOrientation, false, 0);
                    final FullImageData fullImageData = createFullImageData(format, buf, width, height, stride, sliceHeight, orient);
                    final long frameTimeNanos = TimeUnit.MICROSECONDS.toNanos(bufInfo.presentationTimeUs);
                    final FrameData frameData = Objects.requireNonNull(FrameData.create());
                    frameData.addFullImg(fullImageData);

                    mFrameProcessor.push(frameData);
                    mTimestamp = frameTimeNanos + startExtractionNanoTime;

                    mCallback.onFrameDecoded(mTimestamp);
                });
            mVideoExtractor.play();
            stopProcessing();
        } catch (final Throwable throwable) {
            mCallback.onError(throwable);
        }
    }

    private FullImageData createFullImageData(int format, ByteBuffer buf, int width, int height, int stride, int sliceHeight, FullImageData.Orientation orient) {
        final boolean isYUV420SemiPlanarFormat = format == CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        final boolean isYUV420PlanarFormat = format == CodecCapabilities.COLOR_FormatYUV420Planar;
        if (!isYUV420SemiPlanarFormat && !isYUV420PlanarFormat) {
            throw new RuntimeException("Unknown color format = " + format + " (0x" + Integer.toHexString(format) + ")");
        }

        final int uvStride = isYUV420SemiPlanarFormat ? stride : stride / 2;
        final int yPxStride = 1;
        final int uvPxStride = isYUV420SemiPlanarFormat ? 2 : 1;
        final Size size = new Size(width, height);

        buf.position(0);
        final ByteBuffer yBuf = buf.slice();
        buf.position(stride * sliceHeight);
        final ByteBuffer uBuf = buf.slice();
        buf.position(isYUV420SemiPlanarFormat ? stride * sliceHeight + 1 : 5 * stride * sliceHeight / 4);
        final ByteBuffer vBuf = buf.slice();

        return new FullImageData(size, yBuf, uBuf, vBuf, stride, uvStride, uvStride, yPxStride, uvPxStride, uvPxStride, orient);
    }

    public void stopProcessing() {
        if (mVideoExtractor != null) {
            mVideoExtractor.stop();
            mCallback.onFinished();
        }
    }

    /**
     * Get last available frame
     * @return frameProcessor with filled frameData
     */
    @NonNull
    public FrameProcessor frameProcessor() {
        return mFrameProcessor;
    }

    /**
     * Get timestamp of the current frame.
     * @return timestamp
     */
    public long getFrameTimeNanos() {
        return mTimestamp;
    }

    @Override
    public void attach() {
    }

    @Override
    public void detach() {
    }
}
