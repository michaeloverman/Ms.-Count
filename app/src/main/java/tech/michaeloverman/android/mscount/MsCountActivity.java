package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import tech.michaeloverman.android.mscount.utils.Metronome;
import timber.log.Timber;

public class MsCountActivity extends tech.michaeloverman.android.mscount.SingleFragmentActivity {

    public Metronome mMetronome;

    @Override
    protected Fragment createFragment() {
        Timber.d("MsCountActivity createFragment()");
        return MetronomeSelectorFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("MsCountActivity onCreate()");

        mMetronome = Metronome.getInstance();
        mMetronome.setContext(this);
    }


}
