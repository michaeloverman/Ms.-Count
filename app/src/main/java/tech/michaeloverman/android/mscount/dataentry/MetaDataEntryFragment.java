package tech.michaeloverman.android.mscount.dataentry;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import tech.michaeloverman.android.mscount.pojos.DataEntry;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import tech.michaeloverman.android.mscount.programmed.ProgramSelectFragment;
import tech.michaeloverman.android.mscount.utils.Metronome;

/**
 *
 */
public class MetaDataEntryFragment extends Fragment
        implements DataEntryFragment.DataEntryCallback, ProgramSelectFragment.ProgramCallback {
    private static final String TAG = MetaDataEntryFragment.class.getSimpleName();

    @BindView(R.id.composer_name_text_entry)
    EditText mComposerEntry;
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

    public static Fragment newInstance() {
        Log.d(TAG, "newInstance()");
        return new MetaDataEntryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mBuilder = new PieceOfMusic.Builder();
        mPieceOfMusic = new PieceOfMusic();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
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
                                "Countoff subdivisions must divide evenly into baseline subdivisions.",
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
                                    "Tempo must be between %d and %d", Metronome.MIN_TEMPO, Metronome.MAX_TEMPO),
                                    Toast.LENGTH_SHORT).show();
                            mDefaultTempoEntry.setText("");
                            return;
                        }
                    } catch (NumberFormatException n) {
                        Toast.makeText(getContext(), "Tempo must be an integer.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        return view;
    }

    @OnClick(R.id.enter_beats_button)
    public void enterBeatsClicked() {
        Log.d(TAG, "enterBeatsClicked()");
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

        checkForExistingData(composer, title);
    }

    @OnClick(R.id.load_program_button)
    public void loadProgram() {
        Fragment fragment = ProgramSelectFragment.newInstance(this, null);
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, fragment);
        trans.addToBackStack(null);
        trans.commit();
    }

    @Override
    public void newPiece(String pieceId) {

        FirebaseDatabase.getInstance().getReference().child("pieces").child(pieceId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mPieceOfMusic = dataSnapshot.getValue(PieceOfMusic.class);
                        updateGUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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

        if(mDataEntries == null || mDataEntries.size() == 0) {
            Toast.makeText(getContext(), "Error: Please Click \"Program Beats\" to enter your program.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mBuilder.dataEntries(mDataEntries);

        // get all the metadata fields
        String composer = mComposerEntry.getText().toString();
        String title = mTitleEntry.getText().toString();
        String subd = mBaselineSubdivisionEntry.getText().toString();
        String countoff = mCountoffSubdivisionEntry.getText().toString();
        String defaultTempo = mDefaultTempoEntry.getText().toString();
        String rhythm = mBaselineRhythmicValueEntry.getText().toString();

        if(composer.equals("")) {
            Toast.makeText(getContext(), "Error: Please enter a composer's name.",
                    Toast.LENGTH_SHORT).show();
            mComposerEntry.requestFocus();
            return;
        }

        if(title.equals("")) {
            Toast.makeText(getContext(), "Error: Please enter a title.",
                    Toast.LENGTH_SHORT).show();
            mTitleEntry.requestFocus();
            return;
        }

        if(subd.equals("")) {
            Toast.makeText(getContext(), "Error: Please enter a baseline subdivision.",
                    Toast.LENGTH_SHORT).show();
            mBaselineSubdivisionEntry.requestFocus();
            return;
        }

        if(countoff.equals("")) {
            Toast.makeText(getContext(), "Error: Please enter a countoff subdivision.",
                    Toast.LENGTH_SHORT).show();
            mCountoffSubdivisionEntry.requestFocus();
            return;
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
            return;
        }

        try {
            countoffInt = Integer.parseInt(countoff);
            if(countoffInt > subdInt || countoffInt == 0) {
                Toast.makeText(getContext(),
                        "Countoff subdivisions must be an even divisor of the baseline subdivisions.",
                        Toast.LENGTH_SHORT).show();
                mCountoffSubdivisionEntry.requestFocus();
                return;
            }
        } catch (NumberFormatException nfe) {
            Toast.makeText(getContext(), "Please enter only numbers for countoff.", Toast.LENGTH_SHORT).show();
            mBaselineSubdivisionEntry.requestFocus();
            return;
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
            Log.d(TAG, "You should not be here: should not be able to enter anything but numbers, and any numbers entered have already been checked for range.");
        }

        // TODO redo the UI so this is not a raw data input, but a selection from various note values
        try {
            if(!rhythm.equals("")) {
                rhythmInt = Integer.parseInt(rhythm);
                mBuilder.baselineNoteValue(rhythmInt);
            }
        } catch (NumberFormatException nfe) {
            Log.d(TAG, "You really shouldn't be here, with a NumberFormatException on the baseline rhythmic value.");
        }

        // get other optional data entries, if present: tempo multiplier,



        mPieceOfMusic = mBuilder.build();
        saveToDatabase(mPieceOfMusic);

        getFragmentManager().popBackStackImmediate();

    }

    private void checkForExistingData(final String composer, final String title) {

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mPiecesDatabaseReference = mDatabase.getReference();

        // Look for piece first, and if exists, get that key to update; otherwise push() to create
        // new key for new piece.
        mPiecesDatabaseReference.child("composers").child(composer).child(title)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            overwriteDataAlertDialog(title);
                        } else {
                            gotoDataEntryFragment(title);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error: Database connection problem.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void overwriteDataAlertDialog(final String title) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setCancelable(false);
        dialog.setTitle("Overwrite Data?");
        dialog.setMessage("You are about to overwrite existing data. Are you sure you want to continue?" );
        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        gotoDataEntryFragment(title);
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

    private void gotoDataEntryFragment(String title) {
        Fragment fragment;
        if(mDataEntries == null) {
            fragment = DataEntryFragment.newInstance(title, MetaDataEntryFragment.this, mBuilder);
        } else {
            fragment = DataEntryFragment.newInstance(title, MetaDataEntryFragment.this, mBuilder, mDataEntries);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private void saveToDatabase(final PieceOfMusic p) {
        Log.d(TAG, "Saving to local database, or to Firebase: " + p.getTitle() + " by " + p.getAuthor());
        Log.d(TAG, "Pieces is " + p.getDownBeats().size() + " measures long.");

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
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error: Save to database cancelled.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
