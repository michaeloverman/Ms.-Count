package tech.michaeloverman.android.mscount.programmed;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import tech.michaeloverman.android.mscount.pojos.TitleKeyObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgramSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgramSelectFragment extends Fragment
        implements WorksListAdapter.WorksListAdapterOnClickHandler,
        SelectComposerFragment.ComposerCallback {
    private static final String TAG = ProgramSelectFragment.class.getSimpleName();

    @BindView(R.id.piece_list_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.other_pieces_label) TextView mWorksListTitle;
    @BindView(R.id.program_select_progress_bar) ProgressBar mProgressSpinner;

    private static String mCurrentComposer;
    private WorksListAdapter mAdapter;
    private List<TitleKeyObject> mTitlesList;

    static ProgramCallback sProgramCallback = null;


    interface ProgramCallback {
        void newPiece(PieceOfMusic piece);
    }

    public static Fragment newInstance(ProgramCallback pc, String composer) {
        sProgramCallback = pc;
        mCurrentComposer = composer;
        return new ProgramSelectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d(TAG, "onCreate() Composer: " + mCurrentComposer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.program_select_fragment, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager manager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new WorksListAdapter(this.getContext(), mTitlesList, this);
        mRecyclerView.setAdapter(mAdapter);

        getActivity().setTitle("Select a piece by...");

        if(mCurrentComposer == null) {
            selectComposer();
        } else {
            composerSelected();
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sProgramCallback = null;
    }

    @OnClick( { R.id.select_composer_button, R.id.other_pieces_label } )
    public void selectComposer() {
//        mCurrentPiece = null;
        Fragment fragment = SelectComposerFragment.newInstance(this);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClick(String pieceId) {
        FirebaseDatabase.getInstance().getReference().child("pieces").child(pieceId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        PieceOfMusic piece = dataSnapshot.getValue(PieceOfMusic.class);
                        sProgramCallback.newPiece(piece);

                        getFragmentManager().popBackStackImmediate();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    @Override
    public void newComposer(String name) {
        mCurrentComposer = name;
    }

    private void composerSelected() {
        progressSpinner(true);
        Log.d(TAG, "composerSelected() - " + mCurrentComposer);
//        mCurrentComposer = composer;
        FirebaseDatabase.getInstance().getReference().child("composers").child(mCurrentComposer)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> pieceList = dataSnapshot.getChildren();
                        ArrayList<TitleKeyObject> list = new ArrayList<>();
                        for (DataSnapshot snap : pieceList) {
                            list.add(new TitleKeyObject(snap.getKey(), snap.getValue().toString()));
                        }
                        mAdapter.setTitles(list);
                        mWorksListTitle.setText(getString(R.string.work_titles_label, mCurrentComposer));
                        progressSpinner(false);
//                        onClick(list.get(0).getKey());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void progressSpinner(boolean on) {
        if(on) {
            mWorksListTitle.setVisibility(View.INVISIBLE);
            mProgressSpinner.setVisibility(View.VISIBLE);
        } else {
            mWorksListTitle.setVisibility(View.VISIBLE);
            mProgressSpinner.setVisibility(View.INVISIBLE);
        }
    }
}
