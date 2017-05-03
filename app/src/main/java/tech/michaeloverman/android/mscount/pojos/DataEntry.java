/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.pojos;

import java.io.Serializable;

/**
 * Created by Michael on 3/13/2017.
 */

public class DataEntry implements Serializable {
    private static int measureNumberCount;
    private int mData;
    private boolean isBarline;
    private int measureNumber;

    public DataEntry(int data, boolean bar) {
        mData = data;
        isBarline = bar;
        if(isBarline) {
            measureNumberCount++;
            measureNumber = measureNumberCount;
        }
    }

    public DataEntry() { }

    public boolean isBarline() {
        return isBarline;
    }

    public void setBarline(boolean is) {
        isBarline = is;
    }

    public int getData() {
        return mData;
    }

    public void setData(int data) {
        mData = data;
    }

    public void decreaseMeasureNumber() {
        if(isBarline) mData--;
    }
    public void increaseMeasureNumber() {
        if(isBarline) mData++;
    }

    @Override
    public String toString() {
        return mData + ";" + isBarline;
    }
}
