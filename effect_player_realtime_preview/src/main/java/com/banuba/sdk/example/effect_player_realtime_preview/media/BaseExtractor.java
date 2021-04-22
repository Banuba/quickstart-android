package com.banuba.sdk.example.effect_player_realtime_preview.media;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class BaseExtractor {

    protected final Context mContext;
    protected final Uri mSourceFileUri;
    protected final int mTrackIndex;
    protected final MediaFormat mMediaFormat;

    protected BaseExtractor(
            @NonNull Context context,
            @NonNull Uri sourceFileUri,
            @NonNull String mimePrefix
    ) throws IOException {

        mContext = context;
        mSourceFileUri = sourceFileUri;

        MediaExtractor extractor = null;
        try {
            extractor = new MediaExtractor();
            setupDataSource(extractor);

            mTrackIndex = findTrackByType(extractor, mimePrefix);
            if (mTrackIndex == -1) {
                throw new RuntimeException("Track for " + mimePrefix + " not found");
            }

            extractor.selectTrack(mTrackIndex);
            mMediaFormat = extractor.getTrackFormat(mTrackIndex);

        } finally {
            if (extractor != null) {
                extractor.release();
            }
        }

    }


    protected void setupDataSource(@NonNull MediaExtractor extractor) throws IOException {

        if (mSourceFileUri == Uri.EMPTY) {
            throw new FileNotFoundException("Unable to read " + mSourceFileUri);
        }
        extractor.setDataSource(mContext, mSourceFileUri, null);

    }

    protected int findTrackByType(MediaExtractor extractor, String mimePrefix) {
        final int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            final MediaFormat format = extractor.getTrackFormat(i);
            final String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith(mimePrefix)) {
                return i;
            }
        }
        return -1;
    }

}
