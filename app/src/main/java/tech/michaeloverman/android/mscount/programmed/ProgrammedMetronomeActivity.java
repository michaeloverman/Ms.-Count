package tech.michaeloverman.android.mscount.programmed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import tech.michaeloverman.android.mscount.BuildConfig;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeActivity;
import tech.michaeloverman.android.mscount.utils.PrefUtils;
import timber.log.Timber;

/**
 * Created by Michael on 3/24/2017.
 */

public class ProgrammedMetronomeActivity extends MetronomeActivity {

    private static final int FIREBASE_SIGN_IN = 456;
    private static final String KEY_USE_FIREBASE = "use_firebase_key";
    protected FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public boolean useFirebase;
    public static final String PROGRAM_ID_EXTRA = "program_id_extra_from_widget";

    @Override
    protected Fragment createFragment() {
        mMetronome = Metronome.getInstance();
        return ProgrammedMetronomeFragment.newInstance(mMetronome, this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        useFirebase = PrefUtils.usingFirebase(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Timber.d("onAuthStateChanged:signed_in:" + user.getUid());
//                    useFirebase = true;
                } else {
                    // User is signed out
                    Timber.d("onAuthStateChanged:signed_out");
                    useFirebase = false;
                }
                // ...
            }
        };

        Intent intent = getIntent();
        if(intent.hasExtra(PROGRAM_ID_EXTRA)) {
            Timber.d("PROGRAM ID FROM WIDGET DETECTED: GO, GO, GO!!!");
            int id = intent.getIntExtra(PROGRAM_ID_EXTRA, 999);
            PrefUtils.saveWidgetSelectedPieceToPrefs(this, id);
        }


        if(mAuth.getCurrentUser() == null) {
            signInToFirebase();
        }

//        mLocalDatabase = new ProgramDatabaseHelper(this).getWritableDatabase();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_USE_FIREBASE, useFirebase);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.d("onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.programmed_global_menu, menu);
        MenuItem item = menu.findItem(R.id.firebase_local_database);
        item.setTitle(useFirebase ? R.string.use_local_database : R.string.use_cloud_database);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("ACTIVITY: onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.firebase_local_database:
                useFirebase = !useFirebase;
                PrefUtils.saveFirebaseStatus(this, useFirebase);
                if(useFirebase) {
                    item.setTitle(R.string.use_local_database);
                    if(mAuth.getCurrentUser() == null) {
                        signInToFirebase();
                    }
                } else {
                    item.setTitle(R.string.use_cloud_database);
                }
//                updateData();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signInToFirebase() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .setTheme(R.style.AppTheme)
                        .build(),
                FIREBASE_SIGN_IN);
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
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
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
