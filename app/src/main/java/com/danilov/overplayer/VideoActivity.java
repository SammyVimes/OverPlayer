package com.danilov.overplayer;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.danilov.overplayer.core.BaseToolbarActivity;
import com.danilov.overplayer.core.model.Video;
import com.danilov.overplayer.core.service.VideoPlaybackService;
import com.danilov.overplayer.core.view.SurfaceGLView;

/**
 * Created by Semyon on 23.08.2015.
 */
public class VideoActivity extends BaseToolbarActivity implements VideoPlaybackService.ServiceListener, TextureView.SurfaceTextureListener {

    public static final String VIDEO_KEY = "VIDEO";

    private TextureView videoView;
    private TextView titleTextView;
    private SeekBar seekBar;
    private FrameLayout holder;
    private Video video = null;

    private VideoPlaybackService service;
    private VideoPlaybackService.VServiceConnection serviceConnection;
    private boolean isAlreadyRunning = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);
        videoView = findViewWithId(R.id.video_view);
        holder = findViewWithId(R.id.holder);
        seekBar = findViewWithId(R.id.seek_bar);
        titleTextView = findViewWithId(R.id.video_title);
        Intent intent = getIntent();
        if (intent != null) {
            video = intent.getParcelableExtra(VIDEO_KEY);
            if (video == null) {
                finish();
            }
        }
        if (savedInstanceState != null) {
            isAlreadyRunning = true;
        }
        titleTextView.setText(video.getTitle());
        videoView.setSurfaceTextureListener(this);

        holder.setOnClickListener(onVideoClickListener);
        videoView.setOnClickListener(onVideoClickListener);
    }

    private View.OnClickListener onVideoClickListener = new View.OnClickListener() {

        private boolean isHidden = false;

        @Override
        public void onClick(final View v) {
            if (isHidden) {
                seekBar.setVisibility(View.VISIBLE);
            } else {
                seekBar.setVisibility(View.INVISIBLE);
            }
            isHidden = !isHidden;
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        serviceConnection = VideoPlaybackService.bindService(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAlreadyRunning = true;
        if (serviceConnection != null && service != null) {
            service.switchToOverlay();
            serviceConnected = false;
            unbindService(serviceConnection);
        }
    }



    private boolean serviceConnected = false;
    private boolean surfaceCreated = false;

    @Override
    public void onServiceConnected(final VideoPlaybackService service) {
        this.service = service;
        serviceConnected = true;
        checkStart();
    }

    @Override
    public void onServiceDisconnected(final VideoPlaybackService service) {
        this.service = null;
    }

    private void checkStart() {
        if (serviceConnected && surfaceCreated) {
            service.setHolder(holder);
            service.setSeekBar(seekBar);
            service.setVideoView(videoView);
            if (!isAlreadyRunning) {
                service.setVideo(video);
            }
            Surface surface = new Surface(videoView.getSurfaceTexture());
            service.startVideoPlaybackFromActivity(surface);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
        surfaceCreated = true;
        checkStart();
    }

    @Override
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
        surfaceCreated = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(final SurfaceTexture surface) {

    }
}