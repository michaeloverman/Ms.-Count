package tech.michaeloverman.android.mscount.database;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PieceSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PieceSelectFragment extends DatabaseAccessFragment
        implements WorksListAdapter.WorksListAdapterOnClickHandler,
        ComposerSelectFragment.ComposerCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_PROGRAM_LOADER = 432;
    private static final int NO_DATA_ERROR_CODE = 41;

    @BindView(R.id.piece_list_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.composers_name_label) TextView mComposersNameView;
    @BindView(R.id.error_view) TextView mErrorView;
    @BindView(R.id.program_select_progress_bar) ProgressBar mProgressSpinner;
    @BindView(R.id.select_composer_button) Button mSelectComposerButton;

    private String mCurrentComposer;
    private WorksListAdapter mAdapter;
    private List<TitleKeyObject> mTitlesList;
    private Cursor mCursor;
    private PieceOfMusic mPieceOfMusic;

    private LoadNewProgramActivity mActivity;


//
//    public interface ProgramCallback {
//        void newPiece(PieceOfMusic piece);
//    }

//    public static Fragment newInstance(String composer) {
////        mCursor = c;
////        sProgramCallback = pc;
////        Timber.d("Just set sPragramCallback : " + sProgramCallback);
//        mCurrentComposer = composer;
//    }
    public static Fragment newInstance() {
        return new PieceSelectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
//        Timber.d("useFirebase = " + mActivity.useFirebase);

        mActivity = (LoadNewProgramActivity) getActivity();


//        mActivity.getSupportLoaderManager().initLoader(ID_PROGRAM_LOADER, null, this);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Timber.d("onCreateOptionsMenu");
//        menu.removeItem(R.id.create_new_program_option);
//        MenuItem item = menu.findItem(R.id.firebase_local_database);
//        Log.d(TAG, "useFirebase =Timber.d(rammedMetronomeActivity)getActivity()).useFirebase);
//        item.setTitle(((ProgrammedMetronomeActivity)getActivity()).useFirebase ?
//                R.string.use_local_database : R.string.use_cloud_database);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.program_select_fragment, container, false);
        ButterKnife.bind(this, view);

        Timber.d("onCreateView useFirebase: " + mActivity.useFirebase);

//        if(mCursor != null) {
//            mActivity.getSupportLoaderManager().restartLoader(WORKS_LOADER_ID, null, this);
//        }

        LinearLayoutManager manager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new WorksListAdapter(this.getContext(), mTitlesList, this);
        mRecyclerView.setAdapter(mAdapter);

        mCurrentComposer = mActivity.mCurrentComposer;
        Timber.d("onCreate() Composer: " + mCurrentComposer);

        if(mActivity.useFirebase) {
            mActivity.setTitle(getString(R.string.select_piece_by));
        } else {
            mActivity.setTitle(getString(R.string.select_a_piece));
            makeComposerRelatedViewsInvisible();
        }

        if (mActivity.useFirebase && mCurrentComposer == null) {
            selectComposer();
        } else {
            composerSelected();
        }

        Timber.d("Returning completed view....!!!");
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.d("ProgramSelectFragment detaching - this is where sProgramCallback may be nullified...");
//        sProgramCallback = null;
    }

    @OnClick( { R.id.select_composer_button, R.id.composers_name_label} )
    public void selectComposer() {
//        mCurrentPiece = null;
        Fragment fragment = ComposerSelectFragment.newInstance(this);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClick(int position, final String pieceId) {
        Timber.d("ProgramSelect onClick() pieceId: " + pieceId);
        progressSpinner(true);
        if(mActivity.useFirebase) {
            FirebaseDatabase.getInstance().getReference().child("pieces").child(pieceId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mPieceOfMusic = dataSnapshot.getValue(PieceOfMusic.class);
                            mPieceOfMusic.setFirebaseId(pieceId);
                            Timber.d("Just loaded piece from Firebase. Id: " + pieceId);
                            mActivity.setProgramResult(mPieceOfMusic);
                            mActivity.finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), "A database error occurred. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            mCursor.moveToPosition(position);
            PieceOfMusic.Builder builder = new PieceOfMusic.Builder()
                    .author(mCursor.getString(ProgramDatabaseSchema.MetProgram.POSITION_COMPOSER))
                    .title(mCursor.getString(ProgramDatabaseSchema.MetProgram.POSITION_TITLE))
                    .subdivision(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_PRIMANY_SUBDIVISIONS))
                    .countOffSubdivision(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_COUNOFF_SUBDIVISIONS))
                    .defaultTempo(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_DEFAULT_TEMPO))
                    .baselineNoteValue(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_DEFAULT_RHYTHM))
                    .tempoMultiplier(mCursor.getDouble(ProgramDatabaseSchema.MetProgram.POSITION_TEMPO_MULTIPLIER))
                    .firstMeasureNumber(mCursor.getInt(ProgramDatabaseSchema.MetProgram.POSITION_MEASURE_COUNTE_OFFSET))
                    .dataEntries(mCursor.getString(ProgramDatabaseSchema.MetProgram.POSITION_DATA_ARRAY))
                    .firebaseId(mCursor.getString(ProgramDatabaseSchema.MetProgram.POSITION_FIREBASE_ID));
            mCursor.close();

            mActivity.setProgramResult(builder.build());
            mActivity.finish();
        }

