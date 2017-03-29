package tech.michaeloverman.android.mscount.pojos;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tech.michaeloverman.android.mscount.utils.Utilities;

/**
 * Created by Michael on 2/20/2017.
 */

public class PieceOfMusic {
    private static final String TAG = PieceOfMusic.class.getSimpleName();
    public static final int COUNTOFF_LENGTH = 4;
    public static final int SIXTEENTH = 1;
    public static final int DOTTED_SIXTEENTH = 5;
    public static final int EIGHTH = 2;
    public static final int DOTTED_EIGHTH = 3;
    public static final int QUARTER = 4;
    public static final int DOTTED_QUARTER = 6;
    public static final int HALF = 8;
    public static final int DOTTED_HALF = 12;
    public static final int WHOLE = 16;
    private static final int DEFAULT_DEFAULT_TEMPO = 120;

    private String mTitle;
    private String mAuthor;
    private List<Integer> mBeats;
    private List<Integer> mDownBeats;
    private int mSubdivision;
    private int[] mCountOff;
    private int mCountOffSubdivision;
    private int mCountOffMeasureLength;
    private int mDefaultTempo;
    private double mTempoMultiplier;
    private int mBaselineNoteValue;
    private int mMeasureCountOffset;
    private List<DataEntry> mRawData;
    private String mFirebaseId;

    public PieceOfMusic(String title) {
        Log.d(TAG, "PieceOfMusic constructor()");
        mTitle = title;
    }

