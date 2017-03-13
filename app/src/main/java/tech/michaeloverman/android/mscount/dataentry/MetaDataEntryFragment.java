package tech.michaeloverman.android.mscount.dataentry;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.pojos.DataEntry;
import tech.michaeloverman.android.mscount.pojos.HardData;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import tech.michaeloverman.android.mscount.utils.Utilities;

/**
 *
 */
public class MetaDataEntryFragment extends Fragment
        implements DataEntryFragment.DataEntryCallback {
    private static final String TAG = MetaDataEntryFragment.class.getSimpleName();

    @BindView(R.id.composer_name_text_entry)
    EditText mComposerEntry;
    @BindView(R.id.title_text_entry) EditText mTitleEntry;
    @BindView(R.id.baseline_subdivision_entry) EditText mBaselineSubdivisionEntry;
    @BindView(R.id.countoff_subdivision_entry) EditText mCountoffSubdivisionEntry;
    @BindView(R.id.default_tempo_label) TextView mDefaultTempoLabel;
    @BindView(R.id.default_tempo_entry) EditText mDefaultTempoEntry;
//    @BindView(R.id.first_beat_subdivisions_entry) EditText mFirstBeatEntry;
//    @BindView(R.id.beats_entry_container) LinearLayout mBeatEntryContainer;
    @BindView(R.id.enter_beats_button) Button mEnterBeatsButton;
    @BindView(R.id.baseline_rhythmic_value_entry) EditText mBaselineRhythmicValueEntry;

    private PieceOfMusic mPieceOfMusic;
    private PieceOfMusic.Builder mBuilder;
    private List<Integer> mBeats;
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

        mPieceOfMusic = new PieceOfMusic();
        mBeats = new ArrayList<>();
        mDownBeats = new ArrayList<>();

//        loadNewPieces();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.meta_data_input_layout, container, false);
        ButterKnife.bind(this, view);
//        mBeatEntryContainer = (LinearLayout) this.getActivity().findViewById(R.id.beats_entry_container);
//        Log.d(TAG, mBeatEntryContainer.toString());

//        mDefaultTempoLabel.setText(getString(R.string.beats_in_current_measure, mDownBeats.size() + 1));
//        mDefaultTempoEntry.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(s.length() < 1) return;
//                int beats = Integer.parseInt(s.toString());
//                createBeatEntries(beats);
//            }
//        });

        return view;
    }

    @OnClick(R.id.enter_beats_button)
    public void enterBeatsClicked() {
        String composer = mComposerEntry.getText().toString();
        if(composer == null) {
            toastError();
            return;
        }
        String title = mTitleEntry.getText().toString();
        if(title == null) {
            toastError();
            return;
        }
        String subd = mBaselineSubdivisionEntry.getText().toString();
        if(subd == null) {
            toastError();
            return;
        }
        String countoffSubd = mCountoffSubdivisionEntry.getText().toString();
        if(countoffSubd == null) {
            toastError();
            return;
        }
        String defaultTempo = mDefaultTempoEntry.getText().toString();
        if(defaultTempo == null) {
            toastError();
            return;
        }
        String countOffValue = mBaselineRhythmicValueEntry.getText().toString();
        if(countOffValue == null) {
            toastError();
            return;
        }

        mBuilder = new PieceOfMusic.Builder()
                .author(composer)
                .title(title)
                .subdivision(Integer.parseInt(subd))
                .countOffSubdivision(Integer.parseInt(countoffSubd))
                .defaultTempo(Integer.parseInt(defaultTempo))
                .baselineNoteValue(Integer.parseInt(countOffValue));

//        mPieceOfMusic = mBuilder.build();
        gotoDataEntryFragment(title);
    }

//    private void createBeatEntries(int newBeats) {
//        mBeatEntryContainer.removeAllViews();
//        for(int i = 0; i < newBeats; i++) {
//            EditText newEditText = new EditText(this.getContext());
//            newEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
//            newEditText.setGravity(Gravity.CENTER);
//            if(i != newBeats - 1) {
//                newEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//            } else {
//                newEditText.setNextFocusForwardId(R.id.beats_in_measure_entry);
//            }
//            mBeatEntryContainer.addView(newEditText);
//        }
//
//    }

    @OnClick(R.id.save_program_button)
    public void saveProgram() {
//        mPieceOfMusic.buildCountoff();
//        Utilities.appendCountoff(mPieceOfMusic.countOffArray(), mBeats, mDownBeats);
//        mPieceOfMusic.setBeats(mBeats);
//        mPieceOfMusic.setDownBeats(mDownBeats);

        saveToDatabase();

        getFragmentManager().popBackStackImmediate();

    }

    private void gotoDataEntryFragment(String title) {
        Fragment fragment = DataEntryFragment.newInstance(title, this, mBuilder);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadNewPieces() {
        PieceOfMusic.Builder loader;
        int NUMPIECES = HardData.composers.length;
        for(int i = 0; i < NUMPIECES; i++) {
            loader = new PieceOfMusic.Builder()
                    .title(HardData.titles[i])
                    .author(HardData.composers[i])
                    .subdivision(HardData.subdivisions[i])
                    .countOffSubdivision(HardData.countoffsubdivisions[i])
                    .downBeats(HardData.lotsOdownBeats[i])
                    .defaultTempo(HardData.defaultTempos[i])
                    .baselineNoteValue(HardData.primaryRhythmValue[i]);
            if(HardData.lotsObeats[i].length == 0) {
                loader.beats(Utilities.createBeatList(HardData.lotsOdownBeats[i], HardData.subdivisions[i]));
            } else {
                loader.beats(HardData.lotsObeats[i]);
            }

            saveToDatabase(loader.build());
        }
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

                    }
                });



    }

    private void saveToDatabase() {
        saveToDatabase(mPieceOfMusic);
    }

    private void toastError() {
        Toast.makeText(this.getContext(), "You Must Enter Data to Save Data!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void returnDataList(List<DataEntry> data, PieceOfMusic.Builder builder) {
        mBuilder = builder;
        mBuilder.dataEntries(data);
        mPieceOfMusic = mBuilder.build();
    }
}
