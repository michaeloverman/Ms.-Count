package tech.michaeloverman.android.mscount.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.database.ProgramDatabaseSchema;
import tech.michaeloverman.android.mscount.favorites.FavoritesContract;
import tech.michaeloverman.android.mscount.programmed.ProgrammedMetronomeFragment;

/**
 * Created by Michael on 4/14/2017.
 */

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor data = null;
    AppWidgetManager widgetManager;
    int appWidgetId;

    private static final String[] PROGRAM_COLUMNS = {
            ProgramDatabaseSchema.MetProgram._ID,
            ProgramDatabaseSchema.MetProgram.COLUMN_TITLE
    };
    static final int INDEX_PROGRAM_ID = 0;
    static final int INDEX_PROGRAM_TITLE = 1;


    public WidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;

        if(intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
    }


    @Override
    public void onCreate() {
        widgetManager = AppWidgetManager.getInstance(mContext);
    }

    @Override
    public void onDataSetChanged() {
        if(data != null) {
            data.close();
        }

        // temporarily clear identity
        final long identityToken = Binder.clearCallingIdentity();

        Uri favoritesLocationUri = FavoritesContract.BASE_CONTENT_URI;
        data = mContext.getContentResolver().query(favoritesLocationUri,
                PROGRAM_COLUMNS,
                null,
                null,
                FavoritesContract.FavoriteEntry.COLUMN_PIECE_TITLE + " ASC");

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if(data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if(position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        String title = data.getString(INDEX_PROGRAM_TITLE);
        views.setTextViewText(R.id.widget_item_title, title);

        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(ProgrammedMetronomeFragment.PROGRAM_ID_EXTRA,
                data.getInt(INDEX_PROGRAM_ID));
        views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}