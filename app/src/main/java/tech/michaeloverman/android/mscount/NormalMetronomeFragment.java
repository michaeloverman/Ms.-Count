package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeListener;

/**
 * Created by Michael on 2/24/2017.
 */

public class NormalMetronomeFragment extends Fragment implements MetronomeListener {


    private Metronome mMetronome;
    private boolean mMetronomeRunning;
    @BindView(R.id.normal_start_stop_fab) FloatingActionButton mStartStopFab;
    @BindView(R.id.current_tempo) TextView mTempoSetting;
    @BindView(R.id.tempo_down_button) ImageButton mTempoDownButton;
    @BindView(R.id.tempo_up_button) ImageButton mTempoUpButton;
    private float mBPM;

    private boolean mWholeNumbersSelected = true;

    private GestureDetectorCompat mDetector;


    public static Fragment newInstance() {
        return new NormalMetronomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mMetronome = new Metronome(getActivity(), this);
        mDetector = new GestureDetectorCompat(this.getContext(), new MetronomeGestureListener());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.normal_metronome_fragment, container, false);
        ButterKnife.bind(this, view);

        mTempoSetting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return true;
            }
        });

        mBPM = 123.5654f;
        updateDisplay();
        return view;
    }

    @Override
    @OnClick( R.id.normal_start_stop_fab )
    public void metronomeStartStop() {
        if(mMetronomeRunning) {
            mMetronome.stop();
            mMetronomeRunning = false;
            mStartStopFab.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mMetronomeRunning = true;
            if(mWholeNumbersSelected) {
                mMetronome.play((int)mBPM);
            } else {
                mMetronome.play(mBPM);
            }
            mStartStopFab.setImageResource(android.R.drawable.ic_media_pause);
        }
        clearSubdivisionFabs();

    }

    private void clearSubdivisionFabs() {
        if(mMetronomeRunning) {
            // TODO erase Fabs
        } else {
            // TODO bring FABS back
        }
    }

    @Override
    public void metronomeMeasureNumber(String mm) {
        // method not used
    }

    @OnClick( { R.id.whole_numbers, R.id.decimals } )
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.whole_numbers:
                if (checked) {
                    mWholeNumbersSelected = true;
                    mTempoDownButton.setVisibility(View.GONE);
                    mTempoUpButton.setVisibility(View.GONE);
                }
                break;
            case R.id.decimals:
                if (checked) {
                    mWholeNumbersSelected = false;
                    mTempoDownButton.setVisibility(View.VISIBLE);
                    mTempoUpButton.setVisibility(View.VISIBLE);
                }
                break;
        }
        updateDisplay();
    }

    @OnClick(R.id.tempo_down_button)
    public void onDownButtonClick() {
        mBPM -= 0.1f;
        updateDisplay();
    }
    @OnClick(R.id.tempo_up_button)
    public void onUpButtonClick() {
        mBPM += 0.1f;
        updateDisplay();
    }
    private void changeTempo(float tempoChange) {
        mBPM += tempoChange;
        if(mBPM > 300) mBPM = 300f;
        else if(mBPM < 20) mBPM = 20f;
        updateDisplay();
    }

    private void updateDisplay() {

        if(mWholeNumbersSelected) {
            mTempoSetting.setText((int) mBPM + "");
        } else {
            mTempoSetting.setText((float)((int)(mBPM * 10)) / 10  + "");
        }

    }

    class MetronomeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "Gestures";
        private static final float MINIMUM_Y_FOR_FAST_CHANGE = 10;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown: " + e.toString());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(Math.abs(distanceY) > MINIMUM_Y_FOR_FAST_CHANGE) {
                changeTempo(distanceY / 10);
            } else {
                changeTempo(-distanceX / 100);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            mDirection.setText("flinging: " + velocityX + ", " + velocityY);

            return true;
        }
    }
}
