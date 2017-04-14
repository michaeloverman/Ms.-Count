package tech.michaeloverman.android.mscount.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import tech.michaeloverman.android.mscount.MsCountActivity;
import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.programmed.ProgrammedMetronomeFragment;
import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class MsCountWidgetProvider extends AppWidgetProvider {

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ms_count_widget);
        views.setTextViewText(R.id.widget_header, widgetText);

        Intent intent = new Intent(context, MsCountActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_header, pendingIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views, appWidgetId);
        } else {
            setRemoteAdapterV11(context, views, appWidgetId);
        }

        Intent clickIntentTemplate = new Intent(context, ProgrammedMetronomeFragment.class);

        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntent(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
        views.setEmptyView(R.id.widget_list, R.id.widget_empty);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, final RemoteViews views, int widgetId) {
        Timber.d("setting remote adapter");
        Intent intent = new Intent(context, WidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        views.setRemoteAdapter(R.id.widget_list, intent);
    }
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, RemoteViews views, int widgetId) {
        Timber.d("setting remote adapter v11");
        Intent intent = new Intent(context, WidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        views.setRemoteAdapter(0, R.id.widget_list, intent);
    }
}

