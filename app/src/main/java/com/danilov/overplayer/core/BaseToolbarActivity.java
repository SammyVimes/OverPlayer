package com.danilov.overplayer.core;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Semyon on 06.12.2014.
 */
public class BaseToolbarActivity extends ActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public <T> T findViewWithId(final int id) {
        return (T) super.findViewById(id);
    }

}
