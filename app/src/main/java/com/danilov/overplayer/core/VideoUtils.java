package com.danilov.overplayer.core;

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.danilov.overplayer.core.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon on 23.08.2015.
 */
public class VideoUtils {

    public static void listDeviceVideo(final VideoLoadListener listener) {
        final Context context = OverPlayerApplication.getContext();
        CursorLoader cursorLoader = new CursorLoader(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                null, // Return all rows
                null, null);
        cursorLoader.registerListener(0, new Loader.OnLoadCompleteListener<Cursor>() {
            @Override
            public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
                List<Video> videos = new ArrayList<Video>();

                int idColumn = data.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int pathColumn = data.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int titleColumn = data.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);

                if(data != null){
                    ContentResolver resolver = context.getContentResolver();
                    while(data.moveToNext()){
                        String filePath = data.getString(pathColumn);
                        long id = data.getLong(idColumn);
                        String title = data.getString(titleColumn);
                        Video video = new Video(id, title, filePath);
                        videos.add(video);
                    }
                }
                listener.onVideosLoaded(videos);
            }
        });
        cursorLoader.loadInBackground();
        cursorLoader.startLoading();
    }

    public static Bitmap getVideoThumbnail(final long videoId) {
        final Context context = OverPlayerApplication.getContext();
        Bitmap thumb = MediaStore.Video.Thumbnails.getThumbnail(
                context.getContentResolver(),
                videoId, MediaStore.Video.Thumbnails.MICRO_KIND, null);
        return thumb;
    }

    public static interface VideoLoadListener {

        void onVideosLoaded(final List<Video> videos);

    }

}
