package com.danilov.overplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.danilov.overplayer.core.BaseToolbarActivity;
import com.danilov.overplayer.core.ServiceContainer;
import com.danilov.overplayer.core.VideoUtils;
import com.danilov.overplayer.core.adapter.BaseAdapter;
import com.danilov.overplayer.core.image.LocalImageManager;
import com.danilov.overplayer.core.image.VideoThumbnailRequest;
import com.danilov.overplayer.core.model.Video;

import java.util.List;


public class AllVideosActivity extends BaseToolbarActivity implements AdapterView.OnItemClickListener {

    private GridView videosView;

    private LocalImageManager localImageManager = null;
    private int thumbSize = 120;
    private VideoAdapter videoAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localImageManager = ServiceContainer.getService(LocalImageManager.class);
        setContentView(R.layout.activity_all_videos);
        videosView = findViewWithId(R.id.videos);
        videosView.setOnItemClickListener(this);
        thumbSize = (int) getResources().getDimension(R.dimen.grid_item_height);
        VideoUtils.listDeviceVideo(videoLoadListener);
    }

    private VideoUtils.VideoLoadListener videoLoadListener = new VideoUtils.VideoLoadListener() {
        @Override
        public void onVideosLoaded(List<Video> videos) {
            if (videos != null) {
                videoAdapter = new VideoAdapter(AllVideosActivity.this, R.layout.video_grid_item, videos);
                videosView.setAdapter(videoAdapter);
            }
        }
    };

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        Video item = videoAdapter.getItem(position);
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra(VideoActivity.VIDEO_KEY, item);
        startActivity(intent);
    }


    private class VideoAdapter extends BaseAdapter<VideoHolder, Video>{

        public VideoAdapter(Context context, int resource, List<Video> objects) {
            super(context, resource, objects);
        }

        @Override
        public void onBindViewHolder(VideoHolder holder, int position) {
            Video item = getItem(position);
            holder.videoTitle.setText(item.getTitle());

            VideoThumbnailRequest videoThumbnailRequest = new VideoThumbnailRequest(item.getId());
            videoThumbnailRequest.init(holder.videoThumbnail, "uri" + item.getId(), thumbSize);
            Bitmap bitmap = localImageManager.loadBitmap(videoThumbnailRequest);
            if (bitmap != null) {
                holder.videoThumbnail.setImageBitmap(bitmap);
            }
        }

        @Override
        public VideoHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            LayoutInflater inflater = LayoutInflater.from(AllVideosActivity.this);
            return new VideoHolder(inflater.inflate(R.layout.video_grid_item, viewGroup, false));
        }

    }

    private class VideoHolder extends BaseAdapter.BaseHolder {

        private TextView videoTitle;
        private ImageView videoThumbnail;

        protected VideoHolder(View view) {
            super(view);
            videoTitle = findViewById(R.id.video_title);
            videoThumbnail = findViewById(R.id.video_cover);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_videos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}