    public PieceOfMusic() { }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
//        Log.d(TAG, "setTitle()" + title);
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
//        Log.d(TAG, "setAuthor()" + author);
    }

    public int getSubdivision() {
        return mSubdivision;
    }

    public void setSubdivision(int subdivision) {
        mSubdivision = subdivision;
//        mCountOffSubdivision = mSubdivision;
//        Log.d(TAG, "setSubdivision()" + subdivision + " COsub: " + mCountOffSubdivision);
    }

    public int getCountOffSubdivision() {
        return mCountOffSubdivision == 0 ? mSubdivision : mCountOffSubdivision;
    }

    public void setCountOffSubdivision(int countOffSubdivision) {
        mCountOffSubdivision = countOffSubdivision;
//        Log.d(TAG, "setCountOffSubdivision()" + countOffSubdivision + " sub: " + mSubdivision);
    }

    public int getDefaultTempo() {
        return mDefaultTempo;
    }

    public void setDefaultTempo(int defaultTempo) {
        mDefaultTempo = defaultTempo;
//        Log.d(TAG, "setDefaultTempo()" + defaultTempo);
    }

    public double getTempoMultiplier() {
        return mTempoMultiplier;
    }

    public void setTempoMultiplier(double tempoMultiplier) {
        mTempoMultiplier = tempoMultiplier;
//        Log.d(TAG, "setTempoMultiplier" + tempoMultiplier);
    }

    public int getBaselineNoteValue() {
        return mBaselineNoteValue;
    }

    public void setBaselineNoteValue(int baselineNoteValue) {
        mBaselineNoteValue = baselineNoteValue;
//        Log.d(TAG, "setBaselineNoteValue() " + baselineNoteValue);
    }

    public int getMeasureCountOffset() {
        return mMeasureCountOffset;
    }

    public void setMeasureCountOffset(int measureCountOffset) {
        mMeasureCountOffset = measureCountOffset;
//        Log.d(TAG, "setMeasureCountOffset()" +measureCountOffset);
    }

    public List<Integer> getBeats() {
        return mBeats;
    }

    public String getFirebaseId() {
        return mFirebaseId;
    }
    /**
     * Accepts array of the length of each beat, by number of primary subdivisions,
     * combines that with a generated count off measure, and saves the entire array.
     *
     * @param beats
     */
    public void setBeats(List<Integer> beats) {
//        int[] countoff = buildCountoff(mSubdivision);
//        int[] allBeats = combine(countoff, beats);
//        mBeats = new ArrayList<>();
//        for(int i = 0; i < allBeats.length; i++) {
//            mBeats.add(allBeats[i]);
//        }
//        printArray(mBeats);
        mBeats = beats;
    }

    public void setRawData(List<DataEntry> data) {

        mRawData = data;
    }

    public List<DataEntry> getRawData() {
        return mRawData;
    }

    public String getRawDataAsString() {
        StringBuilder rawString = new StringBuilder();
        for(DataEntry d : mRawData) {
            rawString.append(d);
            rawString.append("\n");
        }
        return rawString.toString();
    }

    /** Uses the 'length' of first beat to generate count off measure */
    public void buildCountoff() {
        mCountOffMeasureLength = COUNTOFF_LENGTH + mCountOffSubdivision - 1;
        mCountOff = new int[mCountOffMeasureLength];
        int i;
        for (i = 0; i < mCountOff.length; ) {
            if (i != COUNTOFF_LENGTH - 2) {
                mCountOff[i++] = mSubdivision;
            } else {
                for (int j = 0; j < mCountOffSubdivision; j++) {
                    mCountOff[i++] = mSubdivision / mCountOffSubdivision;
                }
            }
        }
        Utilities.appendCountoff(mCountOff, mBeats, mDownBeats);
    }

    public int[] countOffArray() {
        return mCountOff;
    }



    public List<Integer> getDownBeats() {
        return mDownBeats;
    }

    public void setDownBeats(List<Integer> downBeats) {
//        int[] allDownBeats = new int[downBeats.length + 1];
//        allDownBeats[0] = mCountOffMeasureLength;
//        System.arraycopy(downBeats, 0, allDownBeats, 1, downBeats.length);
//
//        mDownBeats = new ArrayList<>();
//        for (int i = 0; i < allDownBeats.length; i++) {
//            mDownBeats.add(allDownBeats[i]);
//        }
        mDownBeats = downBeats;
    }

    public static class Builder {
        private String title;
        private String author;
        private List<Integer> beats;
        private List<Integer> downBeats;
        private List<DataEntry> rawData;
        private int subdivision;
        private int countOffSubdivision;
        private int defaultTempo;
        private double tempoMultiplier;
        private int baselineNoteValue;
        private int measureCountOffset;
        private String firebaseId;

        public Builder() {

        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }
        public Builder author(String author) {
            this.author = author;
            return this;
        }
        public Builder beats(List<Integer> beats) {
            this.beats = beats;
            return this;
        }
//        public Builder beats(int[] beats) {
//            return beats(Utilities.arrayToIntegerList(beats));
//        }
        public Builder downBeats(List<Integer> downBeats) {
            this.downBeats = downBeats;
            return this;
        }
//        public Builder downBeats(int[] downBeats) {
//            return downBeats(Utilities.arrayToIntegerList(downBeats));
//        }
        public Builder subdivision(int sub) {
            this.subdivision = sub;
            return this;
        }
        public Builder countOffSubdivision(int coSub) {
            this.countOffSubdivision = coSub;
            return this;
        }
        public Builder defaultTempo(int tempo) {
            this.defaultTempo = tempo;
            return this;
        }
        public Builder tempoMultiplier(double mult) {
            this.tempoMultiplier = mult;
            return this;
        }
        public Builder baselineNoteValue(int value) {
            this.baselineNoteValue = value;
            return this;
        }
        public Builder firstMeasureNumber(int offset) {
            this.measureCountOffset = offset - 1;
            return this;
        }
        public Builder dataEntries(List<DataEntry> data) {
            this.rawData = data;
            this.beats = new ArrayList<>();
            this.downBeats = new ArrayList<>();

            int start = data.get(0).isBarline() ? 1 : 0;

            int beatsPerMeasureCount = 0;

            for(int i = start; i < data.size(); i++) {

                if(data.get(i).isBarline()) {
                    this.downBeats.add(beatsPerMeasureCount);
                    beatsPerMeasureCount = 0;
                } else {
                    this.beats.add(data.get(i).getData());
                    beatsPerMeasureCount++;
                }
            }

            return this;
        }
        public Builder dataEntries(String data) {
            String[] entries = data.split("\\n");
            List<DataEntry> list = new ArrayList<>();
            for(int i = 0; i < entries.length; i++) {
                String[] entry = entries[i].split(";");
                list.add(new DataEntry(Integer.parseInt(entry[0]), Boolean.parseBoolean(entry[1])));
            }
            return dataEntries(list);
        }
        public Builder firebaseId(String id) {
            this.firebaseId = id;
            return this;
        }
        public PieceOfMusic build() {
            return new PieceOfMusic(this);
        }
    }

    private PieceOfMusic(Builder builder) {
        mTitle = builder.title;
        mAuthor = builder.author;
        mBeats = builder.beats;
        mDownBeats = builder.downBeats;
        mSubdivision = builder.subdivision;
        mCountOffSubdivision = builder.countOffSubdivision;
        mMeasureCountOffset = builder.measureCountOffset;
        mDefaultTempo = builder.defaultTempo == 0 ? DEFAULT_DEFAULT_TEMPO : builder.defaultTempo;
        mTempoMultiplier = builder.tempoMultiplier == 0.0 ? 1.0 : builder.tempoMultiplier;
        mBaselineNoteValue = builder.baselineNoteValue == 0 ? QUARTER : builder.baselineNoteValue;
        mRawData = builder.rawData;
        mFirebaseId = builder.firebaseId;

        buildCountoff();
    }
}
