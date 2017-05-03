/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.pojos;

import android.support.annotation.NonNull;

import timber.log.Timber;


/**
 * Created by Michael on 2/25/2017.
 */

public class TitleKeyObject implements Comparable {
    
    String mTitle;
    String mKey;

    public TitleKeyObject(String title, String key) {
        Timber.d("TitleKeyObject constructor(): " + title + ", " + key);
        mTitle = title;
        mKey = key;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        TitleKeyObject other = (TitleKeyObject) o;
        return mTitle.compareTo(other.mTitle);
    }
}
