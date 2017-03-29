package tech.michaeloverman.android.mscount.programmed;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tech.michaeloverman.android.mscount.BuildConfig;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.SingleFragmentActivity;
import tech.michaeloverman.android.mscount.database.ProgramDatabaseHelper;
import tech.michaeloverman.android.mscount.utils.Metronome;

/**
 * Created by Michael on 3/24/2017.
 */

public class ProgrammedMetronomeActivity extends SingleFragmentActivity {
    private static final String TAG = ProgrammedMetronomeActivity.class.getSimpleName();
    private static final int FIREBASE_SIGN_IN = 456;

    private FirebaseAuth mAuth;
    private boolean userIsAdmin;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private SQLiteDatabase mLocalDatabase;

    public boolean useFirebase;

    protected Metronome mMetronome;

    @Override
    protected Fragment createFragment() {
        mMetronome = new Metronome(this);
        return ProgrammedMetronomeFragment.newInstance(mMetronome);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        useFirebase = true;

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    checkAdmin();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    useFirebase = false;
                }
                // ...
            }
        };

        if(mAuth.getCurrentUser() == null) {
            signInToFirebase();
        }

        mLocalDatabase = new ProgramDatabaseHelper(this).getWritableDatabase();

    }

    private void signInToFirebase() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .build(),
                FIREBASE_SIGN_IN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.programmed_global_menu, menu);
        MenuItem item = menu.findItem(R.id.firebase_local_database);
        item.setTitle(useFirebase ? R.string.use_local_database : R.string.use_cloud_database);
        return true;
    }



    private void checkAdmin() {
        FirebaseDatabase.getInstance().getReference().child("admin")
                .child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot == null) {
                            Log.d(TAG, "user is NOT admin");
                            userIsAdmin = false;
                        } else {
                            Log.d(TAG, "user IS admin");
                            userIsAdmin = true;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public boolean isUserAdmin() {
        return userIsAdmin;
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
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
        switch (item.getItemId()) {
            case R.id.firebase_local_database:
                useFirebase = !useFirebase;
                if(useFirebase) {
                    item.setTitle("Use local database");
                    if(mAuth.getCurrentUser() == null) {
                        signInToFirebase();
                    }
                } else {
                    item.setTitle("Use cloud database");
                }
                updateData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateData() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(f instanceof ProgramSelectFragment) {
            ((ProgramSelectFragment) f).updateData();
        } else if (f instanceof SelectComposerFragment) {
            ((SelectComposerFragment) f).updateData();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FIREBASE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
//                startActivity(SignedInActivity.createIntent(this, response));
//                finish();
                Log.d(TAG, "signed into Firebase");
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
        Log.d(TAG, m);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mMetronome.isRunning()) {
            mMetronome.stop();
        }
    }
}
