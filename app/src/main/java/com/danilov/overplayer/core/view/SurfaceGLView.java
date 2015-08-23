package com.danilov.overplayer.core.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Semyon on 23.08.2015.
 */
public class SurfaceGLView extends GLSurfaceView implements GLSurfaceView.Renderer {

    public SurfaceGLView(final Context context) {
        super(context);
        setRenderer(this);
    }

    public SurfaceGLView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {

    }

    @Override
    public void onDrawFrame(final GL10 gl) {

    }

}