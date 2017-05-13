/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.utils;

import android.content.Context;
import android.media.SoundPool;
import android.os.CountDownTimer;

import java.util.List;

import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import timber.log.Timber;

/**
 * Created by Michael on 10/6/2016.
 */

public class Metronome {
    public static final String ACTION_METRONOME_START_STOP = "metronome_start_stop_action";
    private static final long TWENTY_MINUTES = 60000 * 20;
    public static final int MAX_SUBDIVISIONS = 10;
    public static final int MAX_TEMPO = 400;
    public static final int MIN_TEMPO = 15;

    /* Sounds and Such */
    private SoundPool mSoundPool;
    private int mDownBeatClickId, mInnerBeatClickId, mSubdivisionBeatClickId;
    private final float[] mClickVolumes;
    private Context mContext;

    /* Timer, clicker variables */
    private CountDownTimer mTimer;
    private long mDelay;
    private boolean mClicking;

    private MetronomeStartStopListener mListener;
    private ProgrammedMetronomeListener mPListener;

//    private static final Metronome instance = new Metronome();
//
//    public static Metronome getInstance() {
//        return instance;
//    }
    public Metronome(Context context) {

        mContext = context;

        mSoundPool = ClickSounds.getSoundPool(mContext);

        mClicking = false;

        mClickVolumes = new float[MAX_SUBDIVISIONS];
        for(int i = 0; i < MAX_SUBDIVISIONS; i++) {
            mClickVolumes[i] = 1.0f;
        }

    }
//
//    public void setContext(Context context) {
//        mContext = context;
//    }

    public void setMetronomeStartStopListener(MetronomeStartStopListener ml) {
        mListener = ml;
    }

    public void setProgrammedMetronomeListener(ProgrammedMetronomeListener pl) {
        mPListener = pl;
    }

    public boolean isRunning() {
        return mClicking;
    }
    /**
     * Simple metronome click: takes either int or float tempo marking, and number of
     * beats per measure, calculates delay between clicks in millis,
     * starts simple timer, and clicks at defined intervals.
     */
    public void play(int tempo, int beats) {
        getClicksFromSharedPrefs();
        mDelay = 60000 / tempo;
        if(beats == 1) {
            startClicking();
        } else {
            playMeasures(beats);
        }
    }
    public void play(float tempo, int beats) {
        getClicksFromSharedPrefs();
        float delay = 60000f / tempo;
        mDelay = (int) delay;
        if(beats == 1) {
            startClicking();
        } else {
            playMeasures(beats);
        }
    }

    /**
     * Simple, single click metronome start
     */
    private void startClicking() {
        Timber.d("int delay: "+ mDelay);

        mTimer = new CountDownTimer(TWENTY_MINUTES, mDelay) {
            @Override
            public void onTick(long millisUntilFinished) {
                mSoundPool.play(mDownBeatClickId, mClickVolumes[0], mClickVolumes[0], 1, 0, 1.0f);
            }

            @Override
            public void onFinish() {
                mClicking = false;
//                mListener.metronomeStartStop(); Do something to change UI on listener end...
            }
        };
        mClicking = true;
        mTimer.start();
    }

    /**
     * Click with number of subdivisions metronome start
     * @param subs
     */
    private void playMeasures(final int subs) {
//        logSubdivisionVolumes(subs);

        mTimer = new CountDownTimer(TWENTY_MINUTES, mDelay) {
            int subCount = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                if(subCount == 0) {
                    mSoundPool.play(mDownBeatClickId, mClickVolumes[subCount], mClickVolumes[subCount], 1, 0, 1.0f);
                } else {
                    mSoundPool.play(mInnerBeatClickId, mClickVolumes[subCount], mClickVolumes[subCount], 1, 0, 1.0f);
                }
                if(++subCount == subs) subCount = 0;
            }

            @Override
            public void onFinish() {
                mClicking = false;
            }
        };
        mClicking = true;
        mTimer.start();
    }
