package tech.michaeloverman.android.mscount.programmed;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.database.LoadNewProgramActivity;
import tech.michaeloverman.android.mscount.database.ProgramDatabaseSchema;
import tech.michaeloverman.android.mscount.dataentry.MetaDataEntryFragment;
import tech.michaeloverman.android.mscount.favorites.FavoritesContract;
import tech.michaeloverman.android.mscount.favorites.FavoritesDBHelper;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeListener;
import tech.michaeloverman.android.mscount.utils.PrefUtils;
import tech.michaeloverman.android.mscount.utils.Utilities;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Michael on 2/24/2017.
 */

public class ProgrammedMetronomeFragment extends Fragment
        implements MetronomeListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final boolean UP = true;
    private static final boolean DOWN = false;
    private static final int MAXIMUM_TEMPO = 350;
    private static final int MINIMUM_TEMPO = 1;
    private static final String CURRENT_PIECE_TITLE_KEY = "current_piece_key";
    private static final String CURRENT_TEMPO_KEY = "current_tempo_key";
    private static final String CURRENT_COMPOSER_KEY = "current_composer_key";
    private static final int ID_PIECE_LOADER = 434;

    private static final int REQUEST_NEW_PROGRAM = 44;
    public static final String EXTRA_COMPOSER_NAME = "composer_name_extra";
    public static final String EXTRA_USE_FIREBASE = "program_database_option";

    private PieceOfMusic mCurrentPiece;
    private String mCurrentPieceKey;
    private int mCurrentTempo;
    private String mCurrentComposer;
    private boolean mIsCurrentFavorite;
    private Metronome mMetronome;
    private boolean mMetronomeRunning;

    @BindView(R.id.current_composer_name) TextView mTVCurrentComposer;
    @BindView(R.id.current_program_title) TextView mTVCurrentPiece;
    @BindView(R.id.current_tempo_setting) TextView mTVCurrentTempo;
    @BindView(R.id.primary_beat_length_image) ImageView mBeatLengthImage;
    @BindView(R.id.start_stop_fab) FloatingActionButton mStartStopButton;
    @BindView(R.id.tempo_up_button) ImageButton mTempoUpButton;
    @BindView(R.id.tempo_down_button) ImageButton mTempoDownButton;
    @BindView(R.id.current_measure_number) TextView mCurrentMeasureNumber;

    private Handler mRunnableHandler;
    private Runnable mDownRunnable;
    private Runnable mUpRunnable;
    private static final int INITIAL_TEMPO_CHANGE_DELAY = 400;
    private static final int FIRST_FASTER_SPEED_DELAY = 80;
    private static final int RATE_OF_DELAY_CHANGE = 2;
    private static int mTempoChangeDelay;
    private static final int ONE_LESS = INITIAL_TEMPO_CHANGE_DELAY - 2;
    private static final int MIN_TEMPO_CHANGE_DELAY = 20;

    private static ProgrammedMetronomeActivity mActivity;
    private static Cursor mCursor;

    public static Fragment newInstance(Metronome m, ProgrammedMetronomeActivity a) {
        ProgrammedMetronomeFragment fragment = new ProgrammedMetronomeFragment();
        mActivity = a;
        fragment.setMetronome(m);
        return fragment;
    }

    private void setMetronome(Metronome m) {
        mMetronome = m;
        mMetronome.setMetronomeListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        getActivity().setTitle(getString(R.string.app_name));

        if(savedInstanceState != null) {
            Timber.d("found savedInstanceState");
//            mCurrentTempo = savedInstanceState.getInt(CURRENT_TEMPO_KEY);
            mCurrentPieceKey = savedInstanceState.getString(CURRENT_PIECE_TITLE_KEY);
//            mCurrentComposer = savedInstanceState.getString(CURRENT_COMPOSER_KEY);
            Timber.d("savedInstanceState retrieved: composer: " + mCurrentComposer);
            getPieceFromKey();
        } else {
            Timber.d("savedInstanceState not found - looking to SharedPrefs");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            mCurrentPieceKey = PrefUtils.getSavedPieceKey(mActivity);

            if(mCurrentPieceKey != null) {
                checkKeyFormat();
                getPieceFromKey();
            }
            mCurrentTempo = PrefUtils.getSavedTempo(mActivity);
        }

        mMetronomeRunning = false;

        mRunnableHandler = new Handler();
        mTempoChangeDelay = INITIAL_TEMPO_CHANGE_DELAY;
        mDownRunnable = new Runnable() {
            @Override
            public void run() {
                if(mTempoChangeDelay == ONE_LESS) mTempoChangeDelay = FIRST_FASTER_SPEED_DELAY;
                else if (mTempoChangeDelay < MIN_TEMPO_CHANGE_DELAY) mTempoChangeDelay = MIN_TEMPO_CHANGE_DELAY;
                changeTempo(DOWN);
                mRunnableHandler.postDelayed(this, mTempoChangeDelay--);
            }
        };
        mUpRunnable = new Runnable() {
            @Override
            public void run() {
                if(mTempoChangeDelay == ONE_LESS) mTempoChangeDelay = FIRST_FASTER_SPEED_DELAY;
                else if (mTempoChangeDelay < MIN_TEMPO_CHANGE_DELAY) mTempoChangeDelay = MIN_TEMPO_CHANGE_DELAY;
                changeTempo(UP);
                mRunnableHandler.postDelayed(this, mTempoChangeDelay -= RATE_OF_DELAY_CHANGE);
            }
        };
    }

    private void checkKeyFormat() {
        Timber.d("Firebase: " + mActivity.useFirebase + " :: key: " + mCurrentPieceKey.charAt(0));
        if(mActivity.useFirebase) {
            try {
                Integer.parseInt(mCurrentPieceKey);
            } catch (NumberFormatException nfe) {
                mActivity.useFirebase = false;
                PrefUtils.saveFirebaseStatus(mActivity, mActivity.useFirebase);
            }
        } else {
            if(mCurrentPieceKey.charAt(0) == '-') {
                mActivity.useFirebase = true;
                PrefUtils.saveFirebaseStatus(mActivity, mActivity.useFirebase);
            }
        }
    }

    private void getPieceFromKey() {
        Timber.d("getPieceFromKey() " + mCurrentPieceKey);
        Timber.d("activity's useFirebase: " + mActivity.useFirebase);
//        boolean firebase = PrefUtils.usingFirebase(mActivity);
        if(mActivity.useFirebase) {
            getPieceFromFirebase();
        } else {
            if(mCursor == null) {
                Timber.d("mCursor is null, initing loader...");
                mActivity.getSupportLoaderManager().initLoader(ID_PIECE_LOADER, null, this);
            } else {
                Timber.d("mCursor exists, going straight to data");
                getPieceFromSql();
            }
        }
    }

    private void getPieceFromFirebase() {
        Timber.d("getPieceFromFirebase()");
        FirebaseDatabase.getInstance().getReference().child("pieces").child(mCurrentPieceKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mCurrentPiece = dataSnapshot.getValue(PieceOfMusic.class);
                        updateVariables();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "A database error occurred. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getPieceFromSql() {
        Timber.d("getPieceFromSql()");
        int localDbId = Integer.parseInt(mCurrentPieceKey);
        mCursor.moveToFirst();
        while(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_ID) != localDbId) {
            Timber.d("_id: " + mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_ID)
                    + " != " + localDbId);
            if(!mCursor.moveToNext()) {
                programNotFoundError(localDbId);
                return;
            }
        }
        PieceOfMusic.Builder builder = new PieceOfMusic.Builder()
                .author(mCursor.getString(ProgramDatabaseSchema.MetProgram.POSITION_COMPOSER))
                .title(mCursor.getString(ProgramDatabaseSchema.MetProgram.POSITION_TITLE))
                .subdivision(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_PRIMANY_SUBDIVISIONS))
                .countOffSubdivision(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_COUNOFF_SUBDIVISIONS))
                .defaultTempo(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_DEFAULT_TEMPO))
                .baselineNoteValue(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_DEFAULT_RHYTHM))
                .tempoMultiplier(mCursor.getDouble(ProgramDatabaseSchema.MetProgram.POSITION_TEMPO_MULTIPLIER))
                .firstMeasureNumber(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_MEASURE_COUNTE_OFFSET))
                .dataEntries(mCursor.getString(ProgramDatabaseSchema.MetProgram.POSITION_DATA_ARRAY))
                .firebaseId(mCursor.getString(ProgramDatabaseSchema.MetProgram.POSITION_FIREBASE_ID));
