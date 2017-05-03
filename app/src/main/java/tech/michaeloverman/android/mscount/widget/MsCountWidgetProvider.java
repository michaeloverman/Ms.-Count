/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import tech.michaeloverman.android.mscount.MsCountActivity;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.programmed.ProgrammedMetronomeActivity;
import timber.log.Timber;

import static tech.michaeloverman.android.mscount.favorites.FavoritesProvider.ACTION_DATA_UPDATED;

/**
 * Implementation of App Widget functionality.
 */
public class MsCountWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Timber.d("onUpdate: " + appWidgetIds.length + " widgets...");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Timber.d("updateAppWidget");
//        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ms_count_widget);
//        views.setTextViewText(R.id.widget_header, widgetText);

        Intent intent = new Intent(context, MsCountActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views, appWidgetId);
        } else {
            setRemoteAdapterV11(context, views, appWidgetId);
        }

        Intent clickIntentTemplate = new Intent(context, ProgrammedMetronomeActivity.class);

        Timber.d("clickIntentTemplate created");

        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntent(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
        Timber.d("PendingIntent set");
        views.setEmptyView(R.id.widget_list, R.id.widget_empty);
        Timber.d("Empty view set");

//         Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Timber.d("appWidgetManager called to updateAppWidget: " + appWidgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Timber.d("Provider onReceive(): " + intent.getAction());
        if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) ||
                intent.getAction().equals(ACTION_DATA_UPDATED)) {
            Timber.d("Widget related Intent caught: updating widget!!");
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            Timber.d(manager.toString());
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, getClass()));
            Timber.d("widget ids[0]: " + ids[0]);
            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
        } else {
            Timber.d("Non widget related intent caught: this shouldn't be happening....");
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Timber.d("onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Timber.d("onDisabled");
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, final RemoteViews views, int widgetId) {
        Intent intent = new Intent(context, WidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        views.setRemoteAdapter(R.id.widget_list, intent);
    }
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, RemoteViews views, int widgetId) {
        Intent intent = new Intent(context, WidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        views.setRemoteAdapter(0, R.id.widget_list, intent);
    }
}

