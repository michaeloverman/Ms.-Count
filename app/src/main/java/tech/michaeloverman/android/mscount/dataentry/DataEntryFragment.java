/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.dataentry;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.pojos.DataEntry;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import timber.log.Timber;

/**
 * This fragment handles the UI and the logic for entering program data in the Programmed
 * Metronome activity.
 *
 * Created by Michael on 3/12/2017.
 */

public class DataEntryFragment extends Fragment {

    private final static boolean BARLINE = true;
    private final static boolean BEAT = false;


    private PieceOfMusic.Builder mBuilder;

    @BindView(R.id.data_title_view) TextView mTitleView;
    @BindView(R.id.entered_data_recycler_view) RecyclerView mEnteredDataRecycler;
    @BindView(R.id.help_overlay) FrameLayout mInstructionsLayout;


    private String mTitle;
    private List<DataEntry> mDataList;
    private float mDataMultipliedBy = 1.0f;
    private DataMultipliedListener mDataMultipliedListener;
    private int mMeasureNumber;
    private DataListAdapter mAdapter;
    private boolean mDataItemSelected;

    public interface DataMultipliedListener {
        void dataValuesMultipliedBy(float multiplier);
    }

    public static Fragment newInstance(String title, PieceOfMusic.Builder builder,
                                       DataMultipliedListener dml) {
        return newInstance(title, builder, new ArrayList<DataEntry>(), dml);
    }

    public static Fragment newInstance(String title, PieceOfMusic.Builder builder,
                                       List<DataEntry> data, DataMultipliedListener dml) {

        DataEntryFragment fragment = new DataEntryFragment();
        fragment.mTitle = title;
        fragment.mBuilder = builder;
        fragment.mDataList = data;
        fragment.mDataMultipliedListener = dml;

        return fragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mMeasureNumber = 0;

        // set up measure numbers - add opening barline, if necessary
        if(mDataList.size() > 0) {
            mMeasureNumber = mDataList.get(mDataList.size() - 1).getData();
        } else {
            mDataList.add(new DataEntry(++mMeasureNumber, BARLINE));
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_input_layout, container, false);
        ButterKnife.bind(this, view);

        mTitleView.setText(mTitle);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        mEnteredDataRecycler.setLayoutManager(manager);
        mAdapter = new DataListAdapter();
        mEnteredDataRecycler.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        mEnteredDataRecycler.scrollToPosition(mDataList.size() - 1);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.data_entry_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.double_data_values:
                multiplyValues(2);
                break;
            case R.id.halve_data_values:
                divideValues(2);
                break;
            case R.id.triple_data_values:
                multiplyValues(3);
                break;
            case R.id.third_data_values:
                divideValues(3);
                break;
            case R.id.help_menu_item:
                makeInstructionsVisible();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
//        mBuilder.tempoMultiplier(mDataMultipliedBy);
        return true;
    }

    private void multiplyValues(int multiplier) {
        for (DataEntry entry: mDataList) {
            if(!entry.isBarline()) {
                entry.setData(entry.getData() * multiplier);
            }
        }
        mAdapter.notifyDataSetChanged();
        mDataMultipliedBy *= (float) multiplier;
    }

    private void divideValues(int divider) {
        List<DataEntry> tempList = new ArrayList<>();
        for (DataEntry entry : mDataList) {
            if(!entry.isBarline() && entry.getData() % divider != 0) {
                cantDivideError();
                return;
            } else {
                tempList.add(new DataEntry(entry.getData() / divider, entry.isBarline()));
            }
        }
        mDataList = tempList;
        mAdapter.notifyDataSetChanged();
        mDataMultipliedBy /= divider;
    }

    private void cantDivideError() {
        Toast.makeText(getContext(), R.string.cant_evenly_divide, Toast.LENGTH_SHORT).show();
    }

