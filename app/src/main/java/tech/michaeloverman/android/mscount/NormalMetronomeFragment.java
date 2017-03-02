package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.MetronomeListener;

/**
 * Created by Michael on 2/24/2017.
 */

public class NormalMetronomeFragment extends Fragment implements MetronomeListener {


    private Metronome mMetronome;
    private boolean mMetronomeRunning;


    public static Fragment newInstance() {
        return new NormalMetronomeFragment();
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
        View view = inflater.inflate(R.layout.normal_metronome_fragment, container, false);

        return view;
    }

    @Override
    public void metronomeStartStop() {

    }


    @Override
    public void metronomeMeasureNumber(String mm) {
        // method not used
    }
}
