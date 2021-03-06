/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.programmed;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.transition.Fade;
import android.transition.TransitionInflater;
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
import tech.michaeloverman.android.mscount.dataentry.MetaDataEntryFragment;
import tech.michaeloverman.android.mscount.utils.MetronomeActivity;
import tech.michaeloverman.android.mscount.utils.PrefUtils;
import timber.log.Timber;

/**
 * This activity manages the various frgaments involved in the programmed metronome. Particularly
 * the local vs cloud database options menu item, and Firebase signin.
 *
 * Created by Michael on 3/24/2017.
 */

public class ProgrammedMetronomeActivity extends MetronomeActivity {

    private static final int FIREBASE_SIGN_IN = 456;
    private static final String KEY_USE_FIREBASE = "use_firebase_key";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public boolean useFirebase;
    private MenuItem databaseMenuItem;
    public static final String PROGRAM_ID_EXTRA = "program_id_extra_from_widget";

    @Override
    protected Fragment createFragment() {
        return ProgrammedMetronomeFragment.newInstance();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            setupWindowAnimations();
        }

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
                    goLocal();
                }
                // ...
            }
        };

        Intent intent = getIntent();
        if (intent.hasExtra(PROGRAM_ID_EXTRA)) {
            Timber.d("PROGRAM ID FROM WIDGET DETECTED: GO, GO, GO!!!");
            int id = intent.getIntExtra(PROGRAM_ID_EXTRA, 999);
            PrefUtils.saveWidgetSelectedPieceToPrefs(this, id);
        }


        if (mAuth.getCurrentUser() == null) {
            signInToFirebase();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.d("onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.programmed_global_menu, menu);
        databaseMenuItem = menu.findItem(R.id.firebase_local_database);
        updateDatabaseOptionMenuItem();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart()");
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume() - firebase: " + useFirebase);
        useFirebase = PrefUtils.usingFirebase(this);
        if(databaseMenuItem != null) {
            updateDatabaseOptionMenuItem();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("ACTIVITY: onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.firebase_local_database:
                useFirebase = !useFirebase;
                if (useFirebase) {
                    if (mAuth.getCurrentUser() == null) {
                        signInToFirebase();
                    }
                }
                updateDatabaseOptionMenuItem();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_USE_FIREBASE, useFirebase);
    }

    @Override
    public void onBackPressed() {
        Fragment f = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof MetaDataEntryFragment) {
            losingDataAlertDialog();
        } else {
            actuallyGoBack();
        }

    }

    private void actuallyGoBack() {

        super.onBackPressed();

    }

    @TargetApi(21)
    private void setupWindowAnimations() {
        Fade slide = (Fade) TransitionInflater.from(this).inflateTransition(R.transition.activity_fade_enter);
        getWindow().setEnterTransition(slide);
        getWindow().setAllowEnterTransitionOverlap(true);
    }

    private void signInToFirebase() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .setTheme(R.style.AppTheme_FirebaseSignIn)
                        .build(),
                FIREBASE_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("ACTIVITY: onActivityResult()");

        if (requestCode == FIREBASE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
//                startActivity(SignedInActivity.createIntent(this, response));
//                finish();
                Timber.d("signed into Firebase");
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showToast(R.string.sign_in_cancelled);
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showToast(R.string.no_internet_connection);
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showToast(R.string.unknown_error);
                } else {
                    showToast(R.string.unknown_sign_in_response);
                }
                goLocal();
            }

        }
    }

    private void updateDatabaseOptionMenuItem() {
        Timber.d("updateDatabaseOptionMenuItem");
        PrefUtils.saveFirebaseStatus(this, useFirebase);
        if(databaseMenuItem != null) {
            databaseMenuItem.setTitle(useFirebase ?
                    R.string.use_local_database : R.string.use_cloud_database);
        }
    }

    private void goLocal() {
        useFirebase = false;
        updateDatabaseOptionMenuItem();
    }

    private void showToast(int message) {
        String m = getString(message);
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
        Timber.d(m);
    }

    private void losingDataAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.erase_data)
                .setMessage(R.string.leave_without_save)
                .setPositiveButton(R.string.leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        actuallyGoBack();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}
