package tech.michaeloverman.android.mscount.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by Michael on 4/4/2017.
 */

public class ChangeClicksDialogFragment extends DialogFragment {

    private int mDownbeatId, mInnerbeatId;

    public interface ChangeClicksListener {
        void onClicksChanged(int downBeatId, int innerBeatId);
    }

    ChangeClicksListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ChangeClicksListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ChangeClickListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change click sound?")
                .setPositiveButton("Downbeat", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeDownBeat(mDownbeatId);
                    }
                })
                .setNeutralButton("Other beat", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeInnerBeat(mInnerbeatId);
                    }
                })
                .setNegativeButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onClicksChanged(mDownbeatId, mInnerbeatId);
                    }
                });
        return builder.create();
    }

    private void changeDownBeat(int id) {
        Toast.makeText(getContext(), "Changing downbeat", Toast.LENGTH_SHORT).show();
    }

    private void changeInnerBeat(int id) {
        Toast.makeText(getContext(), "Changing Innerbeat", Toast.LENGTH_SHORT).show();
    }
}
