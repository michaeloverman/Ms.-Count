package tech.michaeloverman.android.mscount.dataentry;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import timber.log.Timber;

/**
 * Created by Michael on 5/7/2017.
 */

public class MetaDataOptionsFragment extends Fragment {

    private Context mContext;
    private PieceOfMusic.Builder mBuilder;
    private int mBaselineRhythm;

    @BindView(R.id.measure_offset_entry) EditText mMeasureOffsetEntry;
    @BindView(R.id.tempo_multiplier_entry) EditText mTempoMultiplierEntry;
    @BindView(R.id.display_rhythmic_value_recycler) RecyclerView mDisplayValueEntry;
    private DisplayNoteValuesAdapter mDisplayValueAdapter;

    public static Fragment newInstance(Context context, PieceOfMusic.Builder builder, int baselineRhythm) {
        MetaDataOptionsFragment fragment = new MetaDataOptionsFragment();
        fragment.mContext = context;
        fragment.mBuilder = builder;
        fragment.mBaselineRhythm = baselineRhythm;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.meta_data_options_layout, container, false);
        ButterKnife.bind(this, view);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext,
                LinearLayoutManager.HORIZONTAL, false);
        mDisplayValueEntry.setLayoutManager(manager);
        mDisplayValueAdapter = new DisplayNoteValuesAdapter(
                getResources().obtainTypedArray(R.array.note_values));
        mDisplayValueEntry.setAdapter(mDisplayValueAdapter);
        mDisplayValueAdapter.setSelectedPosition(mBaselineRhythm);

        // Remove soft keyboard when focus on recycler
        mDisplayValueEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    InputMethodManager imm = (InputMethodManager) v.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        return view;
    }

    @OnClick(R.id.options_cancel_button)
    public void cancelOptionsWithoutSave() {
        getFragmentManager().popBackStackImmediate();
    }

    @OnClick(R.id.save_options_button)
    public void save() {
        int offset = Integer.parseInt(mMeasureOffsetEntry.getText().toString());
        float multiplier = Float.parseFloat(mTempoMultiplierEntry.getText().toString());
        int display = mDisplayValueAdapter.getSelectedRhythm();

        mBuilder.firstMeasureNumber(offset)
                .tempoMultiplier(multiplier)
                .displayNoteValue(display);

        getFragmentManager().popBackStackImmediate();
    }

    class DisplayNoteValuesAdapter extends RecyclerView.Adapter<DisplayNoteValuesAdapter.NoteViewHolder> {

        TypedArray noteValueImages;
        private int selectedPosition;
        private String[] descriptions = getResources()
                .getStringArray(R.array.note_value_content_descriptions);

        public DisplayNoteValuesAdapter(TypedArray images) {
//            Timber.d("NoteValuesAdapter CREATED!!!");
//            Timber.d("TypedArray: " + images.toString());
            noteValueImages = images;
//            Timber.d("Index count: " + noteValueImages.length());
        }

        public int getSelectedRhythm() {
            switch(selectedPosition) {
                case 0: return PieceOfMusic.SIXTEENTH;
                case 1: return PieceOfMusic.DOTTED_SIXTEENTH;
                case 2: return PieceOfMusic.EIGHTH;
                case 3: return PieceOfMusic.DOTTED_EIGHTH;
                case 4: return PieceOfMusic.QUARTER;
                case 5: return PieceOfMusic.DOTTED_QUARTER;
                case 6: return PieceOfMusic.HALF;
                case 7: return PieceOfMusic.DOTTED_HALF;
                case 8: return PieceOfMusic.WHOLE;
                default: return PieceOfMusic.QUARTER;
            }
        }

        public void setSelectedPosition(int rhythm) {
            Timber.d("setting selected rhythmic value..." + rhythm);
            switch(rhythm) {
                case PieceOfMusic.SIXTEENTH:
                    selectedPosition = 0;
                    break;
                case PieceOfMusic.DOTTED_SIXTEENTH:
                    selectedPosition = 1;
                    break;
                case PieceOfMusic.EIGHTH:
                    selectedPosition = 2;
                    break;
                case PieceOfMusic.DOTTED_EIGHTH:
                    selectedPosition = 3;
                    break;
                case PieceOfMusic.QUARTER:
                    selectedPosition = 4;
                    break;
                case PieceOfMusic.DOTTED_QUARTER:
                    selectedPosition = 5;
                    break;
                case PieceOfMusic.HALF:
                    selectedPosition = 6;
                    break;
                case PieceOfMusic.DOTTED_HALF:
                    selectedPosition = 7;
                    break;
                case PieceOfMusic.WHOLE:
                    selectedPosition = 8;
                    break;
                default: selectedPosition = 4;
            }
            Timber.d("selectedPosition = " + selectedPosition);
        }

        @Override
        public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.note_value_image_view, parent, false);
            return new NoteViewHolder(item);
        }

        @Override
        public void onBindViewHolder(NoteViewHolder holder, final int position) {
            holder.image.setImageDrawable(noteValueImages.getDrawable(position));
            Timber.d("onBindViewHolder, position: " + position + " selected: " + (position == selectedPosition));

            if(selectedPosition == position) {
                holder.itemView.setBackground(getResources().getDrawable(R.drawable.roundcorner_accent));
            } else {
                holder.itemView.setBackground(getResources().getDrawable(R.drawable.roundcorner_parchment));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemChanged(selectedPosition);
                    selectedPosition = position;
                    notifyItemChanged(selectedPosition);
                }
            });

            holder.itemView.setContentDescription(descriptions[position]);
        }

        @Override
        public int getItemCount() {
            return noteValueImages.length();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder {
            ImageView image;

            public NoteViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.note_value_image);
                Timber.d("NoteViewHolder created, image: ");
            }
        }
    }
}
