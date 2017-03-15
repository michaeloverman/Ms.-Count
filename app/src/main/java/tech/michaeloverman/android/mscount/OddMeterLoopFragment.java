package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeListener;

/**
 * Created by Michael on 3/14/2017.
 */

public class OddMeterLoopFragment extends Fragment implements MetronomeListener {
    private static final String TAG = OddMeterLoopFragment.class.getSimpleName();

    private static final float MAX_TEMPO_BPM = Metronome.MAX_TEMPO;
    private static final float MIN_TEMPO_BPM = Metronome.MIN_TEMPO;
    private static final float SUBDIVISION_DISPLAY_SIZE = 40;
    private static final int MARGIN = 8;

    private Metronome mMetronome;
    private boolean mMetronomeRunning;
    private float mBPM;
    private int mMultiplier;

    @BindView(R.id.oddmeter_start_stop_fab) FloatingActionButton mStartStopFab;
    @BindView(R.id.oddmeter_tempo_view) TextView mTempoSetting;
    @BindView(R.id.extra_subdivision_buttons) LinearLayout mOtherButtons;

    private List<Integer> mSubdivisionsList;
    private List<View> mSubdivisionViews;
    @BindView(R.id.subdivision_layout) LinearLayout mSubdivisionLayout;
//    private LinearLayout mSubdivisionLayout;

    private GestureDetectorCompat mDetector;

    public static Fragment newInstance() {
        return new OddMeterLoopFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mMetronome = new Metronome(getActivity(), this);
        mDetector = new GestureDetectorCompat(this.getContext(), new MetronomeGestureListener());

        mSubdivisionsList = new ArrayList<>();
        mSubdivisionViews = new ArrayList<>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.oddmeter_metronome_layout, container, false);
        ButterKnife.bind(this, view);
//        mSubdivisionLayout = (LinearLayout) this.getActivity().findViewById(R.id.subdivision_layout);
        // use the "naked" listener to catch ACTION_UP (release) for resetting tempo
        // otherwise defer to GestureDetector to handle scrolling
        mTempoSetting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                switch(action) {
                    case MotionEvent.ACTION_UP:
                        if(mMetronomeRunning) {
                            // stop the met
                            metronomeStartStop();
                            // restart at new tempo
                            metronomeStartStop();
                        }
                        break;
                    default:
                        mDetector.onTouchEvent(event);
                }
                return true;
            }
        });

        mBPM = 120f; //TODO change this to read from past shared prefs
        mMultiplier = 2;

        return view;
    }

    @OnClick( { R.id.one_subs_button, R.id.two_subs_button, R.id.three_subs_button,
            R.id.four_subs_button, R.id.five_subs_button, R.id.six_subs_button,
            R.id.seven_subs_button, R.id.eight_subs_button, R.id.nine_subs_button,
            R.id.ten_subs_button,} )
    public void addSubdivision(TextView button) {
        boolean wasRunning = false;
        if(mMetronomeRunning) {
            metronomeStartStop();
            wasRunning = true;
        }
        int beat = Integer.parseInt(button.getText().toString());
        mSubdivisionsList.add(beat);
        mSubdivisionViews.add(getNewSubdivisionView(beat));

        if(mOtherButtons.isShown()) mOtherButtons.setVisibility(View.GONE);

        updateSubdivisionDisplay();

        if(wasRunning) metronomeStartStop();
    }

    @OnClick(R.id.other_subs_button)
    public void addUnusualSubdivision() {
        Log.d(TAG, "add a different length of subdivision");
        mOtherButtons.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.delete_button)
    public void deleteSubdivision() {
        Log.d(TAG, "remove a subdivision");
        if(mSubdivisionsList.size() == 0) return;
        boolean wasRunning = false;
        if(mMetronomeRunning) {
            wasRunning = true;
            metronomeStartStop();
        }
        mSubdivisionsList.remove(mSubdivisionsList.size() - 1);
        mSubdivisionLayout.removeView(mSubdivisionViews.get(mSubdivisionViews.size() - 1));
        mSubdivisionViews.remove(mSubdivisionViews.size() - 1);

        if(wasRunning && mSubdivisionsList.size() > 0) metronomeStartStop();
    }

    @Override
    public void onDestroy() {
        // TODO store settings in SharedPrefers

        super.onDestroy();
    }

    @Override
    @OnClick(R.id.oddmeter_start_stop_fab)
    public void metronomeStartStop() {
        Log.d(TAG, "Loop length: " + mSubdivisionsList.size() + ", view size: " + mSubdivisionViews.size());
        if(mMetronomeRunning) {
            mMetronome.stop();
            mMetronomeRunning = false;
            mStartStopFab.setImageResource(android.R.drawable.ic_media_play);
        } else {
            if(mSubdivisionsList.size() == 0) {
                Toast.makeText(getContext(), "You must enter subdivisions to click subdivisions...", Toast.LENGTH_SHORT).show();
                return;
            }
            mMetronomeRunning = true;
            mMetronome.play(((int) mBPM * mMultiplier), mSubdivisionsList);
            mStartStopFab.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public void metronomeMeasureNumber(String mm) {
        // not used in this metronome
    }

    private void changeTempo(float tempoChange) {
        mBPM += tempoChange;
        if(mBPM > MAX_TEMPO_BPM) mBPM = MAX_TEMPO_BPM;
        else if(mBPM < MIN_TEMPO_BPM) mBPM = MIN_TEMPO_BPM;
        updateTempoDisplay();
    }

    private void updateTempoDisplay() {
        mTempoSetting.setText((int) mBPM + "");
    }

    private void updateSubdivisionDisplay() {

    }

    private View getNewSubdivisionView(int value) {
        TextView view = new TextView(getContext());
        view.setText(value + "");
        view.setTextSize(SUBDIVISION_DISPLAY_SIZE);
        view.setBackground(getResources().getDrawable(R.drawable.roundcorner_parchment));
        view.setPadding(MARGIN, 0, MARGIN, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

        view.setLayoutParams(params);
        mSubdivisionLayout.addView(view);
        return view;
    }

    class MetronomeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "Gestures";
        private static final float MINIMUM_Y_FOR_FAST_CHANGE = 10;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(Math.abs(distanceY) > MINIMUM_Y_FOR_FAST_CHANGE) {
                changeTempo(distanceY / 10);
            } else {
                changeTempo(-distanceX / 100);
            }
            return true;
        }
    }
}
