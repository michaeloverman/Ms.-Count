package tech.michaeloverman.android.stickit;

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

/**
 * Created by Michael on 10/6/2016.
 */

public class Metronome {
    private static final String TAG = "Metronome";
    private static final String SOUNDS_FOLDER = "sample_sounds";
    private static final long TWENTY_MINUTES = 60000 * 20;

    private AssetManager mAssets;
    private List<Click> mClicks = new ArrayList<>();
    private SoundPool mSoundPool;
    private Integer mClickId;
    private Integer mHiClickId, mLoClickId;
    private long mDelay;

    private boolean mClicking;

    public Metronome(Context context) {
        mAssets = context.getAssets();
        mClicking = false;
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        loadSounds();
    }

//    Runnable mClicker = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                while(mClicking) {
//                    mSoundPool.play(mClickId, 1.0f, 1.0f, 1, 0, 1.0f);
//                    wait(mDelay);
//                }
//            } catch (InterruptedException ie) {
//            }
//        }
//
//    };

    CountDownTimer mTimer;

    public void play(int tempo) {
        mDelay = 60000 / tempo / 4;
        final int[] loop = { 4, 4, 6, 4, 4, 4, 6, 4, 2, 2, 4, 4, 5, 6, 7, 8 };
        mClickId = mClicks.get(0).getSoundId();
//        mHiClickId = mClicks.get(1).getSoundId();
//        mLoClickId = mClicks.get(2).getSoundId();
        if (mClickId == null) {
            return;
        }

        mTimer = new CountDownTimer(TWENTY_MINUTES, mDelay) {
            int count = 0;
            int goal = 0;
            int loopPointer = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                if (count == goal) {
                    mSoundPool.play(mClickId, 1.0f, 1.0f, 1, 0, 1.0f);
                    goal += loop[loopPointer++];
                    if(loopPointer >= loop.length) {
                        loopPointer = 0;
                    }
                }
                count++;

            }

            @Override
            public void onFinish() {

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
