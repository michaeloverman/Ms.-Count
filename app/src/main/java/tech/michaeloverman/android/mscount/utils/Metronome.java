package tech.michaeloverman.android.mscount.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tech.michaeloverman.android.mscount.pojos.Click;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import timber.log.Timber;

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
//    private Click mDownBeatClick, mOtherBeatClick;
    private int mDownBeatClickId, mInnerBeatClickId;
    private float[] mClickVolumes;
    private Context mContext;

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
    public Metronome(Context context) {
        mContext = context;
        mAssets = mContext.getAssets();
//        setMetronomeListener(ml);
        mClicking = false;
//        SoundPool.Builder builder = new SoundPool.Builder()
//                .setAudioAttributes(new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_GAME)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                        .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
//                        .build())
//                .setMaxStreams(5);
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
//        mSoundPool = builder.build();
        loadSounds();
        mClickVolumes = new float[MAX_SUBDIVISIONS];
        for(int i = 0; i < MAX_SUBDIVISIONS; i++) {
            mClickVolumes[i] = 1.0f;
        }





    }

    public void setMetronomeListener(MetronomeListener ml) {
        mListener = ml;
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
            playSubdivisions(beats);
        }
    }
    public void play(float tempo, int beats) {
        getClicksFromSharedPrefs();
        float delay = 60000f / tempo;
        mDelay = (int) delay;
        if(beats == 1) {
            startClicking();
        } else {
            playSubdivisions(beats);
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
            }
        };
        mClicking = true;
        mTimer.start();
    }

    /**
     * Click with number of subdivisions metronome start
     * @param subs
     */
    private void playSubdivisions(final int subs) {
//        mDownBeatClickId = mClicks.get(1).getSoundId();
//        mInnerBeatClickId = mClicks.get(2).getSoundId();

        logSubdivisionVolumes(subs);

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

    private void logSubdivisionVolumes(int subs) {
        StringBuffer sb = new StringBuffer("Subdivision Volumes: ");
        for(int i = 0; i < subs; i++) {
            sb.append(mClickVolumes[i] + ", ");
        }
        Timber.d(sb.toString());
    }

    private void logClickIds() {
        Timber.d("mDownbeatClickId = " + mDownBeatClickId);
        Timber.d("mInnerBeatClickId = " + mInnerBeatClickId);
    }
    /**
     * Programmed click, accepts a PieceOfMusic to define changing click patterns, and a
     * tempo marking.
     * @param p
     * @param tempo
     */
    public void play(PieceOfMusic p, int tempo) {
        logClickIds();
        Timber.d("metronome play()");
        if(p.getTempoMultiplier() != 0) {
            Timber.d("tempo multiplier!! " + p.getTempoMultiplier());
            tempo *= p.getTempoMultiplier();
        }
        mDelay = 60000 / p.getSubdivision() / tempo;
        Timber.d(p.toString());
        final int[] beats = Utilities.integerListToArray(p.getBeats());
        final int[] downBeats = Utilities.integerListToArray(p.getDownBeats());
        final int countOffSubs = p.getCountOffSubdivision();
        final int measureNumberOffset = p.getMeasureCountOffset();

        getClicksFromSharedPrefs();

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
                        mListener.metronomeMeasureNumber( (measureNumberOffset + measurePointer++) + "");
                    } else { // inner beat
                        mSoundPool.play(mInnerBeatClickId, 1.0f, 1.0f, 1, 0, 1.0f);

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
                mClicking = false;
//                mListener.metronomeStartStop();
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
    public void play(int tempo, List<Integer> groupings) {

        Timber.d("play an odd-meter loop");

        final int[] beats = Utilities.integerListToArray(groupings);

        Timber.d("beat loop length: " + beats.length);

        mDelay = 60000 / tempo;

        getClicksFromSharedPrefs();

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
    private void loadSounds() {
        String[] soundNames;
        try {
            soundNames = mAssets.list(SOUNDS_FOLDER);
            Timber.d("Found " + soundNames.length + " sounds");
        } catch (IOException ioe) {
            Timber.d("Could not list assets", ioe);
            return;
        }
        for (String filename : soundNames) {
            try {
                String assetPath = SOUNDS_FOLDER + "/" + filename;
                Click click = new Click(assetPath);
                load(click);
                mClicks.add(click);
                Timber.d("  Loaded: " + filename);
            } catch (IOException ioe) {
                Timber.d("Could not load sound " + filename, ioe);
                return;
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

    private void getClicksFromSharedPrefs() {
        mDownBeatClickId = PrefUtils.getDownBeatClickId(mContext);
        mInnerBeatClickId = PrefUtils.getInnerBeatClickId(mContext);
    }
}
