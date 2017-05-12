/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.database;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

    private MenuItem mDeleteCancelMenuItem;

    private String mCurrentComposer;
    private WorksListAdapter mAdapter;
    private List<TitleKeyObject> mTitlesList;
    private Cursor mCursor;
    private PieceOfMusic mPieceOfMusic;
    private boolean mDeleteFlag;
    private LoadNewProgramActivity mActivity;

    public static Fragment newInstance() {
        return new PieceSelectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mActivity = (LoadNewProgramActivity) getActivity();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Timber.d("onCreateOptionsMenu");
        inflater.inflate(R.menu.delete_menu_item, menu);
        super.onCreateOptionsMenu(menu, inflater);
        mDeleteCancelMenuItem = menu.findItem(R.id.delete_program_menu_item);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("Fragment menu option");
        switch(item.getItemId()) {
            case R.id.delete_program_menu_item:
                if(!mDeleteFlag) {
                    toastDeleteInstructions();
                    prepareProgramDelete();
                } else {
                    cleanUpProgramDelete();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toastDeleteInstructions() {
        Toast.makeText(mActivity, R.string.select_to_delete, Toast.LENGTH_SHORT).show();
    }

    private void prepareProgramDelete() {
        mDeleteFlag = true;
        mDeleteCancelMenuItem.setTitle(R.string.cancel_delete);
        mDeleteCancelMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private void cleanUpProgramDelete() {
        mDeleteFlag = false;
        mDeleteCancelMenuItem.setTitle(R.string.delete_program);
        mDeleteCancelMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        mAdapter.notifyDataSetChanged();
        progressSpinner(false);
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

//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        Timber.d("onViewCreated() - transition should go now");
//        super.onViewCreated(view, savedInstanceState);
//        mActivity.supportStartPostponedEnterTransition();
//    }

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

//        android.transition.ChangeBounds changeBounds = (android.transition.ChangeBounds) TransitionInflater.from(mActivity).inflateTransition(R.transition.change_bounds);
//        fragment.setSharedElementEnterTransition(changeBounds);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
//                .addSharedElement(mComposersNameView, getString(R.string.transition_composer_name_view))
                .commit();
    }

    @Override
    public void onClick(String pieceId, final String title) {
        Timber.d("ProgramSelect onClick() pieceId: " + pieceId);
        progressSpinner(true);

        if(!mDeleteFlag) {
//            PrefUtils.saveFirebaseStatus(mActivity, mActivity.useFirebase);
//            if(!mActivity.useFirebase) {
//                pieceId = Integer.toString(sqlId);
//            }
            mActivity.setProgramResult(pieceId);
            mActivity.finish();
//            if(mActivity.useFirebase) {
//                getPieceFromFirebase(pieceId);
//            } else {
//                getPieceFromSql(position);
//            }
        } else {
            dialogDeleteConfirmation(pieceId, title);
        }
    }

    private void dialogDeleteConfirmation(final String pieceId, final String title) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setCancelable(false)
                .setTitle(R.string.delete_program_dialog_title)
                .setMessage(getString(R.string.delete_confirmation_question, title))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mActivity.useFirebase) {
                            checkFirebaseAuthorizationToDelete(pieceId, title);
                        } else {
                            deletePieceFromSql(pieceId, title);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cleanUpProgramDelete();
                    }
                });
        dialog.create().show();
    }

    private void deletePieceFromSql(String id, String title) {
        Toast.makeText(mActivity, R.string.delete_from_sql_toast, Toast.LENGTH_SHORT).show();
        int idInt;
        try {
            idInt = Integer.parseInt(id);
        } catch (NumberFormatException numE) {
            Timber.d(getString(R.string.incorrect_format_database_id));
            return;
        }
        new DeleteFromSqlTask(idInt, title).execute();
        cleanUpProgramDelete();
    }

    private void checkFirebaseAuthorizationToDelete(final String id, final String title) {

        final String userId = getFirebaseAuthId();

        FirebaseDatabase.getInstance().getReference().child("pieces").child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.d("userId: " + userId);
                        Timber.d("creatorId: " + dataSnapshot.child("creatorId"));
                        if(dataSnapshot.child("creatorId").getValue().equals(userId)) {
                            completeAuthorizedFirebaseDelete(id, title);
                        } else {
                            Toast.makeText(mActivity, R.string.not_authorized_to_delete_toast,
                                    Toast.LENGTH_SHORT).show();
                            cleanUpProgramDelete();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private String getFirebaseAuthId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    private void completeAuthorizedFirebaseDelete(final String id, final String title) {
        Toast.makeText(mActivity, R.string.delete_from_firebase_toast, Toast.LENGTH_SHORT).show();
        //TODO delete from Firebase....
        // delete from composers
        FirebaseDatabase.getInstance().getReference().child("composers").child(mCurrentComposer)
                .child(title).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Timber.d(title + " deleted from cloud database...");
            }
        });
        // delete from pieces
        FirebaseDatabase.getInstance().getReference().child("pieces").child(id).removeValue(
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        cleanUpProgramDelete();
                        selectComposer();
                    }
                }
        );
    }

    @Override
    public void newComposer(String name) {
        mCurrentComposer = name;
//        if(!mActivity.useFirebase) {
//            mActivity.getSupportLoaderManager().restartLoader(ID_PROGRAM_LOADER, null, this);
//        }
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
            mActivity.getSupportLoaderManager().initLoader(ID_PROGRAM_LOADER, null, this);
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
                message = getString(R.string.no_programs_currently_in_database);
                break;
            default:
                message = getString(R.string.unknown_error_occurred);
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

    class DeleteFromSqlTask extends AsyncTask {
        private final int _id;
        private final String mTitle;
        private final ProgressDialog dialog = new ProgressDialog(mActivity);

        private DeleteFromSqlTask(int itemId, String title) {
            _id = itemId;
            mTitle = title;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.deleting_title, mTitle));
            dialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            Uri uri = ProgramDatabaseSchema.MetProgram.CONTENT_URI;
            String whereClause = "_id=?";
            String[] args = new String[] { Integer.toString(_id) };
            mActivity.getContentResolver().delete(uri, whereClause, args);

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if(dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
