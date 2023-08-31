package com.banuba.sdk.example.effect_player_realtime_preview.media;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;


public class VideoExtractor extends BaseExtractor {

    private static final String TAG = "VideoExtractor";

    private final DecoderVideoListener mDecoderVideoListener;

    private volatile boolean mStopRequested;
    private int mWidth;
    private int mHeight;
    private int mStride;
    private int mSliceHeight;
    private int mColorFormat;
    private int mRotation;

    public interface DecoderVideoListener {
        void onVideoBufferDecoded(int rotation, int width, int height, int stride, int sliceHeight, int colorFormat, @NonNull ByteBuffer data, @NonNull MediaCodec.BufferInfo bufferInfo);
    }

    @NonNull
    public static VideoExtractor getVideoDecoder(
            @NonNull Context context,
            @NonNull Uri sourceFileUri,
            @Nullable DecoderVideoListener decoderVideoListener
    ) throws IOException {
        return new VideoExtractor(context, sourceFileUri, "video/", decoderVideoListener);
    }

    private VideoExtractor(
            @NonNull Context context,
            @NonNull Uri sourceFileUri,
            @NonNull String mimePrefix,
            @Nullable DecoderVideoListener decoderVideoListener
    ) throws IOException {
        super(context, sourceFileUri, mimePrefix);
        mDecoderVideoListener = decoderVideoListener;
    }

    public void stop() {
        mStopRequested = true;
    }

    public void play() throws IOException {
        MediaExtractor extractor = null;
        MediaCodec decoder = null;

        try {
            extractor = new MediaExtractor();
            setupDataSource(extractor);

            int trackIndex = mTrackIndex;
            extractor.selectTrack(trackIndex);

            final MediaFormat format = extractor.getTrackFormat(trackIndex);

            mWidth = format.getInteger(MediaFormat.KEY_WIDTH);
            mHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
            mRotation = format.containsKey(MediaFormat.KEY_ROTATION) ? format.getInteger(MediaFormat.KEY_ROTATION) : 0;
            mStride = mWidth;
            mSliceHeight = mHeight;

            final String mime = Objects.requireNonNull(format.getString(MediaFormat.KEY_MIME));
            decoder = MediaCodec.createDecoderByType(mime);

            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, detectColorFormat(decoder, mime));

            decoder.configure(format, null, null, 0);
            decoder.start();

            try {

                doExtract(extractor, trackIndex, decoder);
            } catch (Exception e) {
                Log.w(TAG, "Error while extracting", e);
            }

        } finally {
            try {
                if (decoder != null) {
                    decoder.stop();
                    decoder.release();
                }
            } catch (Exception e) {
                Log.w(TAG, "Error while releasing decoder", e);
            }

            try {

                if (extractor != null) {
                    extractor.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @SuppressWarnings("deprecation")
    private int detectColorFormat(@NonNull MediaCodec decoder, @NonNull String mime) {

        // https://source.android.com/compatibility/11/android-11-cdd#5_1_7_video_codecs
        // Video decoders MUST support at least one of a planar or semiplanar YUV420 8:8:8 color format
        final MediaCodecInfo.CodecCapabilities cap = decoder.getCodecInfo().getCapabilitiesForType(mime);
        for (int i = 0; i < cap.colorFormats.length; i++) {
            final int colorFormat = cap.colorFormats[i];
            if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar ||
                    colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                return colorFormat;
            }
        }
        return COLOR_FormatYUV420Flexible;
    }

    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder) {

        final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {

            if (mStopRequested) {
                return;
            }

            // Feed more data to the decoder.
            if (!inputDone) {
                final int inputBufIndex = decoder.dequeueInputBuffer(10000);
                if (inputBufIndex >= 0) {
                    final ByteBuffer inputBuf = decoder.getInputBuffer(inputBufIndex);
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.
                    if (inputBuf != null) {
                        final int chunkSize = extractor.readSampleData(inputBuf, 0);
                        if (chunkSize < 0) {
                            // End of stream -- send empty frame with EOS flag set.
                            decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            if (extractor.getSampleTrackIndex() != trackIndex) {
                                Log.d(TAG, "WTF?! got sample from track " + extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                            }
                            long presentationTimeUs = extractor.getSampleTime();
                            decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                    presentationTimeUs, 0);
                            extractor.advance();
                        }
                    }
                }
            }

            final int decoderStatus = decoder.dequeueOutputBuffer(bufferInfo, 10000);
            //noinspection StatementWithEmptyBody
            if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
            } else //noinspection deprecation
                if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    final MediaFormat format = decoder.getOutputFormat();
                    mColorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
                    mStride = format.containsKey(MediaFormat.KEY_STRIDE) ? format.getInteger(MediaFormat.KEY_STRIDE) : mWidth;
                    mSliceHeight = format.containsKey(MediaFormat.KEY_SLICE_HEIGHT) ? format.getInteger(MediaFormat.KEY_SLICE_HEIGHT) : mHeight;
                    mRotation = format.containsKey(MediaFormat.KEY_ROTATION) ? format.getInteger(MediaFormat.KEY_ROTATION) : mRotation;
                } else if (decoderStatus < 0) {
                    throw new RuntimeException("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }

                    final boolean doRender = (bufferInfo.size != 0);

                    try {

                        ByteBuffer data = decoder.getOutputBuffer(decoderStatus);
                        if (doRender && data != null) {
                            if (mDecoderVideoListener != null) {
                                mDecoderVideoListener.onVideoBufferDecoded(mRotation, mWidth, mHeight, mStride, mSliceHeight, mColorFormat, data, bufferInfo);
                            }
                        }

                    } catch (Exception e) {
                        Log.e(TAG, " MOVIE.PLAYER onBufferDecoded  ERROR = " + e.getMessage());
                    }

                    decoder.releaseOutputBuffer(decoderStatus, doRender);

                }
        }
    }


}
