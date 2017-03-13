package tech.michaeloverman.android.mscount.pojos;

/**
 * Created by Michael on 3/13/2017.
 */

public class DataEntry {
    private static int measureNumberCount;
    private int mCount;
    private boolean isBarline;
    private int measureNumber;

    public DataEntry(int count, boolean bar) {
        mCount = count;
        isBarline = bar;
        if(isBarline) {
            measureNumberCount++;
            measureNumber = measureNumberCount;
        }
    }

    public boolean isBarline() {
        return isBarline;
    }

    public int getData() {
        return mCount;
    }

    public void decreaseMeasureNumber() {
        if(isBarline) mCount--;
    }
    public void increaseMeasureNumber() {
        if(isBarline) mCount++;
    }

}
