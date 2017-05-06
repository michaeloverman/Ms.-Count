/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.dataentry;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.database.LoadNewProgramActivity;
import tech.michaeloverman.android.mscount.database.ProgramDatabaseSchema;
import tech.michaeloverman.android.mscount.pojos.DataEntry;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import tech.michaeloverman.android.mscount.programmed.ProgrammedMetronomeActivity;
import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.Utilities;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 *
 */
public class MetaDataEntryFragment extends Fragment
        implements DataEntryFragment.DataEntryCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_NEW_PROGRAM = 1984;

    @BindView(R.id.composer_name_text_entry) EditText mComposerEntry;
    @BindView(R.id.title_text_entry) EditText mTitleEntry;
    @BindView(R.id.baseline_subdivision_entry) EditText mBaselineSubdivisionEntry;
    @BindView(R.id.countoff_subdivision_entry) EditText mCountoffSubdivisionEntry;
    @BindView(R.id.default_tempo_label) TextView mDefaultTempoLabel;
    @BindView(R.id.default_tempo_entry) EditText mDefaultTempoEntry;
    @BindView(R.id.enter_beats_button) Button mEnterBeatsButton;
    @BindView(R.id.baseline_rhythmic_value_recycler) RecyclerView mBaselineRhythmicValueEntry;
    private NoteValuesAdapter mBaselineRhythmicValueAdapter;
    private int mTemporaryBaselineRhythm = 4;
    private float mBaselineMultiplier = 1.0f;

    private PieceOfMusic mPieceOfMusic;
    private PieceOfMusic.Builder mBuilder;
    private String mCurrentPieceKey;
    private String mFirebaseId;
    private List<DataEntry> mDataEntries;
    private List<Integer> mDownBeats;

    private static ProgrammedMetronomeActivity mActivity;
    private static final int ID_PIECE_LOADER = 435;
    private static Cursor mCursor;

    public static Fragment newInstance(ProgrammedMetronomeActivity a, Cursor c) {
        Timber.d("newInstance()");
        mActivity = a;
        mCursor = c;
        return new MetaDataEntryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate()");
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mBuilder = new PieceOfMusic.Builder();
        mPieceOfMusic = new PieceOfMusic();

    }

