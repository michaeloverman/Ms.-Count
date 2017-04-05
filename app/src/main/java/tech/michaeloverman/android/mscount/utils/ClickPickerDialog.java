package tech.michaeloverman.android.mscount.utils;

import android.support.v4.app.DialogFragment;

/**
 * Created by Michael on 4/4/2017.
 */

public class ClickPickerDialog extends DialogFragment {

    public interface ClickPickerListener {
        void newClickPicked(int id);
    }
}
