package tech.michaeloverman.android.mscount.programmed;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.michaeloverman.android.mscount.R;

/**
 * This fragment gets the complete list of composer names from Firebase database, and
 * lists them. When one is selected, the name is returned to the PreprogrammedMetronomeFragment
 * for piece selection. The ComposerCallback interface is defined here, for implementation
 * by PreprogrammedMetronomeFragment, in order to communicate the selection back.
 *
 * Created by Michael on 2/26/2017.
 */

public class SelectComposerFragment extends Fragment {
    private static final String TAG = SelectComposerFragment.class.getSimpleName();

    @BindView(R.id.composer_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.composer_select_progress_bar) ProgressBar mProgressBar;

    private ComposerListAdapter mAdapter;

    /** Listener for returning selection to PreprogrammedMetronomeFragment */
    static ComposerCallback sComposerCallback = null;

    /** Interface for PreprogrammedMetronomeFragment to implement in order to
     *  listen for selection.
     */
    interface ComposerCallback {
        void newComposer(String name);
    }

    /**
     * Instance constructor accepts Communicator reference for callback.
     * @param cc
     * @return
     */
    public static Fragment newInstance(ComposerCallback cc) {
        Log.d(TAG, "newInstance()");
        sComposerCallback = cc;
        return new SelectComposerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.metadata_entry_menu, menu);
        menu.removeItem(R.id.create_new_program_option);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.select_composer_layout, container, false);
        ButterKnife.bind(this, view);

        getActivity().setTitle(getString(R.string.select_a_composer));

        LinearLayoutManager manager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new ComposerListAdapter(this.getContext());
        mRecyclerView.setAdapter(mAdapter);

        loadComposers();

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sComposerCallback = null;
    }

    /**
     * Contact Firebase Database, get all the composer's names, attach to adapter for
     * recycler viewing
     */
    private void loadComposers() {
        Log.d(TAG, "loadComposers()");
        progressSpinner(true);
        FirebaseDatabase.getInstance().getReference().child("composers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                        List<String> list = new ArrayList<>();
                        for(DataSnapshot snap : iterable) {
                            list.add(snap.getKey());
                        }
                        Collections.sort(list);
                        mAdapter.setComposers(list);
                        progressSpinner(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }

    /**
     * Adapter class to handle recycler view, listing composer names
     */
    class ComposerListAdapter extends RecyclerView.Adapter<ComposerListAdapter.ComposerViewHolder> {

        Context mContext;
        private List<String> composers;

        public ComposerListAdapter(Context context) {
            Log.d(TAG, "ComposerListAdapter constructor");
            mContext = context;
        }
        @Override
        public ComposerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder");
            View item =LayoutInflater.from(mContext).inflate(R.layout.list_item_composer, parent, false);
            return new ComposerViewHolder(item);
        }

        @Override
        public void onBindViewHolder(ComposerViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder()");
            holder.composerName.setText(composers.get(position));
        }

        @Override
        public int getItemCount() {
            return composers == null ? 0 : composers.size();
        }

        public void setComposers(List<String> list) {
            composers = list;
            notifyDataSetChanged();
        }

        class ComposerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.composer_name_tv)
            TextView composerName;

            public ComposerViewHolder(View itemView) {
                super(itemView);
                Log.d(TAG, "ComposerViewHolder constructor");
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                String name = composers.get(position);

                // send selected composer name to PreprogrammedMetronomeFragment via callback
                sComposerCallback.newComposer(name);

                // close this fragment and return
                getFragmentManager().popBackStackImmediate();
            }
        }
    }

    private void progressSpinner(boolean on) {
        if(on) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