//    private void rewriteAllProgramsWithCreatorId() {
//        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference();
//        dRef.child("pieces")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Iterable<DataSnapshot> programs = dataSnapshot.getChildren();
//                        for(DataSnapshot snap : programs) {
//                            PieceOfMusic p = snap.getValue(PieceOfMusic.class);
//                            p.setCreatorId("dvM60nH1mHYBYjuBAxminpA4Zve2");
//                            Map<String, Object> update = new HashMap<>();
//                            update.put("/pieces/" + snap.getKey(), p);
//                            dRef.updateChildren(update);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.metadata_entry_menu, menu);
//        menu.removeItem(R.id.create_new_program_option);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("onCreateView()");
        View view = inflater.inflate(R.layout.meta_data_input_layout, container, false);
        ButterKnife.bind(this, view);

        // When a countoff value is entered, make sure it is an even divisor of the baseline subdivisions
        mCountoffSubdivisionEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int primary = Integer.parseInt(mBaselineSubdivisionEntry.getText().toString());
                    int countoff = Integer.parseInt(s.toString());
                    if(primary % countoff != 0) {
                        Toast.makeText(getContext(),
                                R.string.countoff_must_fit_subdivisions,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException n) {
                    return;
                }
            }
        });

        // When default tempo is entered, make sure it is in the metronome's range
        mDefaultTempoEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    try {
                        int tempo = Integer.parseInt(mDefaultTempoEntry.getText().toString());
                        if(tempo < Metronome.MIN_TEMPO || tempo > Metronome.MAX_TEMPO) {
                            Toast.makeText(getContext(), String.format(
                                    getString(R.string.tempo_between_min_max),
                                            Metronome.MIN_TEMPO, Metronome.MAX_TEMPO),
                                    Toast.LENGTH_SHORT).show();
                            mDefaultTempoEntry.setText("");
                            return;
                        }
                    } catch (NumberFormatException n) {
                        Toast.makeText(getContext(), R.string.tempo_must_be_integer, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        RecyclerView.LayoutManager manager = new LinearLayoutManager(mActivity,
                LinearLayoutManager.HORIZONTAL, false);
        mBaselineRhythmicValueEntry.setLayoutManager(manager);
        mBaselineRhythmicValueAdapter = new NoteValuesAdapter(
                getResources().obtainTypedArray(R.array.note_values));
        mBaselineRhythmicValueEntry.setAdapter(mBaselineRhythmicValueAdapter);
        mBaselineRhythmicValueAdapter.setSelectedPosition(mTemporaryBaselineRhythm);

        mBaselineRhythmicValueEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    InputMethodManager imm = (InputMethodManager) v.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mBaselineMultiplier != 1.0f) {
            int current = Integer.parseInt(mBaselineSubdivisionEntry.getText().toString());
            float multiplied = current * mBaselineMultiplier;
            mBaselineSubdivisionEntry.setText((int) multiplied + "");
            mBaselineMultiplier = 1.0f;
        }
    }

    @OnClick(R.id.enter_beats_button)
    public void enterBeatsClicked() {
        Timber.d("enterBeatsClicked()");
        String composer = mComposerEntry.getText().toString();
        if(composer.equals("")) {
            toastError();
            return;
        }

        String title = mTitleEntry.getText().toString();
        if(title.equals("")) {
            toastError();
            return;
        }

        mBuilder.author(composer)
                .title(title);

        mTemporaryBaselineRhythm = mBaselineRhythmicValueAdapter.getSelectedRhythm();
        Timber.d("mTemporaryBaselineRhythm" + mTemporaryBaselineRhythm);
        gotoDataEntryFragment(title);
    }

    private void gotoDataEntryFragment(String title) {
        Fragment fragment;
        if(mDataEntries == null) {
            fragment = DataEntryFragment.newInstance(title, this, mBuilder);
        } else {
            fragment = DataEntryFragment.newInstance(title, this, mBuilder, mDataEntries);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_existing_program_option:
                loadProgram();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadProgram() {
        Intent intent = new Intent(mActivity, LoadNewProgramActivity.class);
        startActivityForResult(intent, REQUEST_NEW_PROGRAM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) {
            Toast.makeText(mActivity, R.string.problem_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        switch(requestCode) {
            case REQUEST_NEW_PROGRAM:
                getPieceFromKey(data.getStringExtra(LoadNewProgramActivity.EXTRA_NEW_PROGRAM));
                break;
            default:
        }
    }

    private void getPieceFromKey(String key) {

        mCurrentPieceKey = key;

        if(mCurrentPieceKey.charAt(0) == '-') {
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
                        mPieceOfMusic = dataSnapshot.getValue(PieceOfMusic.class);
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
        mPieceOfMusic = builder.build();
        updateVariables();
    }

    private void programNotFoundError(int id) {
        Toast.makeText(mActivity, "Program with id " + id + " is not found in the database.",
                Toast.LENGTH_SHORT).show();
    }

    private void updateVariables() {
        if(mPieceOfMusic.getRawData() == null) {
            mPieceOfMusic.constructRawData();
        }
        mFirebaseId = mPieceOfMusic.getFirebaseId();
        updateGUI();
    }

    private void updateGUI() {
        mComposerEntry.setText(mPieceOfMusic.getAuthor());
        mTitleEntry.setText(mPieceOfMusic.getTitle());
        mBaselineSubdivisionEntry.setText(Integer.toString(mPieceOfMusic.getSubdivision()));
        mCountoffSubdivisionEntry.setText(Integer.toString(mPieceOfMusic.getCountOffSubdivision()));
        mDefaultTempoEntry.setText(Integer.toString(mPieceOfMusic.getDefaultTempo()));
        mBaselineRhythmicValueAdapter.setSelectedPosition(mPieceOfMusic.getBaselineNoteValue());
        mBaselineRhythmicValueAdapter.notifyDataSetChanged();
        mDataEntries = mPieceOfMusic.getRawData();
    }

    @OnClick(R.id.save_program_button)
    public void saveProgram() {

        if (!validateDataEntries()) return;

        // get all the metadata fields
        if (!validateMetaDataEntries()) return;

        mBuilder.firebaseId(mFirebaseId);

        mBuilder.creatorId(getFirebaseAuthId());

        mPieceOfMusic = mBuilder.build();

        if(mActivity.useFirebase) {
            checkFirebaseForExistingData(); // beginning of method chain to save to cloud
        } else {
            saveToSqlDatabase();
        }
    }

    private String getFirebaseAuthId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    private boolean validateMetaDataEntries() {
        String composer = mComposerEntry.getText().toString();
        String title = mTitleEntry.getText().toString();
        String subd = mBaselineSubdivisionEntry.getText().toString();
        String countoff = mCountoffSubdivisionEntry.getText().toString();
        String defaultTempo = mDefaultTempoEntry.getText().toString();
        int rhythm = mBaselineRhythmicValueAdapter.getSelectedRhythm();

        if(composer.equals("")) {
            Toast.makeText(getContext(), R.string.error_no_composer_message,
                    Toast.LENGTH_SHORT).show();
            mComposerEntry.requestFocus();
            return false;
        }

        if(title.equals("")) {
            Toast.makeText(getContext(), R.string.error_no_title_message,
                    Toast.LENGTH_SHORT).show();
            mTitleEntry.requestFocus();
            return false;
        }

        if(subd.equals("")) {
            Toast.makeText(getContext(), R.string.error_no_subdivision_message,
                    Toast.LENGTH_SHORT).show();
            mBaselineSubdivisionEntry.requestFocus();
            return false;
        }

        if(countoff.equals("")) {
            Toast.makeText(getContext(), R.string.error_no_countoff_message,
                    Toast.LENGTH_SHORT).show();
            mCountoffSubdivisionEntry.requestFocus();
            return false;
        }

        int subdInt, countoffInt;
        try {
            subdInt = Integer.parseInt(subd);
            if(subdInt < 1 || subdInt > 24) {
                Toast.makeText(getContext(), "Subdivisions out of range.", Toast.LENGTH_SHORT).show();
            } else if (subdInt > 12) {
                //TODO dialog box to confirm unusually large baseline subdivision
            }
        } catch (NumberFormatException nfe) {
            Toast.makeText(getContext(), "Please enter only numbers for subdivisions.", Toast.LENGTH_SHORT).show();
            mBaselineSubdivisionEntry.requestFocus();
            return false;
        }

        try {
            countoffInt = Integer.parseInt(countoff);
            if(countoffInt > subdInt || countoffInt == 0) {
                Toast.makeText(getContext(),
                        "Countoff subdivisions must be an even divisor of the baseline subdivisions.",
                        Toast.LENGTH_SHORT).show();
                mCountoffSubdivisionEntry.requestFocus();
                return false;
            }
        } catch (NumberFormatException nfe) {
            Toast.makeText(getContext(), "Please enter only numbers for countoff.", Toast.LENGTH_SHORT).show();
            mBaselineSubdivisionEntry.requestFocus();
            return false;
        }


        mBuilder.author(composer)
                .title(title)
                .subdivision(subdInt)
                .countOffSubdivision(countoffInt);
        int tempoInt;
        try {
            if(!defaultTempo.equals("")) {
                tempoInt = Integer.parseInt(defaultTempo);
                mBuilder.defaultTempo(tempoInt);
            }
        } catch (NumberFormatException nfe) {
            Timber.d("You should not be here: should not be able to enter anything but numbers, and any numbers entered have already been checked for range.");
        }

        mBuilder.baselineNoteValue(rhythm);

//        try {
//            if(!rhythm.equals("")) {
//                rhythmInt = Integer.parseInt(rhythm);
//            }
//        } catch (NumberFormatException nfe) {
//            Timber.d("You really shouldn't be here, with a NumberFormatException on the baseline rhythmic value.");
//        }

        // TODO Get other meta data entries: tempo multiplier, etc.


        return true;
    }

    private boolean validateDataEntries() {
        if(mDataEntries == null || mDataEntries.size() == 0) {
            Toast.makeText(getContext(), R.string.enter_data_before_saving,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        mBuilder.dataEntries(mDataEntries);
        return true;
    }

    /**
     * Checks database for existence same title by same composer. If it doesn't exist,
     * goes ahead and saves. If it does exists, call on a dialog to confirm before saving.
     */
//    private void checkFirebaseForExistingData() {
//
////        if(mActivity.useFirebase) {
//        if(true) {
//            checkFirebaseForExistence();
//        } else {
//            saveToSqlDatabase();
//        }
//    }

    private void saveToSqlDatabase() {
        Timber.d("this where it should be saving to sql");
        ContentValues contentValues = Utilities.getContentValuesFromPiece(mPieceOfMusic);
        ContentResolver resolver = getContext().getContentResolver();
        Uri returnUri = resolver.insert(ProgramDatabaseSchema.MetProgram.CONTENT_URI, contentValues);
        if(returnUri != null) {
            getFragmentManager().popBackStackImmediate();
        } else {
            databaseSaveErrorStayHere();
        }
    }

    private void checkFirebaseForExistingData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference();

        // Look for piece first, and if exists, get that key to update; otherwise push() to create
        // new key for new piece.
        final String composer = mPieceOfMusic.getAuthor();
        final String title = mPieceOfMusic.getTitle();
        databaseReference.child("composers").child(composer).child(title)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String key = dataSnapshot.getValue().toString();
                            checkIfAuthorizedCreator(key);
                        } else {
                            saveToFirebase(mPieceOfMusic);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error: Database problem. Save canceled.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAuthorizedCreator(String key) {
        FirebaseDatabase.getInstance().getReference().child("pieces").child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        PieceOfMusic pieceFromFirebase = dataSnapshot.getValue(PieceOfMusic.class);

                        if(pieceFromFirebase.getCreatorId().equals(mPieceOfMusic.getCreatorId())) {
                            overwriteFirebaseDataAlertDialog(mPieceOfMusic.getTitle(), mPieceOfMusic.getAuthor());
                        } else {
                            Toast.makeText(mActivity, R.string.not_authorized_save_local,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Called prior to saving, if piece by same title already exists in database. If confirmed,
     * overwrites data, if canceled, does nothing.
     *
     * @param title
     * @param composer
     */
    private void overwriteFirebaseDataAlertDialog(final String title, final String composer) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setCancelable(false);
        dialog.setTitle("Overwrite Data?");
        dialog.setMessage(String.format(getResources().getString(R.string.overwrite_data_confirmation), title, composer));
        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveToFirebase(mPieceOfMusic);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Action for "Cancel".
                    }
                });

        final AlertDialog alert = dialog.create();
        alert.show();
    }

    public void saveToFirebase(final PieceOfMusic p) {
        Timber.d("Saving to local database, or to Firebase: " + p.getTitle() + " by " + p.getAuthor());
//        Timber.d("Pieces is " + p.getDownBeats().size() + " measures long.");

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mPiecesDatabaseReference = mDatabase.getReference();

        // Look for piece first, and if exists, get that key to update; otherwise push() to create
        // new key for new piece.
        mPiecesDatabaseReference.child("composers").child(p.getAuthor()).child(p.getTitle())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key;
                        if(dataSnapshot.exists()) {
                            // update
                            key = dataSnapshot.getValue().toString();
                        } else {
                            // push to create
                            key = mPiecesDatabaseReference.child("pieces").push().getKey();
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("/pieces/" + key, p);
                        updates.put("/composers/" + p.getAuthor() + "/" + p.getTitle(), key);
                        mPiecesDatabaseReference.updateChildren(updates);

                        getFragmentManager().popBackStackImmediate();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        databaseSaveErrorStayHere();
                    }
                });
    }

    private void databaseSaveErrorStayHere() {
        Toast.makeText(getContext(), "Error: Save to database cancelled.",
                Toast.LENGTH_SHORT).show();
    }

    private void toastError() {
        Toast.makeText(this.getContext(),
                "Please enter a composer name and title before continuing.",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback from the DataEntryFragment. Takes the raw data and the reference to the original
     * Builder object, and assigns to local variables.
     * @param data
     * @param builder
     */
    @Override
    public void returnDataList(List<DataEntry> data, PieceOfMusic.Builder builder, float baselineMultiplier) {
        mBuilder = builder;
        mDataEntries = data;
        Timber.d("mTemporaryBaselineRhythm: " + mTemporaryBaselineRhythm);
        mBaselineRhythmicValueAdapter.setSelectedPosition(mTemporaryBaselineRhythm);
        if(baselineMultiplier != 1) {
            mBaselineMultiplier = baselineMultiplier;
        }
//        mBaselineRhythmicValueAdapter.notifyDataSetChanged();
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
            mPieceOfMusic = null;
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

    class NoteValuesAdapter extends RecyclerView.Adapter<NoteValuesAdapter.NoteViewHolder> {

        TypedArray noteValueImages;
        private int selectedPosition;

        public NoteValuesAdapter(TypedArray images) {
//            Timber.d("NoteValuesAdapter CREATED!!!");
//            Timber.d("TypedArray: " + images.toString());
            noteValueImages = images;
//            Timber.d("Index count: " + noteValueImages.length());
        }

        public int getSelectedRhythm() {
            switch(selectedPosition) {
                case 0: return PieceOfMusic.SIXTEENTH;
                case 1: return PieceOfMusic.DOTTED_SIXTEENTH;
                case 2: return PieceOfMusic.EIGHTH;
                case 3: return PieceOfMusic.DOTTED_EIGHTH;
                case 4: return PieceOfMusic.QUARTER;
                case 5: return PieceOfMusic.DOTTED_QUARTER;
                case 6: return PieceOfMusic.HALF;
                case 7: return PieceOfMusic.DOTTED_HALF;
                case 8: return PieceOfMusic.WHOLE;
                default: return PieceOfMusic.QUARTER;
            }
        }

        public void setSelectedPosition(int rhythm) {
            Timber.d("setting selected rhythmic value..." + rhythm);
            switch(rhythm) {
                case PieceOfMusic.SIXTEENTH:
                    selectedPosition = 0;
                    break;
                case PieceOfMusic.DOTTED_SIXTEENTH:
                    selectedPosition = 1;
                    break;
                case PieceOfMusic.EIGHTH:
                    selectedPosition = 2;
                    break;
                case PieceOfMusic.DOTTED_EIGHTH:
                    selectedPosition = 3;
                    break;
                case PieceOfMusic.QUARTER:
                    selectedPosition = 4;
                    break;
                case PieceOfMusic.DOTTED_QUARTER:
                    selectedPosition = 5;
                    break;
                case PieceOfMusic.HALF:
                    selectedPosition = 6;
                    break;
                case PieceOfMusic.DOTTED_HALF:
                    selectedPosition = 7;
                    break;
                case PieceOfMusic.WHOLE:
                    selectedPosition = 8;
                    break;
                default: selectedPosition = 4;
            }
            Timber.d("selectedPosition = " + selectedPosition);
        }

        @Override
        public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.note_value_image_view, parent, false);
            return new NoteViewHolder(item);
        }

        @Override
        public void onBindViewHolder(NoteViewHolder holder, final int position) {
            holder.image.setImageDrawable(noteValueImages.getDrawable(position));
            Timber.d("onBindViewHolder, position: " + position + " selected: " + (position == selectedPosition));

            if(selectedPosition == position) {
                holder.itemView.setBackground(getResources().getDrawable(R.drawable.roundcorner_accent));
            } else {
                holder.itemView.setBackground(getResources().getDrawable(R.drawable.roundcorner_parchment));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemChanged(selectedPosition);
                    selectedPosition = position;
                    notifyItemChanged(selectedPosition);
                }
            });
        }

        @Override
        public int getItemCount() {
            return noteValueImages.length();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder {
            ImageView image;

            public NoteViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.note_value_image);
                Timber.d("NoteViewHolder created, image: ");
            }
        }
    }
}
