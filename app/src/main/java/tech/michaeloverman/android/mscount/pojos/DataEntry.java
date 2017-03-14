package tech.michaeloverman.android.mscount.pojos;

/**
 * Created by Michael on 3/13/2017.
 */

public class DataEntry {
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

}
