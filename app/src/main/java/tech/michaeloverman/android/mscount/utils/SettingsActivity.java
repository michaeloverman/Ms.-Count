package tech.michaeloverman.android.mscount.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import tech.michaeloverman.android.mscount.R;

/**
 * Created by Michael on 4/4/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    protected String[] mEntries;
    protected String[] mValues;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mEntries = intent.getStringArrayExtra(MetronomeActivity.EXTRA_ENTRIES);
        mValues = intent.getStringArrayExtra(MetronomeActivity.EXTRA_VALUES);

        setContentView(R.layout.settings_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