//        getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void newComposer(String name) {
        mCurrentComposer = name;
        if(!mActivity.useFirebase) {
            mActivity.getSupportLoaderManager().restartLoader(ID_PROGRAM_LOADER, null, this);
        }
    }

    private void composerSelected() {
        progressSpinner(true);
        Timber.d("composerSelected() - " + mCurrentComposer);

        if(mActivity.useFirebase) {
            Timber.d("Checking Firebase for composer " + mCurrentComposer);
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
                            mComposersNameView.setText(mCurrentComposer);
                            progressSpinner(false);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else {
            Timber.d("Checking SQL for pieces ");
            getActivity().getSupportLoaderManager().initLoader(ID_PROGRAM_LOADER, null, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch(id) {
            case ID_PROGRAM_LOADER:
                Uri queryUri = ProgramDatabaseSchema.MetProgram.CONTENT_URI;
                Timber.d("onCreateLoader() queryUri: " + queryUri);
                String sortOrder = ProgramDatabaseSchema.MetProgram.COLUMN_TITLE + " ASC";

                return new CursorLoader(mActivity,
                        queryUri,
//                        projection,
                        null,
                        null,
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Timber.d("onLoadFinished - cursor data ready");
        progressSpinner(false);
        if(data == null) {
            Timber.d("data == null");
            mErrorView.setVisibility(View.VISIBLE);
            updateEmptyProgramList(NO_DATA_ERROR_CODE);
        } else if (data.getCount() == 0) {
            Timber.d("data.getCount() == 0");
            mErrorView.setVisibility(View.VISIBLE);
            updateEmptyProgramList(NO_DATA_ERROR_CODE);
        } else {
            mCursor = data;
            mErrorView.setVisibility(View.GONE);
            mAdapter.newCursor(mCursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.newCursor(null);
    }

    @Override
    public void updateData() {
        Timber.d("updateData()");
        if(mActivity.useFirebase) {
            makeComposerRelatedViewsVisible();
            selectComposer();
        } else {
            mCurrentComposer = null;
            makeComposerRelatedViewsInvisible();
            composerSelected();
        }
    }

    private void makeComposerRelatedViewsVisible() {
        Timber.d("showing views");
        mSelectComposerButton.setVisibility(View.VISIBLE);
        mComposersNameView.setText(mCurrentComposer);
        mActivity.setTitle(getString(R.string.select_piece_by));
    }
    private void makeComposerRelatedViewsInvisible() {
        Timber.d("removing views");
        mComposersNameView.setText(R.string.local_database_label);
        mSelectComposerButton.setVisibility(View.GONE);
        mActivity.setTitle(getString(R.string.select_a_piece));
    }

    private void updateEmptyProgramList(int code) {
        String message;
        switch(code) {
            case NO_DATA_ERROR_CODE:
                message = "No programs currently in database.";
                break;
            default:
                message = "Unknown error occurred...";
        }
        mErrorView.setText(message);
    }

    private void progressSpinner(boolean on) {
        if(on) {
            mComposersNameView.setVisibility(View.INVISIBLE);
            mProgressSpinner.setVisibility(View.VISIBLE);
        } else {
            mComposersNameView.setVisibility(View.VISIBLE);
            mProgressSpinner.setVisibility(View.INVISIBLE);
        }
    }
}
