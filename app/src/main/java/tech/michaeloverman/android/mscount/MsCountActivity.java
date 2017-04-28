package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.ads.MobileAds;

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

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-9915736656105375~9633528243");

        mMetronome = Metronome.getInstance();
        mMetronome.setContext(this);
    }


}
