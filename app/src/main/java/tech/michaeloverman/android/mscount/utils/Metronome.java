package tech.michaeloverman.android.mscount.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tech.michaeloverman.android.mscount.pojos.Click;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;

/**
 * Created by Michael on 10/6/2016.
 */

public class Metronome {
    private static final String TAG = "Metronome";
    private static final String SOUNDS_FOLDER = "sample_sounds";
    private static final long TWENTY_MINUTES = 60000 * 20;

    /* Sounds and Such */
    private AssetManager mAssets;
    private List<Click> mClicks = new ArrayList<>();
    private SoundPool mSoundPool;
    private Integer mClickId;
    private Integer mHiClickId, mLoClickId;

    /* Timer, clicker variables */
    private CountDownTimer mTimer;
    private long mDelay;
    private boolean mClicking;

    private MetronomeListener mListener;

    /**
     * Constructor accepts context, though for what is not immediately apparent.
     * Loads sound files for clicking...
     *
     * @param context
     */
    public Metronome(Context context, MetronomeListener ml) {
        mAssets = context.getAssets();
        mListener = ml;
        mClicking = false;
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        loadSounds();
    }
    public Metronome(Context context) {
        this(context, null);
    }

    /**
     * Simple metronome click, takes tempo, establishes simple timer, and clicks at
     * defined intervals.
     *
     * @param tempo
     */
    public void play(int tempo) {
        mDelay = 60000 / tempo;
        mClickId = mClicks.get(0).getSoundId();
//        mHiClickId = mClicks.get(1).getSoundId();
//        mLoClickId = mClicks.get(2).getSoundId();
        if (mClickId == null) {
            return;
        }

        mTimer = new CountDownTimer(TWENTY_MINUTES, mDelay) {
            @Override
            public void onTick(long millisUntilFinished) {
                mSoundPool.play(mClickId, 1.0f, 1.0f, 1, 0, 1.0f);
            }

            @Override
            public void onFinish() {

            }
        };
        mTimer.start();
    }

    /**
     * Programmed click, accepts a PieceOfMusic to define changing click patterns, and a
     * tempo marking.
     * @param p
     * @param tempo
     */
    public void play(PieceOfMusic p, int tempo) {
        Log.d(TAG, "metronome play()");
        if(p.getTempoMultiplier() != 0) {
            Log.d(TAG, "tempo multiplier!! " + p.getTempoMultiplier());
            tempo *= p.getTempoMultiplier();
        }
        mDelay = 60000 / tempo / p.getSubdivision();
        Log.d(TAG, p.toString());
        final int[] beats = Utilities.integerListToArray(p.getBeats());
        final int[] downBeats = Utilities.integerListToArray(p.getDownBeats());
        final int countOffSubs = p.getCountOffSubdivision();

//        Utilities.printArray(beats);
//        Utilities.printArray(downBeats);

        mClickId = mClicks.get(0).getSoundId(); // not using this sound at the moment...
        mHiClickId = mClicks.get(1).getSoundId();
        mLoClickId = mClicks.get(2).getSoundId();
        // if the sounds don't load properly, quit while you can...
        if (mClickId == null) {
            return;
        }

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
                        mSoundPool.play(mHiClickId, 1.0f, 1.0f, 1, 0, 1.0f);
                        // start counting until next downbeat
                        beatsPerMeasureCount = downBeats[measurePointer];
                        mListener.metronomeMeasureNumber(measurePointer++ + "");
                    } else { // inner beat
                        mSoundPool.play(mLoClickId, 1.0f, 1.0f, 1, 0, 1.0f);

                    }
                    // if we've reached the end of the piece, stop the metronome.
                    if(beatPointer == beats.length - 1) {
                        stop();
                        mListener.metronomeStartStop();
                    }
                    nextClick += beats[beatPointer++]; // set the subdivision counter for next beat
                    beatsPerMeasureCount--; // count down one beat in the measure
                }
                count++; // count one subdivision gone by...
                if(measurePointer == 1) {
                    if(beatPointer >= 3 + countOffSubs) {
                        mListener.metronomeMeasureNumber("GO");
                    } else if (beatPointer >= 3) {
                        mListener.metronomeMeasureNumber("READY");
                    } else {
                        mListener.metronomeMeasureNumber((beatPointer) + "");
                    }
                }
            }

            @Override
            public void onFinish() {
                this.cancel();
//                mListener.metronomeStartStop();
            }
        };
        mTimer.start();
    }

    public void stop() {
        mTimer.cancel();
    }

    private void loadSounds() {
        String[] soundNames;
        try {
            soundNames = mAssets.list(SOUNDS_FOLDER);
            Log.i(TAG, "Found " + soundNames.length + " sounds");
        } catch (IOException ioe) {
            Log.e(TAG, "Could not list assets", ioe);
            return;
        }
        for (String filename : soundNames) {
            try {
                String assetPath = SOUNDS_FOLDER + "/" + filename;
                Click click = new Click(assetPath);
                load(click);
                mClicks.add(click);
                Log.i(TAG, "  Loaded: " + filename);
            } catch (IOException ioe) {
                Log.e(TAG, "Could not load sound " + filename, ioe);
            }
        }
    }

    private void load(Click click) throws IOException {
        AssetFileDescriptor afd = mAssets.openFd(click.getAssetPath());
        int soundId = mSoundPool.load(afd, 1);
        click.setSoundId(soundId);
    }

    public List<Click> getClicks() {
        return mClicks;
    }
}
