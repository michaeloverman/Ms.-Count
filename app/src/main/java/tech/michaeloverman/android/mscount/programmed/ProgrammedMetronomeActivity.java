package tech.michaeloverman.android.mscount.programmed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;

import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeActivity;
import timber.log.Timber;

/**
 * Created by Michael on 3/24/2017.
 */

public class ProgrammedMetronomeActivity extends MetronomeActivity {

    private static final int FIREBASE_SIGN_IN = 456;

    @Override
    protected Fragment createFragment() {
        mMetronome = Metronome.getInstance();
        return ProgrammedMetronomeFragment.newInstance(mMetronome, this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        mLocalDatabase = new ProgramDatabaseHelper(this).getWritableDatabase();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.d("onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.programmed_global_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("ACTIVITY: onOptionsItemSelected()");
        switch (item.getItemId()) {
//            case R.id.firebase_local_database:
//                useFirebase = !useFirebase;
//                if(useFirebase) {
//                    item.setTitle("Use local database");
//                    if(mAuth.getCurrentUser() == null) {
//                        signInToFirebase();
//                    }
//                } else {
//                    item.setTitle("Use cloud database");
//                }
//                updateData();
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("ACTIVITY: onActivityResult()");

        if(requestCode == FIREBASE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
//                startActivity(SignedInActivity.createIntent(this, response));
//                finish();
                Timber.d("signed into Firebase");
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }
            }

            showSnackbar(R.string.unknown_sign_in_response);
        }
    }

    private void showSnackbar(int message) {
        String m = getString(message);
        Timber.d(m);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mMetronome.isRunning()) {
            mMetronome.stop();
        }
    }


}
