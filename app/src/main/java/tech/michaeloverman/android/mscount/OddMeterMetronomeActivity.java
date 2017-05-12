/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.TransitionInflater;

import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeActivity;

/**
 * Created by Michael on 3/24/2017.
 */

public class OddMeterMetronomeActivity extends MetronomeActivity {

    @Override
    protected Fragment createFragment() {
        mMetronome = new Metronome(this);
        return OddMeterMetronomeFragment.newInstance();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21) {
            setupWindowAnimations();
        }
    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        if(mMetronome.isRunning()) {
//            // stop metronome
//            mMetronome.stop();
//        }
//    }

    @TargetApi(21)
    private void setupWindowAnimations() {
        Fade slide = (Fade) TransitionInflater.from(this).inflateTransition(R.transition.activity_fade_enter);
        getWindow().setEnterTransition(slide);
        getWindow().setAllowEnterTransitionOverlap(true);
    }
}
