package com.banuba.sdk.example.effect_player_realtime_preview.media;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class AudioExtractor extends BaseExtractor {

    private static final String TAG = "AudioExtractor";

    @NonNull
    public MediaFormat getMediaFormat() {
        return mMediaFormat;
    }

    @NonNull
    public static AudioExtractor getAudioExtractor(
            @NonNull Context context,
            @NonNull Uri sourceFileUri
    ) throws IOException {
        return new AudioExtractor(context, sourceFileUri);
    }

    private AudioExtractor(@NonNull Context context, @NonNull Uri sourceFileUri) throws IOException {
        super(context, sourceFileUri, "audio/");
    }

    @NonNull
    public LinkedList<Pair<MediaCodec.BufferInfo, ByteBuffer>> extractAudioStream(int stopSec) {
        MediaExtractor extractor = null;

        final long stopMicroSec = TimeUnit.SECONDS.toMicros(stopSec);

        final LinkedList<Pair<MediaCodec.BufferInfo, ByteBuffer>> result = new LinkedList<>();

        try {
            extractor = new MediaExtractor();
            setupDataSource(extractor);
            extractor.selectTrack(mTrackIndex);

            try {
                doAudioExtract(result, extractor, stopMicroSec);
            } catch (Exception e) {
                Log.w(TAG, "Error while extracting", e);
            }

        } catch (Exception e) {
            // Do nothing
        } finally {

            try {

                if (extractor != null) {
                    extractor.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }


    private void doAudioExtract(@NonNull List<Pair<MediaCodec.BufferInfo, ByteBuffer>> pair, @NonNull MediaExtractor extractor, long stopMicroSec) {

        boolean isAudioData = true;

        final ByteBuffer audioBuffer = ByteBuffer.allocateDirect(1024 * 1024);

        while (isAudioData) {

            audioBuffer.limit(audioBuffer.capacity());
            final long timeAudioMicroSec = extractor.getSampleTime();
            final int audioBufferSize = extractor.readSampleData(audioBuffer, 0);

            final boolean keyframe = isKeyFrame(extractor);
            final boolean partial = isPartialFrame(extractor);

            isAudioData = extractor.advance();

            if (timeAudioMicroSec >= stopMicroSec) {
                isAudioData = false;
            }

            if (audioBufferSize > 0) {
                final int flags = makeFlags(isAudioData, keyframe, partial);
                final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                info.set(0, audioBufferSize, timeAudioMicroSec, flags);
                audioBuffer.limit(audioBufferSize);
                final ByteBuffer audio = ByteBuffer.allocateDirect(audioBufferSize);
                audio.put(audioBuffer);
                pair.add(new Pair<>(info, audio));
            }
        }
    }

    private static boolean isKeyFrame(@NonNull MediaExtractor extractor) {
        return (extractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) > 0;
    }

    private static boolean isPartialFrame(@NonNull MediaExtractor extractor) {
        boolean partial = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            partial = (extractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) > 0;
        }
        return partial;
    }

    private static int makeFlags(boolean hasData, boolean keyframe, boolean partial) {
        int flags = 0;
        if (keyframe) {
            flags = flags | MediaCodec.BUFFER_FLAG_KEY_FRAME;
        }
        if (!hasData) {
            flags = flags | MediaCodec.BUFFER_FLAG_END_OF_STREAM;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (partial) {
                flags = flags | MediaCodec.BUFFER_FLAG_PARTIAL_FRAME;
            }
        }
        return flags;
    }

}
