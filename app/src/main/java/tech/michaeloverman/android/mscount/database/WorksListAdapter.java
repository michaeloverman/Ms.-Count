/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.database;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.pojos.TitleKeyObject;
import timber.log.Timber;

/**
 * Created by Michael on 2/25/2017.
 */

public class WorksListAdapter extends RecyclerView.Adapter<WorksListAdapter.WorksViewHolder> {

    private final Context mContext;
    private List<TitleKeyObject> mTitles;
    private final WorksListAdapterOnClickHandler mClickHandler;

    public WorksListAdapter(@NonNull Context context, WorksListAdapterOnClickHandler handler) {
        mContext = context;
//        mTitles = titles;
        mClickHandler = handler;
    }

    interface WorksListAdapterOnClickHandler {
        void onClick(String key, String title);
    }

    @Override
    public WorksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Timber.d("onCreateViewHolder()");
        View item = LayoutInflater.from(mContext).inflate(R.layout.list_item_work, parent, false);
        return new WorksViewHolder(item);
    }

    @Override
    public void onBindViewHolder(WorksViewHolder holder, int position) {
//        Timber.d("onBindViewHolder()");
        holder.title.setText(mTitles.get(position).getTitle());
//        ViewCompat.setTransitionName(holder.title, "titleViewTrans" + position);
    }

    @Override
    public int getItemCount() {
        return mTitles == null ? 0 : mTitles.size();
    }

    public void setTitles(List<TitleKeyObject> titles) {
        mTitles = titles;
        Timber.d("setTitles() - " + mTitles.size() + " titles...");
        notifyDataSetChanged();
    }

    public void newCursor(Cursor data) {
        List<TitleKeyObject> titles = new ArrayList<>();
        if(data == null) {
            Timber.d("Null cursor...");
            mTitles = titles;
            return;
        }

        Timber.d("newCursor() data.getCount() == " + data.getCount());

        try {
            data.moveToFirst();
            while (!data.isAfterLast()) {
                titles.add(new TitleKeyObject(
                        data.getString(ProgramDatabaseSchema.MetProgram.POSITION_TITLE),
                        data.getString(ProgramDatabaseSchema.MetProgram.POSITION_ID)));
                data.moveToNext();
            }
        } catch (Exception exception) {
            Timber.d("Problem here: check this out...");
        }
        setTitles(titles);
    }

    class WorksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.work_title)
        TextView title;

        WorksViewHolder(View itemView) {
            super(itemView);
//            Timber.d("WorksViewHolder constructor()");
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Timber.d("WorksViewHolder onClick()");
            int position = getAdapterPosition();
            String key = mTitles.get(position).getKey();
            String title = mTitles.get(position).getTitle();
            mClickHandler.onClick(key, title);
        }
    }
}
