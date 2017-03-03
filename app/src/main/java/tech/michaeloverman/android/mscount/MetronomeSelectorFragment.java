package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tech.michaeloverman.android.mscount.programmed.PreprogrammedMetronomeFragment;

/**
 * Created by Michael on 2/24/2017.
 */

public class MetronomeSelectorFragment extends Fragment {

    private static final String TAG = MetronomeSelectorFragment.class.getSimpleName();


    @BindView(R.id.normal_metronome_button) Button mNormalMetButton;
//    @BindView(R.id.sticking_patters_metronome_button) Button mStickingPatternsButton;
    @BindView(R.id.preprogrammed_metronome_button) Button mPreprogrammedMetButton;
    private Unbinder mUnbinder;

    public static Fragment newInstance() {
        return new MetronomeSelectorFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.met_selector_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    // R.id.sticking_patters_metronome_button,
    @OnClick( { R.id.normal_metronome_button, R.id.create_new_program_button,
             R.id.preprogrammed_metronome_button })
    public void buttonClicked(View button) {
        Log.d(TAG, "buttonClicked()");
        Fragment fragment;
        switch(button.getId()) {
            case R.id.normal_metronome_button:
                fragment = NormalMetronomeFragment.newInstance();
                break;
//            case R.id.sticking_patters_metronome_button:
//                fragment = StickingPatternsFragment.newInstance();
//                break;
            case R.id.preprogrammed_metronome_button:
                fragment = PreprogrammedMetronomeFragment.newInstance();
                break;
            case R.id.create_new_program_button:
                fragment = DataEntryFragment.newInstance();
                break;
            default:
                fragment = null;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }
}
