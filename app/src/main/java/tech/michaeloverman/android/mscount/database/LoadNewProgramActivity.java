/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.database;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import tech.michaeloverman.android.mscount.SingleFragmentActivity;
import tech.michaeloverman.android.mscount.programmed.ProgrammedMetronomeFragment;
import tech.michaeloverman.android.mscount.utils.PrefUtils;
import timber.log.Timber;

/**
 * Created by Michael on 3/31/2017.
 */

public class LoadNewProgramActivity extends SingleFragmentActivity {

    public static final String EXTRA_NEW_PROGRAM = "new_program_extra";
    private static final int FIREBASE_SIGN_IN = 451;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    boolean useFirebase;
    String mCurrentComposer;


    @Override
    protected Fragment createFragment() {
        return PieceSelectFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        supportPostponeEnterTransition();

        Intent intent = getIntent();
        if(intent.hasExtra(ProgrammedMetronomeFragment.EXTRA_COMPOSER_NAME)) {
            mCurrentComposer = intent.getStringExtra(ProgrammedMetronomeFragment.EXTRA_COMPOSER_NAME);
        } else {
            mCurrentComposer = null;
        }
        if(intent.hasExtra(ProgrammedMetronomeFragment.EXTRA_USE_FIREBASE)) {
            useFirebase = intent.getBooleanExtra(ProgrammedMetronomeFragment.EXTRA_USE_FIREBASE, true);
            Timber.d("useFirebase received from intent: " + useFirebase);
        }

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

        if(mAuth.getCurrentUser() == null) {
            signInToFirebase();
        }
    }

    void setProgramResult(String pieceId) {
        if(pieceId == null) {
            Timber.d("null piece recieved to return");
        } else {
            Timber.d("setting new program on result intent: " + pieceId);
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_NEW_PROGRAM, pieceId);
        setResult(RESULT_OK, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart useFirebase: " + useFirebase);
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.d("onStop useFirebase: " + useFirebase);
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.d("onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.programmed_global_menu, menu);
        MenuItem item = menu.findItem(R.id.firebase_local_database);
        item.setTitle(useFirebase ? R.string.use_local_database : R.string.use_cloud_database);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("Activity menu option");
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
                updateData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateData() {
        DatabaseAccessFragment f = (DatabaseAccessFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (!useFirebase && f instanceof ComposerSelectFragment) {
//            Timber.d("popping......");
            f.getFragmentManager().popBackStackImmediate();
        } else {
//            Timber.d("switching to cloud");
            f.updateData();
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
                    Toast.makeText(this, R.string.sign_in_cancelled, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(this, R.string.unknown_sign_in_response, Toast.LENGTH_SHORT).show();
        }
    }
//TODO this method is needed when by the save data method someplace else
    // MAYBE NOT anymore - using userId to validate specific program access
//    private void checkAdmin() {
//        FirebaseDatabase.getInstance().getReference().child("admin")
//                .child(mAuth.getCurrentUser().getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if(dataSnapshot == null) {
//                            Timber.d("user is NOT admin");
//                            userIsAdmin = false;
//                        } else {
//                            Timber.d("user IS admin");
//                            userIsAdmin = true;
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }
//    public boolean isUserAdmin() {
//        return userIsAdmin;
//    }

}
