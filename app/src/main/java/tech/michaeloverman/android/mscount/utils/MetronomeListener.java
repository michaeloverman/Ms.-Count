package tech.michaeloverman.android.mscount.utils;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by Michael on 2/28/2017.
 */

public interface MetronomeListener {
    void metronomeStartStop();
    void metronomeMeasureNumber(String mm);
    void registerReceiver(BroadcastReceiver br, IntentFilter filter);
    void unregisterReceiver(BroadcastReceiver br);
}
