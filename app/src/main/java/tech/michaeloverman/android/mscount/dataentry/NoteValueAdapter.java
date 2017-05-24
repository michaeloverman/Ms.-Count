package tech.michaeloverman.android.mscount.dataentry;

/**
 * Created by Michael on 5/24/2017.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.pojos.PieceOfMusic;
import timber.log.Timber;

/**
 * Adapter class handles the recycler view which provides options for baseline rhythmic values.
 */
class NoteValueAdapter extends RecyclerView.Adapter<NoteValueAdapter.NoteViewHolder> {
    private Context mContext;
    final TypedArray noteValueImages;
    private final String[] noteValueDescriptions;
    private int selectedPosition;

    public NoteValueAdapter(Context context, TypedArray images, String[] imageDescriptions) {
        mContext = context;
        noteValueImages = images;
        noteValueDescriptions = imageDescriptions;
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
        View item = LayoutInflater.from(mContext)
                .inflate(R.layout.note_value_image_view, parent, false);
        return new NoteViewHolder(item);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(NoteViewHolder holder, final int position) {
        holder.image.setImageDrawable(noteValueImages.getDrawable(position));
        Timber.d("onBindViewHolder, position: " + position + " selected: " + (position == selectedPosition));

        if(selectedPosition == position) {
            holder.itemView.setBackground(ContextCompat
                    .getDrawable(mContext, R.drawable.roundcorner_accent));
        } else {
            holder.itemView.setBackground(ContextCompat
                    .getDrawable(mContext, R.drawable.roundcorner_parchment));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(selectedPosition);
                selectedPosition = position;
                notifyItemChanged(selectedPosition);
            }
        });

        holder.itemView.setContentDescription(noteValueDescriptions[position]);
    }

    @Override
    public int getItemCount() {
        return noteValueImages.length();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        public NoteViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.note_value_image);
            Timber.d("NoteViewHolder created, image: ");
        }
    }
}
