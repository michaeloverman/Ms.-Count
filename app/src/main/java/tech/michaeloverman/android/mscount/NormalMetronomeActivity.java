package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import tech.michaeloverman.android.mscount.utils.Metronome;

/**
 * Created by Michael on 3/24/2017.
 */

public class NormalMetronomeActivity extends SingleFragmentActivity {
    private static final String TAG = NormalMetronomeActivity.class.getSimpleName();

    protected Metronome mMetronome;

    @Override
    protected Fragment createFragment() {
        mMetronome = new Metronome(this);
        return NormalMetronomeFragment.newInstance(mMetronome);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mMetronome.isRunning()) {
            mMetronome.stop();
        }
    }
}
