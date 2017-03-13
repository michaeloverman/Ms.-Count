package tech.michaeloverman.android.mscount.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
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
    public static final int MAX_SUBDIVISIONS = 10;
    public static final int MAX_TEMPO = 400;
    public static final int MIN_TEMPO = 15;

    /* Sounds and Such */
    private AssetManager mAssets;
    private List<Click> mClicks = new ArrayList<>();
    private SoundPool mSoundPool;
    private Integer mClickId;
    private Integer mHiClickId, mLoClickId;
    private float[] mClickVolumes;

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
        SoundPool.Builder builder = new SoundPool.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                        .build())
                .setMaxStreams(5);
//        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        mSoundPool = builder.build();
        loadSounds();
        mClickVolumes = new float[MAX_SUBDIVISIONS];
        for(int i = 0; i < MAX_SUBDIVISIONS; i++) {
            mClickVolumes[i] = 1.0f;
        }

    }
    public Metronome(Context context) {
        this(context, null);
    }

    /**
     * Simple metronome click: takes either int or float tempo marking, and number of
     * beats per measure, calculates delay between clicks in millis,
     * starts simple timer, and clicks at defined intervals.
     */
    public void play(int tempo, int beats) {
        if(beats == 1) {
            mDelay = 60000 / tempo;
            startClicking();
        } else {
            mDelay = 60000 / tempo;
            playSubdivisions(beats);
        }
    }
    public void play(float tempo, int beats) {
        if(beats == 1) {
            float delay = 60000f / tempo;
            mDelay = (int) delay;
            startClicking();
        } else {
            float delay = 60000f / tempo;
            mDelay = (int) delay;
            playSubdivisions(beats);
        }
    }

    /**
     * Simple, single click metronome start
     */
    private void startClicking() {
        Log.d(TAG, "int delay: "+ mDelay);
        mClickId = mClicks.get(0).getSoundId();
//        mHiClickId = mClicks.get(1).getSoundId();
//        mLoClickId = mClicks.get(2).getSoundId();
        if (mClickId == null) {
            return;
        }

        mTimer = new CountDownTimer(TWENTY_MINUTES, mDelay) {
            @Override
            public void onTick(long millisUntilFinished) {
                mSoundPool.play(mClickId, mClickVolumes[0], mClickVolumes[0], 1, 0, 1.0f);
            }

            @Override
            public void onFinish() {

            }
        };
        mTimer.start();
    }

    /**
     * Click with number of subdivisions metronome start
     * @param subs
     */
    private void playSubdivisions(final int subs) {
        mHiClickId = mClicks.get(1).getSoundId();
        mLoClickId = mClicks.get(2).getSoundId();

        logSubdivisionVolumes(subs);

        mTimer = new CountDownTimer(TWENTY_MINUTES, mDelay) {
            int subCount = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                if(subCount == 0) {
                    mSoundPool.play(mHiClickId, mClickVolumes[subCount], mClickVolumes[subCount], 1, 0, 1.0f);
                } else {
                    mSoundPool.play(mLoClickId, mClickVolumes[subCount], mClickVolumes[subCount], 1, 0, 1.0f);
                }
                if(++subCount == subs) subCount = 0;
            }

            @Override
            public void onFinish() {

            }
        };
        mTimer.start();
    }

    private void logSubdivisionVolumes(int subs) {
        StringBuffer sb = new StringBuffer("Subdivision Volumes: ");
        for(int i = 0; i < subs; i++) {
            sb.append(mClickVolumes[i] + ", ");
        }
        Log.d(TAG, sb.toString());
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
        mDelay = 60000 / p.getSubdivision() / tempo;
        Log.d(TAG, p.toString());
        final int[] beats = Utilities.integerListToArray(p.getBeats());
        final int[] downBeats = Utilities.integerListToArray(p.getDownBeats());
        final int countOffSubs = p.getCountOffSubdivision();
        final int measureNumberOffset = p.getMeasureCountOffset();

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
                        mListener.metronomeMeasureNumber( (measureNumberOffset + measurePointer++) + "");
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

                    if(measurePointer == 1) {
//                        Log.d(TAG, "Countoff Measure, beat" + beatPointer);
                        if(beatPointer >= 3 + countOffSubs) {
                            mListener.metronomeMeasureNumber("GO");
                        } else if (beatPointer >= 3) {
                            mListener.metronomeMeasureNumber("READY");
                        } else {
                            mListener.metronomeMeasureNumber((beatPointer) + "");
                        }
                    }
                }
                count++; // count one subdivision gone by...
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

    public void setClickVolumes(int[] vols) {
        int num = vols.length > MAX_SUBDIVISIONS ? MAX_SUBDIVISIONS : vols.length;
        for(int i = 0; i < num; i++) {
            mClickVolumes[i] = vols[i] / 10.0f;
        }
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
