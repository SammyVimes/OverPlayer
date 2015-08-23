package com.danilov.overplayer.core;

import android.app.Application;
import android.content.Context;

import com.danilov.overplayer.core.cache.BitmapMemoryCache;
import com.danilov.overplayer.core.image.LocalImageManager;

/**
 * Created by Semyon on 23.08.2015.
 */
public class OverPlayerApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        BitmapMemoryCache bmc = new BitmapMemoryCache(0.4f);
        LocalImageManager localImageManager = new LocalImageManager(bmc, getResources());
        ServiceContainer.addService(localImageManager);
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ex.printStackTrace();
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }

    public static Context getContext() {
        return context;
    }

}