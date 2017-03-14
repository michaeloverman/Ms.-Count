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
import android.widget.TextView;

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

    private static final int MAX_TEMPO_BPM = Metronome.MAX_TEMPO;
    private static final int MIN_TEMPO_BPM = Metronome.MIN_TEMPO;

    private Metronome mMetronome;
    private boolean mMetronomeRunning;
    private int mBPM;

    @BindView(R.id.oddmeter_start_stop_fab)
    FloatingActionButton mStartStopFab;
    @BindView(R.id.oddmeter_tempo_view)
    TextView mTempoSetting;


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

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.oddmeter_metronome_layout, container, false);
        ButterKnife.bind(this, view);

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

        return view;
    }

    @OnClick( { R.id.two_subs_button, R.id.three_subs_button, R.id.four_subs_button } )
    public void addSubdivision(TextView button) {
        int beat = Integer.parseInt(button.getText().toString());
        switch (beat) {
            case 2:

                break;
            case 3:

                break;
            case 4:

                break;
            default:
        }
        Log.d(TAG, "Subdivision" + beat + " pressed");
    }

    @OnClick(R.id.other_subs_button)
    public void addUnusualSubdivision() {
        Log.d(TAG, "add a different length of subdivision");
    }

    @OnClick(R.id.delete_button)
    public void deleteSubdivision() {
        Log.d(TAG, "remove a subdivision");
    }

    @Override
    public void onDestroy() {
        // TODO store settings in SharedPrefers

        super.onDestroy();
    }

    @Override
    public void metronomeStartStop() {

    }

    @Override
    public void metronomeMeasureNumber(String mm) {
        // not used in this metronome
    }

    private void changeTempo(float tempoChange) {
        mBPM += tempoChange;
        if(mBPM > MAX_TEMPO_BPM) mBPM = MAX_TEMPO_BPM;
        else if(mBPM < MIN_TEMPO_BPM) mBPM = MIN_TEMPO_BPM;
        updateDisplay();
    }

    private void updateDisplay() {
            mTempoSetting.setText(mBPM + "");
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