//
//    private void logSubdivisionVolumes(int subs) {
//        StringBuilder sb = new StringBuilder("Subdivision Volumes: ");
//        for(int i = 0; i < subs; i++) {
//            sb.append(mClickVolumes[i]).append(", ");
//        }
//        Timber.d(sb.toString());
//    }

    /**
     * Programmed click, accepts a PieceOfMusic to define changing click patterns, and a
     * tempo marking.
     * @param p
     * @param tempo
     */
    public void play(PieceOfMusic p, int tempo) {
        getClicksFromSharedPrefs();
        Timber.d("metronome play()");
        if(p.getTempoMultiplier() != 0) {
            Timber.d("tempo multiplier!! " + p.getTempoMultiplier());
            tempo *= p.getTempoMultiplier();
        }
        mDelay = 60000 / p.getSubdivision() / tempo;
//        Timber.d(p.toString());
        final int[] beats = Utilities.integerListToArray(p.getBeats());
        final int[] downBeats = Utilities.integerListToArray(p.getDownBeats());
        final int countOffSubs = p.getCountOffSubdivision();
        final int measureNumberOffset = p.getMeasureCountOffset();


        mTimer = new CountDownTimer(TWENTY_MINUTES, mDelay) {
            int nextClick = 0;  // number of subdivisions in 'this' beat, before next click
            int count = 0;      // count of subdivisions since last click
            int beatPointer = 0; // pointer to move through beats array
            int beatsPerMeasureCount = 0; // count of beats since last downbeat
            int measurePointer = 0; //pointer to move through downbeats array

            @Override
            public void onTick(long millisUntilFinished) {
                if (count == nextClick) {
                    if(beatsPerMeasureCount == 0) { // It's a downbeat!
                        mSoundPool.play(mDownBeatClickId, 1.0f, 1.0f, 1, 0, 1.0f);
                        // start counting until next downbeat
                        beatsPerMeasureCount = downBeats[measurePointer];
                        mPListener.metronomeMeasureNumber( (measureNumberOffset + measurePointer++) + "");
                    } else { // inner beat
                        mSoundPool.play(mInnerBeatClickId, 1.0f, 1.0f, 1, 0, 1.0f);

                    }
                    // if we've reached the end of the piece, stop the metronome.
                    if(beatPointer == beats.length - 1) {
                        stop();
                        mPListener.metronomeStopAndShowAd();
                    }
                    nextClick += beats[beatPointer++]; // set the subdivision counter for next beat
                    beatsPerMeasureCount--; // count down one beat in the measure

                    if(measurePointer == 1) {
//                        Log.d(TAG, "Countoff Measure, beat" + beatPointer);
                        if(beatPointer >= 3 + countOffSubs) {
                            mPListener.metronomeMeasureNumber("GO");
                        } else if (beatPointer >= 3) {
                            mPListener.metronomeMeasureNumber("READY");
                        } else {
                            mPListener.metronomeMeasureNumber((beatPointer) + "");
                        }
                    }
                }
                count++; // count one subdivision gone by...
            }

            @Override
            public void onFinish() {
                this.cancel();
                mClicking = false;
            }

        };
        mClicking = true;
        mTimer.start();
    }

    /**
     * Method accepts tempo and groupings, loops through groupings
     * @param tempo
     * @param groupings
     */
    public void play(int tempo, List<Integer> groupings, final boolean includeSubs) {
        getClicksFromSharedPrefs();

        Timber.d("play an odd-meter loop");

        final int[] beats = Utilities.integerListToArray(groupings);

        Timber.d("beat loop length: " + beats.length);

        mDelay = 60000 / tempo;


        mTimer = new CountDownTimer(TWENTY_MINUTES, mDelay) {
            int nextClick = 0;  // number of subdivisions in 'this' beat, before next click
            int count = 0;      // count of subdivisions since last click
            int beatPointer = 0; // pointer to move through beats array

            @Override
            public void onTick(long millisUntilFinished) {

                if (count == nextClick) {
                    if(beatPointer == beats.length || beatPointer == 0) { // It's a downbeat!
                        mSoundPool.play(mDownBeatClickId, 1.0f, 1.0f, 1, 0, 1.0f);
                    } else { // inner beat
                        mSoundPool.play(mInnerBeatClickId, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                    // if we've reached the end of the array, loop back to beginning.
                    if(beatPointer == beats.length) {
                        beatPointer = 0;
                    }
                    nextClick += beats[beatPointer++]; // set the subdivision counter for next beat

                } else if (includeSubs) {
                    mSoundPool.play(mSubdivisionBeatClickId, 1.0f, 1.0f, 1, 0, 1.0f);
                }
                count++; // count one subdivision gone by...
            }

            @Override
            public void onFinish() {
                this.cancel();
                mClicking = false;
            }
        };
        mClicking = true;
        mTimer.start();
    }

    public void stop() {
        mTimer.cancel();
        mClicking = false;
    }

    public void setClickVolumes(int[] vols) {
        int num = vols.length > MAX_SUBDIVISIONS ? MAX_SUBDIVISIONS : vols.length;
        for(int i = 0; i < num; i++) {
            mClickVolumes[i] = vols[i] / 10.0f;
        }
    }

    private void getClicksFromSharedPrefs() {
        mDownBeatClickId = PrefUtils.getDownBeatClickId(mContext);
        mInnerBeatClickId = PrefUtils.getInnerBeatClickId(mContext);
        mSubdivisionBeatClickId = PrefUtils.getSubdivisionBeatClickId(mContext);
    }
}