//        mCursor.close();
        mCurrentPiece = builder.build();
        updateVariables();
    }

    private void programNotFoundError(int id) {
        Toast.makeText(mActivity, "Program with id " + id + " is not found in the database.",
                Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.programmed_fragment, container, false);
        ButterKnife.bind(this, view);

        mActivity.setTitle(R.string.app_name);
//        Timber.d("using firebase? " + mActivity.useFirebase);

        mTempoDownButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mRunnableHandler.post(mDownRunnable);
                        break;
                    case MotionEvent.ACTION_UP:
                        mRunnableHandler.removeCallbacks(mDownRunnable);
                        mTempoChangeDelay = INITIAL_TEMPO_CHANGE_DELAY;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        mTempoUpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mRunnableHandler.post(mUpRunnable);
                        break;
                    case MotionEvent.ACTION_UP:
                        mRunnableHandler.removeCallbacks(mUpRunnable);
                        mTempoChangeDelay = INITIAL_TEMPO_CHANGE_DELAY;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });


        if(mCurrentPiece != null) {
            updateGUI();
        }
        return view;
    }

//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        mActivity.supportStartPostponedEnterTransition();
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Timber.d("onCreateOptionsMenu");
        inflater.inflate(R.menu.programmed_menu, menu);
        MenuItem item = menu.findItem(R.id.mark_as_favorite_menu);
        if(mIsCurrentFavorite) {
            fillMenuItem(item);
        } else {
            unfillMenuItem(item);
        }
    }

    @Override
    public void onPause() {
        if(mMetronomeRunning) metronomeStartStop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy() saving prefs....");
        PrefUtils.saveCurrentProgramToPrefs(mActivity, mActivity.useFirebase,
                mCurrentPieceKey, mCurrentTempo);

        Timber.d("Should have just saved " + mCurrentPieceKey + " at " + mCurrentTempo + " BPM");

        mCursor = null;
        super.onDestroy();
    }

    private void changeTempo(boolean direction) {
        if(direction) {
            mCurrentTempo++;
        } else {
            mCurrentTempo--;
        }
        if(mCurrentTempo < MINIMUM_TEMPO) {
            mCurrentTempo = MINIMUM_TEMPO;
        } else if(mCurrentTempo > MAXIMUM_TEMPO) {
            mCurrentTempo = MAXIMUM_TEMPO;
        }
        updateTempoView();

    }

    private void updateTempoView() {
        mTVCurrentTempo.setText(Integer.toString(mCurrentTempo));
    }

    @OnClick( { R.id.current_composer_name, R.id.current_program_title } )
    public void selectNewProgram() {
        Intent intent = new Intent(mActivity, LoadNewProgramActivity.class)
                .putExtra(EXTRA_COMPOSER_NAME, mCurrentComposer)
                .putExtra(EXTRA_USE_FIREBASE, mActivity.useFirebase);
//        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                mActivity, new Pair<View, String>(mTVCurrentComposer, getString(R.string.transition_composer_name_view)) );
//        startActivityForResult(intent, REQUEST_NEW_PROGRAM, activityOptions.toBundle());
        startActivityForResult(intent, REQUEST_NEW_PROGRAM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("FRAGMENT: onActivityResult()");
        if(resultCode != RESULT_OK) {
            Toast.makeText(mActivity, "Problem with return result", Toast.LENGTH_SHORT).show();
            return;
        }
        switch(requestCode) {
            case REQUEST_NEW_PROGRAM:
                Timber.d("REQUEST_NEW_PROGRAM result received");
                mActivity.useFirebase = PrefUtils.usingFirebase(mActivity);
                mCurrentPieceKey = data.getStringExtra(LoadNewProgramActivity.EXTRA_NEW_PROGRAM);
                getPieceFromKey();
                break;
            default:
        }
    }

    @OnClick(R.id.start_stop_fab)
    public void metronomeStartStop() {
        if(mCurrentPiece == null) {
            Toast.makeText(mActivity, "Please select a program before starting metronome.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mMetronomeRunning) {
            Timber.d("metronomeStop() " + mCurrentComposer);
            mMetronome.stop();
            mMetronomeRunning = false;
            mStartStopButton.setImageResource(android.R.drawable.ic_media_play);
            mCurrentMeasureNumber.setText("--");
        } else {
            Timber.d("metronomeStart() " + mCurrentPiece.getTitle());
            mMetronomeRunning = true;
            mStartStopButton.setImageResource(android.R.drawable.ic_media_pause);
            mMetronome.play(mCurrentPiece, mCurrentTempo);
        }
    }

    @Override
    public void metronomeMeasureNumber(String mm) {
        mCurrentMeasureNumber.setText(mm);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mCurrentPiece != null) {
            Timber.d("onSaveInstanceState() " + mCurrentPiece.getTitle() + " by " + mCurrentComposer);
            Timber.d("..... Current Tempo: " + mCurrentTempo);
            outState.putString(CURRENT_PIECE_TITLE_KEY, mCurrentPiece.getTitle());
            outState.putInt(CURRENT_TEMPO_KEY, mCurrentTempo);
            outState.putString(CURRENT_COMPOSER_KEY, mCurrentComposer);
        }
        super.onSaveInstanceState(outState);

    }

    private int getNoteImageResource(int noteValue) {
        switch(noteValue) {
            case PieceOfMusic.SIXTEENTH:
                return R.drawable.ic_16thnote;
            case PieceOfMusic.DOTTED_SIXTEENTH:
                return R.drawable.ic_dotted16th;
            case PieceOfMusic.EIGHTH:
                return R.drawable.ic_8th;
            case PieceOfMusic.DOTTED_EIGHTH:
                return R.drawable.ic_dotted8th;
            case PieceOfMusic.QUARTER:
                return R.drawable.ic_quarter;
            case PieceOfMusic.DOTTED_QUARTER:
                return R.drawable.ic_dotted_4th;
            case PieceOfMusic.HALF:
                return R.drawable.ic_half;
            case PieceOfMusic.DOTTED_HALF:
                return R.drawable.ic_dotted_2th;
            case PieceOfMusic.WHOLE:
                return R.drawable.ic_whole;
            default:
                return R.drawable.ic_quarter;
        }
    }

    private void updateVariables() {
        if(mCurrentPiece == null) {
            selectNewProgram();
            return;
        }

        Timber.d("newPiece() " + mCurrentPiece.getTitle());

        mCurrentComposer = mCurrentPiece.getAuthor();
//        mCurrentPieceKey = mCurrentPiece.getFirebaseId();
        if(mCurrentPiece.getDefaultTempo() != 0) {
            mCurrentTempo = mCurrentPiece.getDefaultTempo();
        }

        updateGUI();

        if(mCurrentPiece.getFirebaseId() != null) {
            new CheckIfFavoriteTask().execute(mCurrentPiece.getFirebaseId());
        }
    }

    private void updateGUI() {
        if(mCurrentPiece == null) {
            mTVCurrentPiece.setText(R.string.no_composer_empty_space);
            mTVCurrentComposer.setText(R.string.select_a_program);
        } else {
            mTVCurrentPiece.setText(mCurrentPiece.getTitle());
            mTVCurrentComposer.setText(mCurrentComposer);
            mBeatLengthImage.setImageResource(getNoteImageResource
                    (mCurrentPiece.getBaselineNoteValue()));
            mCurrentTempo = mCurrentPiece.getDefaultTempo();
            updateTempoView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("FRAGMENT: onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.create_new_program_option:
                openProgramEditor();
                return true;
            case R.id.mark_as_favorite_menu:
                if(mCurrentPiece == null) {
                    Toast.makeText(mActivity, R.string.need_program_before_favorite,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                mIsCurrentFavorite = !mIsCurrentFavorite;
                if(mIsCurrentFavorite) {
                    fillMenuItem(item);
                    makePieceFavorite();
                    saveToSql();
                } else {
                    unfillMenuItem(item);
                    makePieceUnfavorite();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openProgramEditor() {
        Fragment fragment = MetaDataEntryFragment.newInstance(mActivity, mCursor);
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, fragment);
        trans.addToBackStack(null);
        trans.commit();
    }

    private void fillMenuItem(MenuItem item) {
        item.setIcon(R.drawable.ic_heart);
        item.setTitle(getString(R.string.mark_as_unfavorite_menu));
    }

    private void unfillMenuItem(MenuItem item) {
        item.setIcon(R.drawable.ic_heart_outline);
        item.setTitle(getString(R.string.mark_as_favorite_menu));
    }

    private void makePieceFavorite() {
        final SQLiteDatabase db = new FavoritesDBHelper(mActivity).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoritesContract.FavoriteEntry.COLUMN_PIECE_ID, mCurrentPiece.getFirebaseId());
        db.insert(FavoritesContract.FavoriteEntry.TABLE_NAME, null, values);
        db.close();
    }
    private void makePieceUnfavorite() {
        final SQLiteDatabase db = new FavoritesDBHelper(mActivity).getWritableDatabase();
        String selection = FavoritesContract.FavoriteEntry.COLUMN_PIECE_ID + " LIKE ?";
        String[] selectionArgs = { mCurrentPiece.getFirebaseId() };
        db.delete(FavoritesContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    private void saveToSql() {
        ContentValues contentValues = Utilities.getContentValuesFromPiece(mCurrentPiece);
        ContentResolver resolver = getContext().getContentResolver();
        resolver.insert(ProgramDatabaseSchema.MetProgram.CONTENT_URI, contentValues);
    }

    private class CheckIfFavoriteTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Timber.d("CheckIfFavoriteTask running in background!!");
            SQLiteDatabase db = new FavoritesDBHelper(mActivity).getReadableDatabase();
            Cursor cursor = db.query(FavoritesContract.FavoriteEntry.TABLE_NAME,
                    null,
                    FavoritesContract.FavoriteEntry.COLUMN_PIECE_ID + " =?",
                    params,
                    null, null, null);
            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            db.close();
            return exists;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mIsCurrentFavorite = aBoolean;
            mActivity.invalidateOptionsMenu();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("loader creation...");
        switch(id) {
            case ID_PIECE_LOADER:
                Uri queryUri = ProgramDatabaseSchema.MetProgram.CONTENT_URI;
                Timber.d("Uri: " + queryUri.toString());
                return new CursorLoader(mActivity,
                        queryUri,
                        null,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Unimplemented Loader Problem: " + id);
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Timber.d("loader finished...");
        if(data == null || data.getCount() == 0) {
            Toast.makeText(mActivity, "Program Load Error", Toast.LENGTH_SHORT).show();
            mCurrentPiece = null;
            updateGUI();
        } else {
            mCursor = data;
            getPieceFromSql();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Timber.d("onLoaderReset()");
        Timber.d("currentKey = " + mCurrentPieceKey);
        mCursor = null;
    }
}
