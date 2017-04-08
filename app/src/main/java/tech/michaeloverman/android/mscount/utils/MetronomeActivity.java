package tech.michaeloverman.android.mscount.utils;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.SingleFragmentActivity;
import tech.michaeloverman.android.mscount.pojos.Click;

/**
 * Created by Michael on 4/5/2017.
 */

public abstract class MetronomeActivity extends SingleFragmentActivity {

    public static final String EXTRA_ENTRIES = "entries_for_settings";
    public static final String EXTRA_VALUES = "values_for_settings";
    protected Metronome mMetronome;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.global_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                gotoSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Attaches click file names to Settings intent for setting menu
     */
    private void gotoSettings() {
        List<Click> list = mMetronome.getClicks();
        int size = list.size();
        String[] entries = new String[size];
        String[] values = new String[size];
        for(int i = 0; i < size; i++) {
            entries[i] = list.get(i).getName();
            values[i] = Integer.toString(list.get(i).getSoundId());
        }
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(EXTRA_ENTRIES, entries);
        intent.putExtra(EXTRA_VALUES, values);
        startActivity(intent);
    }

//    public Metronome getMetronome() {
//        return mMetronome;
//    }
}
