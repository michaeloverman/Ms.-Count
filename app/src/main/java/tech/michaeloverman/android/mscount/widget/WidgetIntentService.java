/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.database.ProgramDatabaseSchema;
import tech.michaeloverman.android.mscount.favorites.FavoritesContract;
import tech.michaeloverman.android.mscount.programmed.ProgrammedMetronomeActivity;
import timber.log.Timber;

/**
 * Created by overm on 4/14/2017.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetIntentService extends IntentService {

    private static final String[] PROGRAM_COLUMNS = {
            ProgramDatabaseSchema.MetProgram._ID,
            ProgramDatabaseSchema.MetProgram.COLUMN_TITLE
    };
    static final int INDEX_PROGRAM_ID = 0;
    static final int INDEX_COLUMN_TITLE = 1;

    public WidgetIntentService() {
        super("WidgetIntentService");
        Timber.d("WIDGET IntentService created");
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Timber.d("onHandleIntent()");
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(this,
                MsCountWidgetProvider.class));

        Uri programLocationUri = FavoritesContract.FavoriteEntry.CONTENT_URI;
        Cursor data = getContentResolver().query(programLocationUri,
                PROGRAM_COLUMNS,
                null,
                null,
                null);
        data.moveToFirst();

        for(int widgetId : widgetIds) {
            String title = data.getString(INDEX_COLUMN_TITLE);

            RemoteViews views = new RemoteViews(
                    getPackageName(),
                    R.layout.widget_list_item);
            views.setTextViewText(R.id.widget_item_title, title);

            Intent launchIntent = new Intent(this, ProgrammedMetronomeActivity.class);
            PendingIntent pendingIntent = PendingIntent
                    .getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            widgetManager.updateAppWidget(widgetId, views);
        }
    }

//    private int getWidgetWidth(AppWidgetManager manager, int id) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            return getResources().getDimensionPixelSize(R.dimen.widget_default_width);
//        }
//        Bundle options = manager.getAppWidgetOptions(id);
//        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
//            int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
//            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidth, displayMetrics);
//        }
//        return getResources().getDimensionPixelSize(R.dimen.widget_default_width);
//    }
}

