package tech.michaeloverman.android.mscount;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.SettingsActivity;

/**
 * Created by Michael on 3/24/2017.
 */

public class NormalMetronomeActivity extends SingleFragmentActivity {

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.normal_menu, menu);
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

    private void gotoSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mMetronome.isRunning()) {
            mMetronome.stop();
        }
    }
}
