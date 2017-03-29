package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class MsCountActivity extends tech.michaeloverman.android.mscount.SingleFragmentActivity {

    private static final String TAG = MsCountActivity.class.getSimpleName();
    public static int sClickSound;


    @Override
    protected Fragment createFragment() {
        Log.d(TAG, "MsCountActivity createFragment()");
        return MetronomeSelectorFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MsCountActivity onCreate()");

        sClickSound = 4;

    }


}
