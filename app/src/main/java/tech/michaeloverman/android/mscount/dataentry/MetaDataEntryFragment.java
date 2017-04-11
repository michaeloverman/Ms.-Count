package tech.michaeloverman.android.mscount.dataentry;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
        implements DataEntryFragment.DataEntryCallback {
    private static final int REQUEST_NEW_PROGRAM = 1984;

    @BindView(R.id.composer_name_text_entry) EditText mComposerEntry;
    @BindView(R.id.title_text_entry) EditText mTitleEntry;
    @BindView(R.id.baseline_subdivision_entry) EditText mBaselineSubdivisionEntry;
    @BindView(R.id.countoff_subdivision_entry) EditText mCountoffSubdivisionEntry;
    @BindView(R.id.default_tempo_label) TextView mDefaultTempoLabel;
    @BindView(R.id.default_tempo_entry) EditText mDefaultTempoEntry;
    @BindView(R.id.enter_beats_button) Button mEnterBeatsButton;
    @BindView(R.id.baseline_rhythmic_value_entry) EditText mBaselineRhythmicValueEntry;

    private PieceOfMusic mPieceOfMusic;
    private PieceOfMusic.Builder mBuilder;
    private List<DataEntry> mDataEntries;
    private List<Integer> mDownBeats;

    private static ProgrammedMetronomeActivity mActivity;
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

        return view;
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
                mPieceOfMusic = (PieceOfMusic) data.getSerializableExtra(
                        LoadNewProgramActivity.EXTRA_NEW_PROGRAM);
                if(mPieceOfMusic.getRawData() == null) {
                    mPieceOfMusic.constructRawData();
                }
                updateGUI();
                break;
            default:
        }
    }

    private void updateGUI() {
        mComposerEntry.setText(mPieceOfMusic.getAuthor());
        mTitleEntry.setText(mPieceOfMusic.getTitle());
        mBaselineSubdivisionEntry.setText(Integer.toString(mPieceOfMusic.getSubdivision()));
        mCountoffSubdivisionEntry.setText(Integer.toString(mPieceOfMusic.getCountOffSubdivision()));
        mDefaultTempoEntry.setText(Integer.toString(mPieceOfMusic.getDefaultTempo()));
        mBaselineRhythmicValueEntry.setText(Integer.toString(mPieceOfMusic.getBaselineNoteValue()));
        mDataEntries = mPieceOfMusic.getRawData();
    }

    @OnClick(R.id.save_program_button)
    public void saveProgram() {

        if (!validateDataEntries()) return;

        // get all the metadata fields
        if (!validateMetaDataEntries()) return;

        getFirebaseAuthId();

        mPieceOfMusic = mBuilder.build();

        if(mActivity.useFirebase()) {
            checkFirebaseForExistingData(); // beginning of method chain to save to cloud
        } else {
            saveToSqlDatabase();
        }
    }

    private void getFirebaseAuthId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth != null) {
            String creator = auth.getCurrentUser().getUid();
            mBuilder.creatorId(creator);
        }
    }

    private boolean validateMetaDataEntries() {
        String composer = mComposerEntry.getText().toString();
        String title = mTitleEntry.getText().toString();
        String subd = mBaselineSubdivisionEntry.getText().toString();
        String countoff = mCountoffSubdivisionEntry.getText().toString();
        String defaultTempo = mDefaultTempoEntry.getText().toString();
        String rhythm = mBaselineRhythmicValueEntry.getText().toString();

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
        int tempoInt, rhythmInt;
        try {
            if(!defaultTempo.equals("")) {
                tempoInt = Integer.parseInt(defaultTempo);
                mBuilder.defaultTempo(tempoInt);
            }
        } catch (NumberFormatException nfe) {
            Timber.d("You should not be here: should not be able to enter anything but numbers, and any numbers entered have already been checked for range.");
        }

        // TODO redo the UI so this is not a raw data input, but a selection from various note values
        try {
            if(!rhythm.equals("")) {
                rhythmInt = Integer.parseInt(rhythm);
                mBuilder.baselineNoteValue(rhythmInt);
            }
        } catch (NumberFormatException nfe) {
            Timber.d("You really shouldn't be here, with a NumberFormatException on the baseline rhythmic value.");
        }

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
//                        Timber.d("mPiece: " + mPieceOfMusic.getCreatorId());
//                        Timber.d("firePi: " + pieceFromFirebase.getCreatorId());
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
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setCancelable(false);
        dialog.setTitle("Overwrite Data?");
        dialog.setMessage(String.format(getResources().getString(R.string.overwrite_data_confirmation), title, composer));
        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveToFirebase(mPieceOfMusic);
                    }
                })
                .setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Action for "Cancel".
                    }
                });

        final AlertDialog alert = dialog.create();
        alert.show();
    }

    private void saveToFirebase(final PieceOfMusic p) {
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
    public void returnDataList(List<DataEntry> data, PieceOfMusic.Builder builder) {
        mBuilder = builder;
        mDataEntries = data;
    }

}
