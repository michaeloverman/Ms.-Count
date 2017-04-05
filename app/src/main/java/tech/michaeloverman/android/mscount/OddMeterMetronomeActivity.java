package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeActivity;

/**
 * Created by Michael on 3/24/2017.
 */

public class OddMeterMetronomeActivity extends MetronomeActivity {

    @Override
    protected Fragment createFragment() {
        mMetronome = new Metronome(this);
        return OddMeterMetronomeFragment.newInstance(mMetronome);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mMetronome.isRunning()) {
            // stop metronome
            mMetronome.stop();
        }
    }
}
