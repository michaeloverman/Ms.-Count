package tech.michaeloverman.android.mscount.pojos;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;

import tech.michaeloverman.android.mscount.R;

/**
 * Created by Michael on 3/7/2017.
 */

public class SubdivisionFAB extends FloatingActionButton {
    private final int[] colors;

    public SubdivisionFAB(Context context) {
        super(context);
        colors = context.getResources().getIntArray(R.array.subdivision_colors);
    }

    public void setAppearance(int level) {
        this.setBackgroundTintList(ColorStateList.valueOf(colors[level]));
    }
}
