package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by Michael on 3/24/2017.
 */

public class OddMeterMetronomeActivity extends SingleFragmentActivity {

    private static final String TAG = OddMeterMetronomeActivity.class.getSimpleName();

    @Override
    protected Fragment createFragment() {
        return OddMeterMetronomeFragment.newInstance();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
