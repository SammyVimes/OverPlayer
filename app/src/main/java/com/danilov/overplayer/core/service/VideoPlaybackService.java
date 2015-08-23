package com.danilov.overplayer.core.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.danilov.overplayer.R;
import com.danilov.overplayer.core.OverPlayerApplication;
import com.danilov.overplayer.core.model.Video;
import com.danilov.overplayer.core.view.SurfaceGLView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Semyon on 23.08.2015.
 */
public class VideoPlaybackService extends Service {

    private Video video;
    private int currentPosition;
    private boolean newVideo;
    private SeekBar seekBar;
    private boolean isMediaPlayerReady = false;

    public void setVideo(final Video video) {
        this.video = video;
        this.newVideo = true;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        return START_STICKY;
    }

    public void setSeekBar(final SeekBar seekBar) {
        this.seekBar = seekBar;
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private int progress = 0;

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                if (mediaPlayer != null) {
                    int duration = mediaPlayer.getDuration();
                    int seekTo = (int) ((float) duration / 100 * progress);
                    mediaPlayer.seekTo(seekTo);
                }
            }
        });
    }

    private class UpdateTrackTask extends TimerTask {
        @Override
        public void run() {
            if (mediaPlayer != null && seekBar != null && isMediaPlayerReady) {
                try {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    float progress = (float) currentPosition / duration * 100;
                    seekBar.setProgress((int) progress);
                } catch (IllegalStateException e) {

                }
            }
        }
    };

    private Timer timer = null;

    private void startUpdateTask() {
        cancelUpdateTask();
        timer = new Timer();
        timer.schedule(new UpdateTrackTask(), 1000, 1000);
    }

    private void cancelUpdateTask() {
        if (timer != null) {
            timer.cancel();
        }
    }


    public int getCurrentPosition() {
        return currentPosition;
    }

    public Video getVideo() {
        return video;
    }

    private MediaPlayer mediaPlayer;

    public void startVideoPlaybackFromActivity(final Surface surface) {
        if (mediaPlayer != null && isMediaPlayerReady) {
            currentPosition =  mediaPlayer.getCurrentPosition();
        }
        isGlobal = false;
        startVideoPlayback(surface);
        if (globalView != null) {
            final Context context = OverPlayerApplication.getContext().getApplicationContext();
            final WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            wm.removeView(globalView);
        }
    }

    private Handler handler = new Handler();

    public void startVideoPlayback(final Surface surface) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        isMediaPlayerReady = false;
        try {
            mediaPlayer.setDataSource(video.getPath());
            mediaPlayer.prepare();
            isMediaPlayerReady = true;
            mediaPlayer.setSurface(surface);
            if (!newVideo) {
                mediaPlayer.seekTo(currentPosition);
            }
            newVideo = false;
            adjustVideoSize(mediaPlayer);

            mediaPlayer.start();
            startUpdateTask();
//            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isGlobal = false;
    private View globalView = null;

    public void switchToOverlay() {
        if (mediaPlayer != null && isMediaPlayerReady) {
            currentPosition =  mediaPlayer.getCurrentPosition();
        }
        isGlobal = true;
        final Context context = OverPlayerApplication.getContext().getApplicationContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        globalView = layoutInflater.inflate(R.layout.overlay_video, null, false);
        holder = (FrameLayout) globalView;
        SurfaceGLView videoView = (SurfaceGLView) holder.findViewById(R.id.video);
        this.videoView = videoView;
        setSeekBar((SeekBar) holder.findViewById(R.id.seek_bar));

        final WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        int screenOrientation = getScreenOrientation(wm);
        switch (screenOrientation) {
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                width = width / 3;
                height = height / 3;
                break;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                width = width;
                height = height / 3;
                break;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params.x = 0;
        params.y = 0;
        wm.addView(holder, params);
        videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                if (isGlobal) {
                    startVideoPlayback(holder.getSurface());
                }
            }

            @Override
            public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {

            }

            @Override
            public void surfaceDestroyed(final SurfaceHolder holder) {

            }
        });
        View.OnClickListener onVideoClickListener = new View.OnClickListener() {

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
        holder.setOnClickListener(onVideoClickListener);
        holder.setOnTouchListener(new View.OnTouchListener() {

            private int startX;
            private int startY;

            private int startViewX;
            private int startViewY;

            private long startTime;

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startTime = System.currentTimeMillis();
                        startViewX = params.x;
                        startViewY = params.y;
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        int dX = x - startX;
                        int dY = y - startY;
                        int newX = startViewX - dX;
                        int newY = startViewY - dY;
                        params.x = newX;
                        params.y = newY;
                        wm.updateViewLayout(holder, params);
                        break;
                    case MotionEvent.ACTION_UP:
                        long now = System.currentTimeMillis();
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        dX = x - startX;
                        dY = y - startY;
                        if (dX < 5 && dY < 5 && (now - startTime < 150)) {
                            holder.performClick();
                        }
                        break;
                }
                return true;
            }
        });
    }

    private int getScreenOrientation(final WindowManager windowManager) {
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int rotation = defaultDisplay.getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        defaultDisplay.getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }


    private ViewGroup holder;
    private View videoView;

    public void setHolder(final FrameLayout holder) {
        this.holder = holder;
    }

    public void setVideoView(final View videoView) {
        this.videoView = videoView;
    }

    private void adjustVideoSize(final MediaPlayer mediaPlayer) {
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;

        // Get the width of the screen
        int screenWidth = holder.getMeasuredWidth();
        int screenHeight = holder.getMeasuredHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;

        // Get the SurfaceView layout parameters
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            int newHeight = (int) ((float) ((float) videoHeight * (float) screenWidth  / (float) videoWidth));
            lp.height = newHeight;
            int margin = (screenHeight - newHeight) / 2;
            lp.setMargins(0, margin, 0, margin);
        } else {
            int newWidth = (int) (videoProportion * (float) screenHeight);
            lp.width = newWidth;
            int margin = (screenWidth - newWidth) / 2;
            lp.setMargins(margin, 0, margin, 0);
            lp.height = screenHeight;
        }
        // Commit the layout parameters
        videoView.setLayoutParams(lp);
    }

    public static VServiceConnection bindService(final Context context, final ServiceListener listener) {
        VServiceConnection serviceConnection = new VServiceConnection(listener);
        Intent intent = new Intent(context, VideoPlaybackService.class);
        Intent startServiceIntent = new Intent(context, VideoPlaybackService.class);
        context.startService(startServiceIntent);
        context.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        return serviceConnection;
    }

    public static class VServiceConnection implements ServiceConnection {

        private ServiceListener serviceListener;

        private VideoPlaybackService mService;

        public VServiceConnection(final ServiceListener serviceListener) {
            this.serviceListener = serviceListener;
        }

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mService = ((MyBinder) service).getService();
            serviceListener.onServiceConnected(mService);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            serviceListener.onServiceDisconnected(mService);
        }

    }

    public static interface ServiceListener {

        public void onServiceConnected(final VideoPlaybackService service);

        public void onServiceDisconnected(final VideoPlaybackService service);

    }

    @Override
    public IBinder onBind(final Intent intent) {
        return new MyBinder();
    }

    private class MyBinder extends Binder {

        public VideoPlaybackService getService() {
            return VideoPlaybackService.this;
        }

    }

}
