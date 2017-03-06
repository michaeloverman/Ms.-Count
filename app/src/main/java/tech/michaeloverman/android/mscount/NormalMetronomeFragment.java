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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeListener;

/**
 * Created by Michael on 2/24/2017.
 */

public class NormalMetronomeFragment extends Fragment implements MetronomeListener {
    private static final String TAG = NormalMetronomeFragment.class.getSimpleName();
    private static final int MAX_SUBDIVISIONS = Metronome.MAX_SUBDIVISIONS;

    private Metronome mMetronome;
    private boolean mMetronomeRunning;
    @BindView(R.id.normal_start_stop_fab) FloatingActionButton mStartStopFab;
    @BindView(R.id.current_tempo) TextView mTempoSetting;
    private boolean mWholeNumbersSelected = true;
    @BindView(R.id.tempo_down_button) ImageButton mTempoDownButton;
    @BindView(R.id.tempo_up_button) ImageButton mTempoUpButton;
    private float mBPM;
    private int mNumSubdivisions;
    private int[] mSubdivisionVolumes;

    @BindView(R.id.add_subdivisions_fab) FloatingActionButton mAddSubdivisionFAB;
    @BindView(R.id.expanded_add_subdivisions_fab) FloatingActionButton mExpandedAddSubFab;
    @BindView(R.id.expanded_subtract_subdivisions_fab) FloatingActionButton mSubtractSubFab;
    @BindView(R.id.subdivision_indicator1) FloatingActionButton sub1;
    @BindView(R.id.subdivision_indicator2) FloatingActionButton sub2;
    @BindView(R.id.subdivision_indicator3) FloatingActionButton sub3;
    @BindView(R.id.subdivision_indicator4) FloatingActionButton sub4;
    @BindView(R.id.subdivision_indicator5) FloatingActionButton sub5;
    FloatingActionButton[] mSubdivisionIndicators;

    Animation expandingAddFabAnim, expandingSubFabAnim;
    Animation collapsingAddFabAnim, collapsingSubFabAnim;
    Animation fadingFabAnim, unFadingFabAnim;

    private GestureDetectorCompat mDetector;
//    private GestureDetectorCompat mSubdivisionDetector[];


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

        mNumSubdivisions = 1;
        mSubdivisionVolumes = new int[MAX_SUBDIVISIONS];
        for(int i = 0; i < MAX_SUBDIVISIONS; i++) {
            mSubdivisionVolumes[i] = 10;
        }
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

        expandingAddFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.expanding_add_fab);
        expandingSubFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.expanding_sub_fab);
        collapsingAddFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.collapsing_add_fab);
        collapsingSubFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.collapsing_sub_fab);
        collapsingAddFabAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAddSubdivisionFAB.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fadingFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_fab);
        unFadingFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.unfade_fab);

        mSubdivisionIndicators = new FloatingActionButton[]{ sub1, sub2, sub3, sub4, sub5 };
        addSubdivisionVolumeChangeListeners();

        mBPM = 123.5654f;
        updateDisplay();
        return view;
    }

    @OnClick( { R.id.add_subdivisions_fab, R.id.expanded_add_subdivisions_fab } )
    public void addASubdivision() {
        if(mMetronomeRunning) return;
        if(mNumSubdivisions == 1) {
            expandFabs();
        } else if(mNumSubdivisions == MAX_SUBDIVISIONS) {
            Toast.makeText(getActivity(), R.string.too_many_subdivisions, Toast.LENGTH_SHORT).show();
            return;
        }
        mSubdivisionIndicators[mNumSubdivisions].setVisibility(View.VISIBLE);

        mNumSubdivisions++;

    }

    @OnClick(R.id.expanded_subtract_subdivisions_fab)
    public void subtractASubdivision() {
        if(mMetronomeRunning) return;
        mNumSubdivisions--;
        mSubdivisionIndicators[mNumSubdivisions].setVisibility(View.GONE);
        if(mNumSubdivisions == 1) {
            collapseFabs();
        }
    }

    private void expandFabs() {
        mSubtractSubFab.setVisibility(View.VISIBLE);
        mExpandedAddSubFab.setVisibility(View.VISIBLE);
        mAddSubdivisionFAB.setVisibility(View.INVISIBLE);
        mExpandedAddSubFab.startAnimation(expandingAddFabAnim);
        mSubtractSubFab.startAnimation(expandingSubFabAnim);
    }
    private void collapseFabs() {
        mExpandedAddSubFab.startAnimation(collapsingAddFabAnim);
        mSubtractSubFab.startAnimation(collapsingSubFabAnim);
        mSubtractSubFab.setVisibility(View.INVISIBLE);
        mExpandedAddSubFab.setVisibility(View.INVISIBLE);
//        mAddSubdivisionFAB.setVisibility(View.VISIBLE);
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
                mMetronome.play((int)mBPM, mNumSubdivisions);
            } else {
                mMetronome.play(mBPM, mNumSubdivisions);
            }
            mStartStopFab.setImageResource(android.R.drawable.ic_media_pause);
        }
        clearSubdivisionFabs();

    }

    private void clearSubdivisionFabs() {
        if(mMetronomeRunning) {
            if(mNumSubdivisions == 1) {
                mAddSubdivisionFAB.startAnimation(fadingFabAnim);
                mAddSubdivisionFAB.setClickable(false);
            } else {
                mExpandedAddSubFab.startAnimation(fadingFabAnim);
                mExpandedAddSubFab.setClickable(false);
                mSubtractSubFab.startAnimation(fadingFabAnim);
                mSubtractSubFab.setClickable(false);
            }
        } else {
            if(mNumSubdivisions == 1) {
                mAddSubdivisionFAB.startAnimation(unFadingFabAnim);
                mAddSubdivisionFAB.setClickable(true);
            } else {
                mExpandedAddSubFab.startAnimation(unFadingFabAnim);
                mExpandedAddSubFab.setClickable(true);
                mSubtractSubFab.startAnimation(unFadingFabAnim);
                mSubtractSubFab.setClickable(true);
            }
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

    private void addSubdivisionVolumeChangeListeners() {
//        mSubdivisionDetector = new GestureDetectorCompat[MAX_SUBDIVISIONS];

        for(int i = 0; i < MAX_SUBDIVISIONS; i++) {
            final int subdivisionID = i;
//            mSubdivisionDetector[subdivisionID] = new GestureDetectorCompat(this.getContext(),
//                    new SubdivisionGestureListener(subdivisionID));
            mSubdivisionIndicators[subdivisionID].setOnTouchListener(new View.OnTouchListener() {
                float startY;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getActionMasked();
                    switch(action) {
                        case (MotionEvent.ACTION_DOWN) :
                            displayVolumeSub(subdivisionID);
//                            startY = event.getY();
                            return true;
                        case (MotionEvent.ACTION_MOVE) :
                            float distanceY = startY - event.getY();
                            startY = event.getY();
                            changeSubdivisionVolume(subdivisionID, distanceY);
//                            mSubdivisionDetector[subdivisionID].onTouchEvent(event);
                            return true;
                        case (MotionEvent.ACTION_UP) :
                            updateDisplay();
                            return true;
                        default:
                            return false;
                    }

                }

            });
        }
    }
    private void displayVolumeSub(int subdiv) {
        mTempoSetting.setText("vol: " + mSubdivisionVolumes[subdiv]);
    }
    private void changeSubdivisionVolume(int id, float volumeChange) {
        mSubdivisionVolumes[id] += volumeChange;
        if(mSubdivisionVolumes[id] > 10) mSubdivisionVolumes[id] = 10;
        else if(mSubdivisionVolumes[id] < 0) mSubdivisionVolumes[id] = 0;

        mTempoSetting.setText("vol: " + mSubdivisionVolumes[id]);
        mMetronome.setClickVolumes(mSubdivisionVolumes);
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
