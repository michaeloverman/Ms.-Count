package tech.michaeloverman.android.mscount.programmed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.database.LoadNewProgramActivity;
import tech.michaeloverman.android.mscount.dataentry.MetaDataEntryFragment;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeListener;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Michael on 2/24/2017.
 */

public class ProgrammedMetronomeFragment extends Fragment
        implements MetronomeListener {

    private static final boolean UP = true;
    private static final boolean DOWN = false;
    private static final int MAXIMUM_TEMPO = 350;
    private static final int MINIMUM_TEMPO = 1;
    private static final String CURRENT_PIECE_KEY = "current_piece_key";
    private static final String CURRENT_TEMPO_KEY = "current_tempo_key";
    private static final String CURRENT_COMPOSER_KEY = "current_composer_key";
    private static final String PREF_CURRENT_TEMPO = "programmable_tempo_key";
    private static final String PREF_PIECE_KEY = "programmable_piece_id";
    private static final int REQUEST_NEW_PROGRAM = 44;
    public static final String EXTRA_COMPOSER_NAME = "composer_name_extra";

    private PieceOfMusic mCurrentPiece;
    private String mCurrentPieceKey;
    private int mCurrentTempo;
    private String mCurrentComposer;
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
            mCurrentTempo = savedInstanceState.getInt(CURRENT_TEMPO_KEY);
//            mCurrentPiece = savedInstanceState.getString(CURRENT_PIECE_KEY);
            mCurrentComposer = savedInstanceState.getString(CURRENT_COMPOSER_KEY);
            Timber.d("savedInstanceState retrieved: composer: " + mCurrentComposer);
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            mCurrentPieceKey = prefs.getString(PREF_PIECE_KEY, null);
            mCurrentTempo = prefs.getInt(PREF_CURRENT_TEMPO, 120);
//            if(mCurrentPieceKey != null) {
//                newPiece(mCurrentPieceKey);
//            }
        }

//        mMetronome = new Metronome(getActivity(), this);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Timber.d("onCreateOptionsMenu");
        inflater.inflate(R.menu.programmed_menu, menu);
//        Timber.d("useFirebase = " + mActivity.useFirebase);

//        MenuItem item = menu.findItem(R.id.firebase_local_database);
//        item.setTitle(((ProgrammedMetronomeActivity)getActivity()).useFirebase ?
//                R.string.use_local_database : R.string.use_cloud_database);
    }

    @Override
    public void onPause() {
        if(mMetronomeRunning) metronomeStartStop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        SharedPreferences.Editor prefs = PreferenceManager
                .getDefaultSharedPreferences(getContext()).edit();
        prefs.putString(PREF_PIECE_KEY, mCurrentPieceKey);
        prefs.putInt(PREF_CURRENT_TEMPO, mCurrentTempo);
        prefs.commit();

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
//        Fragment fragment = ProgramSelectFragment.newInstance(this, mCurrentComposer,
//                mActivity, mActivity.mCursor);
//        FragmentTransaction trans = getFragmentManager().beginTransaction();
//        trans.replace(R.id.fragment_container, fragment);
//        trans.addToBackStack(null);
//        trans.commit();
        Intent intent = new Intent(mActivity, LoadNewProgramActivity.class);
        intent.putExtra(EXTRA_COMPOSER_NAME, mCurrentComposer);
        startActivityForResult(intent, REQUEST_NEW_PROGRAM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) {
            Toast.makeText(mActivity, "Problem with return result", Toast.LENGTH_SHORT).show();
            return;
        }
        switch(requestCode) {
            case REQUEST_NEW_PROGRAM:
                mCurrentPiece = (PieceOfMusic) data.getSerializableExtra(
                        LoadNewProgramActivity.EXTRA_NEW_PROGRAM);
                updateGUI();
                break;
            default:
        }
    }

    @OnClick(R.id.start_stop_fab)
    public void metronomeStartStop() {
        if(mMetronomeRunning) {
            Timber.d("metronomeStop() " + mCurrentComposer);
            mMetronome.stop();
            mMetronomeRunning = false;
            mStartStopButton.setImageResource(android.R.drawable.ic_media_play);
            mCurrentMeasureNumber.setText("--");
        } else {
            Timber.d("metronomeStart() " + mCurrentPiece.getTitle());
            if(mCurrentPiece == null) {
                Toast.makeText(this.getContext(), R.string.no_program_selected_instruction_toast,
                        Toast.LENGTH_SHORT).show();
                return;
            }
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
            outState.putString(CURRENT_PIECE_KEY, mCurrentPiece.getTitle());
            outState.putInt(CURRENT_TEMPO_KEY, mCurrentTempo);
            outState.putString(CURRENT_COMPOSER_KEY, mCurrentComposer);
        }
        super.onSaveInstanceState(outState);

    }

    private void updateGUI() {
        // TODO set TitleViews etc
        Timber.d("updateGUI() " + mCurrentPiece.getAuthor() + ": " + mCurrentPiece.getTitle());
        mTVCurrentPiece.setText(mCurrentPiece.getTitle());
        mTVCurrentComposer.setText(mCurrentComposer);
        mBeatLengthImage.setImageResource(getNoteImageResource
                (mCurrentPiece.getBaselineNoteValue()));
        updateTempoView();
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

//    @Override
//    public void newPiece(PieceOfMusic piece) {
////        if(((ProgrammedMetronomeActivity) getActivity()).useFirebase) {
////            mCurrentPieceKey = pieceId;
////
////            FirebaseDatabase.getInstance().getReference().child("pieces").child(pieceId)
////                    .addListenerForSingleValueEvent(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            mCurrentPiece = dataSnapshot.getValue(PieceOfMusic.class);
////                            updateVariables();
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////                            Toast.makeText(getContext(), "A database error occurred. Please try again.",
////                                    Toast.LENGTH_SHORT).show();
////                        }
////                    });
////        } else {
////
////        }
//        mCurrentPiece = piece;
//    }

    private void updateVariables() {
        if(mCurrentPiece == null) {
            selectNewProgram();
            return;
        }

        Timber.d("newPiece() " + mCurrentPiece.getTitle());
//        Timber.d("piece COsub: " + mCurrentPiece.getCountOffSubdivision() + "; mCurrentPiece COsub: " + mCurrentPiece.getCountOffSubdivision());

        mCurrentComposer = mCurrentPiece.getAuthor();
        if(mCurrentPiece.getDefaultTempo() != 0) {
            mCurrentTempo = mCurrentPiece.getDefaultTempo();
        }



//        Timber.d("Subd: " + mCurrentPiece.getSubdivision() + "; CountoffSubs: " + mCurrentPiece.getCountOffSubdivision());

        updateGUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_new_program_option:
                openProgramEditor();
                return true;
//            case R.id.edit_existing_program_option:
//                loadProgram();
//                return true;
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

//    private void loadProgram() {
//        Fragment fragment = ProgramSelectFragment.newInstance(this, null);
//        FragmentTransaction trans = getFragmentManager().beginTransaction();
//        trans.replace(R.id.fragment_container, fragment);
//        trans.addToBackStack(null);
//        trans.commit();
//    }
}