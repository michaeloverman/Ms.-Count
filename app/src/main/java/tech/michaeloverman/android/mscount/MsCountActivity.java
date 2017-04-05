package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import timber.log.Timber;

public class MsCountActivity extends tech.michaeloverman.android.mscount.SingleFragmentActivity {

    public static int sClickSound;


    @Override
    protected Fragment createFragment() {
        Timber.d("MsCountActivity createFragment()");
        return MetronomeSelectorFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("MsCountActivity onCreate()");

        sClickSound = 4;

    }


}