    private void makeInstructionsVisible() {
        mInstructionsLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.help_cancel_button)
    public void instructionsCancelled() {
        mInstructionsLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * Delete the last item, or the selected item
     */
    @OnClick(R.id.data_delete_button)
    public void delete() {
        boolean barline = false;
        boolean resetMeasureNumbers = false;
        int itemToDelete = mDataItemSelected ? mAdapter.selectedPosition : mDataList.size() - 1;

        if(mDataList.get(itemToDelete).isBarline()) {
            barline = true;
            resetMeasureNumbers = mDataItemSelected;
        }

        mDataList.remove(itemToDelete);
        if(mAdapter.selectedPosition >= mDataList.size()) {
            mAdapter.selectedPosition = -1;
            mDataItemSelected = false;
        }

        if(barline) mMeasureNumber--;
        if(resetMeasureNumbers) resetMeasureNumbers();

        mAdapter.notifyDataSetChanged();
    }

    private void resetMeasureNumbers() {
        mMeasureNumber = 0;
        for(int i = 0; i < mDataList.size(); i++) {
            if(mDataList.get(i).isBarline()) {
                mDataList.get(i).setData(++mMeasureNumber);
            }
        }
    }

    /**
     * returns data to previous fragment, which allows for saving to Firebase, at least at this
     * point it does...
     */
    @OnClick(R.id.data_save_button)
    public void saveData() {
        Timber.d("saveData()");

        // add barline to end, if not already there
        if(!mDataList.get(mDataList.size() - 1).isBarline()) {
            mDataList.add(new DataEntry(++mMeasureNumber, true));
        }
        mBuilder.dataEntries(mDataList);
        mDataMultipliedListener.dataValuesMultipliedBy(mDataMultipliedBy);
        getFragmentManager().popBackStackImmediate();
    }

    /**
     * returns to previous fragment WITHOUT saving the data
     */
    @OnClick(R.id.data_back_button)
    public void back() {

        if(mDataList == null || mDataList.size() == 0) {
            getFragmentManager().popBackStackImmediate();
        } else {
            losingDataAlertDialog();
        }
    }

    private void losingDataAlertDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.erase_data)
                .setMessage(R.string.delete_for_sure_question)
                .setPositiveButton(R.string.leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getFragmentManager().popBackStackImmediate();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @OnClick( { R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven,
            R.id.eight, R.id.nine, R.id.ten, R.id.twelve, R.id.other, R.id.barline } )
    public void dataEntered(TextView view) {
        String value = view.getText().toString();
        switch(value) {
            case "|":
                if(!mDataItemSelected && mDataList.get(mDataList.size() - 1).isBarline()) return;
                else addDataEntry(null, BARLINE);
                break;
            case "?":
                getIntegerDialogResponse();
                break;
            default:
                addDataEntry(value, BEAT);
                break;
        }

    }

    private void addDataEntry(String valueString, boolean beatOrBarline) {
        int value;
        if(beatOrBarline) {
            value = ++mMeasureNumber;
        } else {
            value = Integer.parseInt(valueString);
        }

        if(mDataItemSelected) {
            mDataList.add(mAdapter.selectedPosition++, new DataEntry(value, beatOrBarline));
            if(beatOrBarline) {
                resetMeasureNumbers();
            }
        } else {
            mDataList.add(new DataEntry(value, beatOrBarline));
        }

        mAdapter.notifyDataSetChanged();

        if(mDataItemSelected) {
            mEnteredDataRecycler.scrollToPosition(mAdapter.selectedPosition);
        } else {
            mEnteredDataRecycler.scrollToPosition(mDataList.size() - 1);
        }

    }

    private void getIntegerDialogResponse() {
        View view = View.inflate(getContext(), R.layout.get_integer_dialog_layout, null);
        final EditText editText = (EditText) view.findViewById(R.id.get_integer_edittext);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.enter_subdivision_value)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addDataEntry(editText.getText().toString(), BEAT);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();

        dialog.show();
        // force keyboard to show automatically
        //noinspection ConstantConditions
        dialog.getWindow().setSoftInputMode(WindowManager
                .LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /**
     * Proceeds up the data list until finding the previous barline, copies from there back to the
     * end again.
     */
    @OnClick(R.id.repeat_sign)
    public void repeatSignClicked() {
        int lastIndex = mDataList.size() - 1;
        // if it's not a barline at the end, add it....
        if(!mDataList.get(lastIndex).isBarline()) {
            mDataList.add(new DataEntry(++mMeasureNumber, BARLINE));
            lastIndex++;
        }
        int i;
        // reverse up the list looking for the previous barline
        for (i = lastIndex - 1; i >= 0; i--) {
            if(mDataList.get(i).isBarline()) break;
        }
        // follow back to the end, copying beats
        for (++i; i < lastIndex; i++) {
            mDataList.add(new DataEntry(mDataList.get(i).getData(), BEAT));
        }

        // add barline at end
        mDataList.add(new DataEntry(++mMeasureNumber, BARLINE));

        mAdapter.notifyDataSetChanged();
        mEnteredDataRecycler.scrollToPosition(mDataList.size() - 1);
    }

    /**
     * Adapter for displaying data as it is entered in the recycler view. Also keeps track
     * if an item is selected for editing.
     */
    public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.DataViewHolder> {

        private static final int VIEW_TYPE_BARLINE = 0;
        private static final int VIEW_TYPE_BEAT = 1;
        private int selectedPosition = -1;

        @Override
        public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            int layoutId;

            // select the xml layout file based on whether it is a beat or a barline
            switch(viewType) {
                case VIEW_TYPE_BARLINE:
                    layoutId = R.layout.data_input_barline_layout;
                    break;
                case VIEW_TYPE_BEAT:
                    layoutId = R.layout.data_input_single_entry_layout;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid view type, value of " + viewType);
            }

            View item = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            return new DataViewHolder(item);
        }

        @Override
        public void onBindViewHolder(final DataViewHolder holder, int position) {
            int data = mDataList.get(position).getData();

            holder.dataEntry.setText(String.valueOf(data));
            if(mDataList.get(position).isBarline()) {
                holder.itemView.setContentDescription(getString(
                        R.string.barline_content_description, data));
            }

            if(position == selectedPosition) {
                holder.itemView.setBackground(ContextCompat.getDrawable(getActivity(),
                        R.drawable.roundcorner_accent));
            } else {
                holder.itemView.setBackground(ContextCompat.getDrawable(getActivity(),
                        R.drawable.roundcorner_parchment));
            }

            // select/deselect data items for edit
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Update views
                    notifyItemChanged(selectedPosition);
                    if(selectedPosition == holder.getAdapterPosition()) {
                        selectedPosition = -1;
                        mDataItemSelected = false;
                    } else {
                        selectedPosition = holder.getAdapterPosition();
                        mDataItemSelected = true;
                    }
                    notifyItemChanged(selectedPosition);

                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataList == null ? 0 : mDataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(mDataList.get(position).isBarline()) {
                return VIEW_TYPE_BARLINE;
            } else {
                return VIEW_TYPE_BEAT;
            }
        }

        class DataViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.data_entry) TextView dataEntry;

            DataViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

        }
    }
}
