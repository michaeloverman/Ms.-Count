package tech.michaeloverman.android.mscount.dataentry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.pojos.DataEntry;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;

/**
 * Created by Michael on 3/12/2017.
 */

public class DataEntryFragment extends Fragment {
    private static final String TAG = DataEntryFragment.class.getSimpleName();

    private PieceOfMusic.Builder mBuilder;
//    private PieceOfMusic mPieceOfMusic;

    static DataEntryCallback sDataEntryCallback;

    @BindView(R.id.data_title_view) TextView mTitleView;
    @BindView(R.id.entered_data_recycler_view) RecyclerView mEnteredDataRecycler;
//    @BindView(R.id.barline) TextView mBarline;
//    @BindView(R.id.one) TextView mOne;
//    @BindView(R.id.two) TextView mTwo;
//    @BindView(R.id.three) TextView mThree;
//    @BindView(R.id.four) TextView mFour;
//    @BindView(R.id.five) TextView mFive;
//    @BindView(R.id.six) TextView mSix;
//    @BindView(R.id.seven) TextView mSeven;
//    @BindView(R.id.eight) TextView mEight;
//    @BindView(R.id.nine) TextView mNine;
//    @BindView(R.id.ten) TextView mTen;
//    @BindView(R.id.twelve) TextView mTwelve;
//    @BindView(R.id.other) TextView mQuestion;
//    @BindView(R.id.data_delete_button) Button mDeleteButton;

    private String mTitle;
    private List<DataEntry> mDataList;
    private int mMeasureNumber;
    private DataListAdapter mAdapter;
    private boolean mDataItemSelected;
//    private int mSelectedDataItemPosition;
//    private DataListAdapter.DataViewHolder mSelectedDataItem;

    interface DataEntryCallback {
        void returnDataList(List<DataEntry> data, PieceOfMusic.Builder builder);
    }

    public static Fragment newInstance(String title, DataEntryCallback callback, PieceOfMusic.Builder builder) {
        Log.d(TAG, "newInstance()");
        DataEntryFragment fragment = new DataEntryFragment();
        fragment.mTitle = title;
        fragment.mBuilder = builder;
        sDataEntryCallback = callback;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        // set up variables
        mDataList = new ArrayList<>();
        mMeasureNumber = 0;
        mDataList.add(new DataEntry(++mMeasureNumber, true));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_input_layout, container, false);
        ButterKnife.bind(this, view);

        mTitleView.setText(mTitle);
//        mTitleView.setText("Dummy Text Title");

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mEnteredDataRecycler.setLayoutManager(manager);
        mAdapter = new DataListAdapter();
        mEnteredDataRecycler.setAdapter(mAdapter);

        return view;
    }

    @OnClick(R.id.data_delete_button)
    public void delete() {
        if(mDataItemSelected) {
            mDataList.remove(mAdapter.selectedPosition);
            if(mAdapter.selectedPosition >= mDataList.size()) {
                mAdapter.selectedPosition = -1;
                mDataItemSelected = false;
            }

        } else {
            mDataList.remove(mDataList.size() - 1);
        }
        mAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.data_save_button)
    public void saveData() {
//        mBuilder.dataEntries(mDataList);
//        mPieceOfMusic.setDataBeats(mDataList);
        // TODO Save to Firebase
        if(!mDataList.get(mDataList.size() - 1).isBarline()) {
            mDataList.add(new DataEntry(++mMeasureNumber, true));
        }
        sDataEntryCallback.returnDataList(mDataList, mBuilder);
        getFragmentManager().popBackStackImmediate();
    }

    @OnClick(R.id.data_back_button)
    public void back() {
//        mPieceOfMusic.setDataBeats(mDataList);
        getFragmentManager().popBackStackImmediate();
    }

    @OnClick( { R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven,
            R.id.eight, R.id.nine, R.id.ten, R.id.twelve, R.id.other, R.id.barline } )
    public void subdivisionCountEntered(TextView view) {
        String value = view.getText().toString();
        switch(value) {
            case "|":
                if(mDataItemSelected) {
//                    mAdapter.notifyItemChanged(mSelectedDataItemPosition);
                    mDataList.add(mAdapter.selectedPosition++, new DataEntry(++mMeasureNumber, true));

//                    mAdapter.notifyItemChanged(mSelectedDataItemPosition);
                }
                else mDataList.add(new DataEntry(++mMeasureNumber, true));
                break;
            case "?":
                // TODO Open dialog to get another number....
                break;
            default:
                if(mDataItemSelected) {
//                    mAdapter.notifyItemChanged(mSelectedDataItemPosition);
                    mDataList.add(mAdapter.selectedPosition++, new DataEntry(Integer.parseInt(value), false));
//                    mAdapter.notifyItemChanged(mSelectedDataItemPosition);
                }
                else mDataList.add(new DataEntry(Integer.parseInt(value), false));
                break;

        }
        Log.d(TAG, value + " subdivisions in next beat");
        mAdapter.notifyDataSetChanged();
        if(!mDataItemSelected) mEnteredDataRecycler.scrollToPosition(mDataList.size() - 1);
    }

    @OnClick(R.id.repeat_sign)
    public void repeatSignClicked() {
//        if(mDataItemSelected) unselectDataItem();

        int lastIndex = mDataList.size() - 1;
        if(mDataList.get(lastIndex).isBarline()) {
            mDataList.remove(lastIndex--);
        }
        int i;
        for (i = lastIndex; i >= 0; i--) {
            if(mDataList.get(i).isBarline()) break;
        }
        mDataList.add(new DataEntry(++mMeasureNumber, true));
        for (++i; i <= lastIndex; i++) {
            mDataList.add(mDataList.get(i));
        }
        mAdapter.notifyDataSetChanged();
        mEnteredDataRecycler.scrollToPosition(mDataList.size() - 1);
    }

    public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.DataViewHolder> {

        private static final int VIEW_TYPE_BARLINE = 0;
        private static final int VIEW_TYPE_BEAT = 1;
        private int selectedPosition = -1;

        @Override
        public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            int layoutId;

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
        public void onBindViewHolder(DataViewHolder holder, final int position) {
            holder.dataEntry.setText(mDataList.get(position).getData() + "");
//            holder.itemView.setSelected(selectedPosition == position);

            if(selectedPosition == position) {
                holder.itemView.setBackground(getResources().getDrawable(R.drawable.roundcorner_accent));
            } else {
                holder.itemView.setBackground(getResources().getDrawable(R.drawable.roundcorner_parchment));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Update views
                    notifyItemChanged(selectedPosition);
                    if(selectedPosition == position) {
                        selectedPosition = -1;
                        mDataItemSelected = false;
                    } else {
                        selectedPosition = position;
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
