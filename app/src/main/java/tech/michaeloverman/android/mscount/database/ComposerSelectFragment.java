package tech.michaeloverman.android.mscount.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import timber.log.Timber;

/**
 * This fragment gets the complete list of composer names from Firebase database, and
 * lists them. When one is selected, the name is returned to the PreprogrammedMetronomeFragment
 * for piece selection. The ComposerCallback interface is defined here, for implementation
 * by PreprogrammedMetronomeFragment, in order to communicate the selection back.
 *
 * Created by Michael on 2/26/2017.
 */

public class ComposerSelectFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ComposerSelectFragment.class.getSimpleName();
    private static final int COMPOSER_LOADER_ID = 99;
    private static final int NO_DATA_ERROR_CODE = 42;

    @BindView(R.id.composer_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.composer_select_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.empty_data_view) TextView mErrorView;
    private ComposerListAdapter mAdapter;
    private static Cursor mCursor;
    private LoadNewProgramActivity mActivity;

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
//        mCursor = c;
        sComposerCallback = cc;
        return new ComposerSelectFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate()");

        mActivity = (LoadNewProgramActivity) getActivity();
        Timber.d("useFirebase = " + mActivity.useFirebase);

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
//        inflater.inflate(R.menu.metadata_entry_menu, menu);
//        menu.removeItem(R.id.create_new_program_option);
//        MenuItem item = menu.findItem(R.id.firebase_local_database);
//        Log.d(TAG, "useFirebase = " + ((ProgrammedMetronomeActivity)getActivity()).useFirebase);
//
//        item.setTitle(((ProgrammedMetronomeActivity)getActivity()).useFirebase ?
//                R.string.use_local_database : R.string.use_cloud_database);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.select_composer_layout, container, false);
        ButterKnife.bind(this, view);

        if(mCursor != null) {
            mActivity.getSupportLoaderManager().restartLoader(COMPOSER_LOADER_ID, null, this);
        }

        mActivity.setTitle(getString(R.string.select_a_composer));

        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
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
        if(mActivity.useFirebase) {
            FirebaseDatabase.getInstance().getReference().child("composers")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                            List<String> list = new ArrayList<>();
                            for (DataSnapshot snap : iterable) {
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
        } else {
            mActivity.getSupportLoaderManager().initLoader(COMPOSER_LOADER_ID, null, this);
        }

    }

    public void updateData() {
        Log.d(TAG, "updateData()");
        loadComposers();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        String[] projection = new String[] {ProgramDatabaseSchema.MetProgram.COLUMN_COMPOSER };

        switch(id) {
            case COMPOSER_LOADER_ID:
                Uri queryUri = ProgramDatabaseSchema.MetProgram.CONTENT_URI;
                String sortOrder = ProgramDatabaseSchema.MetProgram.COLUMN_COMPOSER + " ASC";

                return new CursorLoader(getContext(),
                        queryUri,
//                        projection,
                        null,
                        null,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        progressSpinner(false);
        if(data == null || data.getCount() == 0) {
            mErrorView.setVisibility(View.VISIBLE);
            updateEmptyView(NO_DATA_ERROR_CODE);
        } else {
            mCursor = data;
            mErrorView.setVisibility(View.GONE);
            mAdapter.newCursor(mCursor);
        }
    }

    private void updateEmptyView(int code) {
        Log.d(TAG, "updateEmptyView(code = " + code);
        String message;
        switch(code) {
            case NO_DATA_ERROR_CODE:
                message = "No composers currently in database.";
                break;
            default:
                message = "Unknown error occurred...";
        }
        mErrorView.setText(message);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.newCursor(null);
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

        public void newCursor(Cursor data) {
            List<String> list = new ArrayList<>();
            if(data == null) {
                composers = list;
                return;
            }

            try {
                data.moveToFirst();
                while (!data.isAfterLast()) {
                    list.add(data.getString(ProgramDatabaseSchema.MetProgram.POSITION_COMPOSER));
                    data.moveToNext();
                }
            } finally {
                data.close();
            }

            setComposers(list);
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
//                sComposerCallback.newComposer(name);
                mActivity.mCurrentComposer = name;

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