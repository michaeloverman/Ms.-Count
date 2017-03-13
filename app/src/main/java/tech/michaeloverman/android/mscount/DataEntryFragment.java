package tech.michaeloverman.android.mscount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;

/**
 * Created by Michael on 3/12/2017.
 */

public class DataEntryFragment extends Fragment {
    private static final String TAG = DataEntryFragment.class.getSimpleName();

    private PieceOfMusic mPieceOfMusic;

    @BindView(R.id.data_title_view) TextView mTitleView;
    @BindView(R.id.entered_data_recycler_view) RecyclerView mEnteredDataRecycler;
    @BindView(R.id.barline) TextView mBarline;
    @BindView(R.id.one) TextView mOne;
    @BindView(R.id.two) TextView mTwo;
    @BindView(R.id.three) TextView mThree;
    @BindView(R.id.four) TextView mFour;
    @BindView(R.id.five) TextView mFive;
    @BindView(R.id.six) TextView mSix;
    @BindView(R.id.seven) TextView mSeven;
    @BindView(R.id.eight) TextView mEight;
    @BindView(R.id.nine) TextView mNine;
    @BindView(R.id.ten) TextView mTen;
    @BindView(R.id.eleven) TextView mEleven;
    @BindView(R.id.twelve) TextView mTwelve;
    @BindView(R.id.other) TextView mQuestion;
    @BindView(R.id.data_delete_button) Button mDeleteButton;

    private List<DataEntry> mDataList;
    private int mMeasureNumber;
    private DataListAdapter mAdapter;

    public static Fragment newInstance() {
        Log.d(TAG, "newInstance()");
        DataEntryFragment fragment = new DataEntryFragment();
//        fragment.mPieceOfMusic = piece;
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

//        mTitleView.setText(mPieceOfMusic.getTitle());
        mTitleView.setText("Dummy Text Title");

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mEnteredDataRecycler.setLayoutManager(manager);
        mAdapter = new DataListAdapter();
        mEnteredDataRecycler.setAdapter(mAdapter);

        return view;
    }

    @OnClick(R.id.data_delete_button)
    public void delete() {
        Log.d(TAG, "delete last data item");
        mDataList.remove(mDataList.size() - 1);
        mAdapter.notifyDataSetChanged();
    }

    @OnClick( { R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven,
            R.id.eight, R.id.nine, R.id.ten, R.id.eleven, R.id.twelve, R.id.other, R.id.barline } )
    public void subdivisionCountEntered(TextView view) {
        String value = view.getText().toString();
        switch(value) {
            case "|":
                mDataList.add(new DataEntry(++mMeasureNumber, true));
                break;
            case "1":
                mDataList.add(new DataEntry(1, false));
                break;
            case "2":
                mDataList.add(new DataEntry(2, false));
                break;
            case "3":
                mDataList.add(new DataEntry(3, false));
                break;
            case "4":
                mDataList.add(new DataEntry(4, false));
                break;
            case "5":
                mDataList.add(new DataEntry(5, false));
                break;
            case "6":
                mDataList.add(new DataEntry(6, false));
                break;
            case "7":
                mDataList.add(new DataEntry(7, false));
                break;
            case "8":
                mDataList.add(new DataEntry(8, false));
                break;
            case "9":
                mDataList.add(new DataEntry(9, false));
                break;
            case "10":
                mDataList.add(new DataEntry(10, false));
                break;
            case "11":
                mDataList.add(new DataEntry(11, false));
                break;
            case "12":
                mDataList.add(new DataEntry(12, false));
                break;
            case "?":
                mDataList.add(new DataEntry(13, false));
                break;
        }
        Log.d(TAG, value + " subdivisions in next beat");
        mAdapter.notifyDataSetChanged();
        mEnteredDataRecycler.scrollToPosition(mDataList.size() - 1);
    }

    public void dataEntryClicked(int position) {
        Log.d(TAG, "data point " + position + " clicked");
    }

    public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.DataViewHolder> {

        private static final int VIEW_TYPE_BARLINE = 0;
        private static final int VIEW_TYPE_BEAT = 1;

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
        public void onBindViewHolder(DataViewHolder holder, int position) {
            holder.dataEntry.setText(mDataList.get(position).getData() + "");
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

        class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.data_entry) TextView dataEntry;

            DataViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int value = getAdapterPosition();
                dataEntryClicked(value);
            }
        }
    }
}
