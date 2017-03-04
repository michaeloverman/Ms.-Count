package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import tech.michaeloverman.android.mscount.pojos.HardData;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import tech.michaeloverman.android.mscount.utils.Utilities;

/**
 *
 */
public class DataEntryFragment extends Fragment {
    private static final String TAG = DataEntryFragment.class.getSimpleName();

    @BindView(R.id.composer_name_text_entry)
    EditText mComposerEntry;
    @BindView(R.id.title_text_entry) EditText mTitleEntry;
    @BindView(R.id.baseline_subdivision_entry) EditText mBaselineSubdivisionEntry;
    @BindView(R.id.countoff_subdivision_entry) EditText mCountoffSubdivisionEntry;
    @BindView(R.id.beats_in_measure_label) TextView mBeatsInMeasureLabel;
    @BindView(R.id.beats_in_measure_entry) EditText mBeatsInMeasureEntry;
//    @BindView(R.id.first_beat_subdivisions_entry) EditText mFirstBeatEntry;
    @BindView(R.id.beats_entry_container) LinearLayout mBeatEntryContainer;
    @BindView(R.id.enter_measure_button) Button mEnterMeasureButton;

    private PieceOfMusic mPieceOfMusic;
    private List<Integer> mBeats;
    private List<Integer> mDownBeats;

    public static Fragment newInstance() {
        Log.d(TAG, "newInstance()");
        return new DataEntryFragment();
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
        View view = inflater.inflate(R.layout.data_input_layout, container, false);
        ButterKnife.bind(this, view);
//        mBeatEntryContainer = (LinearLayout) this.getActivity().findViewById(R.id.beats_entry_container);
        Log.d(TAG, mBeatEntryContainer.toString());

        mBeatsInMeasureLabel.setText(getString(R.string.beats_in_current_measure, mDownBeats.size() + 1));
        mBeatsInMeasureEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() < 1) return;
                int beats = Integer.parseInt(s.toString());
                createBeatEntries(beats);
            }
        });

        return view;
    }

    @OnClick(R.id.enter_measure_button)
    public void enterMeasureClick() {
        int numBeats = Integer.parseInt(mBeatsInMeasureEntry.getText().toString());
        mDownBeats.add(numBeats);
        for(int i = 0; i < numBeats; i++) {
            mBeats.add(Integer.parseInt(((EditText) mBeatEntryContainer.getChildAt(i)).getText().toString()));
        }
        mBeatsInMeasureLabel.setText(getString(R.string.beats_in_current_measure, mDownBeats.size() + 1));

        Log.d(TAG, "Measures: " + mDownBeats.size() + ", Beats: " + mBeats.size());
        if(mDownBeats.size() == 5) {
            Log.d(TAG, mDownBeats.toString());
        }
    }

    private void createBeatEntries(int newBeats) {
        mBeatEntryContainer.removeAllViews();
        for(int i = 0; i < newBeats; i++) {
            EditText newEditText = new EditText(this.getContext());
            newEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            newEditText.setGravity(Gravity.CENTER);
            if(i != newBeats - 1) {
                newEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            } else {
                newEditText.setNextFocusForwardId(R.id.beats_in_measure_entry);
            }
            mBeatEntryContainer.addView(newEditText);
        }

    }

    @OnClick(R.id.save_program_button)
    public void saveProgram() {
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

        mPieceOfMusic.setAuthor(composer);
        mPieceOfMusic.setTitle(title);
        mPieceOfMusic.setSubdivision(Integer.parseInt(subd));
        mPieceOfMusic.setCountOffSubdivision(Integer.parseInt(countoffSubd));
        mPieceOfMusic.buildCountoff();
        Utilities.appendCountoff(mPieceOfMusic.countOffArray(), mBeats, mDownBeats);
        mPieceOfMusic.setBeats(mBeats);
        mPieceOfMusic.setDownBeats(mDownBeats);

        saveToDatabase();

        getFragmentManager().popBackStackImmediate();
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
                    .defaultTempo(HardData.defaultTempos[i]);
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
}
