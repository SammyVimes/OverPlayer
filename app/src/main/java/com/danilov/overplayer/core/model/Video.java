package com.danilov.overplayer.core.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Semyon on 23.08.2015.
 */
public class Video implements Parcelable {

    private long id;

    private String title;

    private String path;

    private Video(final Parcel parcel) {
        this.id = parcel.readLong();
        this.title = parcel.readString();
        this.path = parcel.readString();
    }

    public Video(long id, String title, String path) {
        this.id = id;
        this.title = title;
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(final Parcel source) {
            return new Video(source);
        }

        @Override
        public Video[] newArray(final int size) {
            return new Video[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(path);
    }

}
