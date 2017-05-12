package tech.michaeloverman.android.mscount.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tech.michaeloverman.android.mscount.pojos.Click;
import timber.log.Timber;

/**
 * Created by Michael on 5/12/2017.
 */

public class ClickSounds {

    private static final String SOUNDS_FOLDER = "sample_sounds";

    private static AssetManager mAssets;
    private static List<Click> mClicks = new ArrayList<>();
    private static SoundPool mSoundPool;

    public static void loadSounds(Context context) {
        if(mSoundPool != null) return;
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        mAssets = context.getAssets();
        new LoadSoundsTask().execute();
    }

    public static List<Click> getClicks() {
        return mClicks;
    }

    public static SoundPool getSoundPool(Context context) {
        if(mSoundPool == null) {
            loadSounds(context);
        }
        return mSoundPool;
    }

    private static class LoadSoundsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String[] soundNames;
            try {
                soundNames = mAssets.list(SOUNDS_FOLDER);
                Timber.d("Found " + soundNames.length + " sounds");
            } catch (IOException ioe) {
                Timber.d("Could not list assets", ioe);
                return null;
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
                    return null;
                }
            }
            return null;
        }

        private void load(Click click) throws IOException {
            AssetFileDescriptor afd = mAssets.openFd(click.getAssetPath());
            int soundId = mSoundPool.load(afd, 1);
            click.setSoundId(soundId);
        }
    }

}
