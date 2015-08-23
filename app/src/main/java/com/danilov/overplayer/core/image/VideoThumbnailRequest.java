package com.danilov.overplayer.core.image;

import android.graphics.Bitmap;

import com.danilov.overplayer.core.VideoUtils;

/**
 * Created by Semyon on 23.08.2015.
 */
public class VideoThumbnailRequest extends LocalImageManager.ImageRequest {

    private long videoId;

    public VideoThumbnailRequest(long videoId) {
        this.videoId = videoId;
    }

    @Override
    public Bitmap load() {
        return VideoUtils.getVideoThumbnail(videoId);
    }

}
