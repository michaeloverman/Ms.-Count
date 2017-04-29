package tech.michaeloverman.android.mscount.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

/**
 * Created by Michael on 4/29/2017.
 */

public class MetronomeBroadcastReceiver extends BroadcastReceiver {

    private MetronomeListener mListener;

    public MetronomeBroadcastReceiver(MetronomeListener ml) {
        Timber.d("BroadcastReceiver created");
        mListener = ml;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive()  woot woot");
        if(intent.getAction().equals(Metronome.ACTION_METRONOME_START_STOP)) {
            Timber.d(Metronome.ACTION_METRONOME_START_STOP);
            mListener.metronomeStartStop();
        } else if (intent.getAction().equals("Action Reply")) {
            Timber.d("Action Reply received");
        }
    }
}
