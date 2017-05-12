/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import tech.michaeloverman.android.mscount.R;
import timber.log.Timber;

/**
 * Created by Michael on 4/29/2017.
 */

public class WearNotification {

    private final String EXTRA_WEAR_INTENT_ID = "tech.michaeloverman.android.mscount.wearable.extra_message";

    private final Context mContext;
    private boolean mPlaying;
    private final String mTitle;
    private final String mMessage;

    public WearNotification(Context context, String title, String message) {
        mContext = context;
        mTitle = title;
        mMessage = message;
        mPlaying = false;
    }

    public void sendStartStop() {
        Timber.d("sendStartStop()");
        int notifId = 3435;
        Intent wearIntent = new Intent(Metronome.ACTION_METRONOME_START_STOP);
//        wearIntent.putExtra(EXTRA_WEAR_INTENT_ID, 42);
        PendingIntent wearPendingIntent = PendingIntent.getBroadcast(
                mContext, notifId, wearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Timber.d("Pending Intent created");

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                getIcon(),
                "Start/Stop",
                wearPendingIntent)
                .build();
        Timber.d("action created");

        NotificationCompat.WearableExtender wearExtend = new NotificationCompat.WearableExtender()
                .setContentAction(0)
                .setHintHideIcon(true)
                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_FULL_SCREEN)
                .setHintScreenTimeout(NotificationCompat.WearableExtender.SCREEN_TIMEOUT_LONG)
                .setBackground(BitmapFactory.decodeResource(
                        mContext.getResources(), R.mipmap.ic_launcher))
                .setContentIntentAvailableOffline(false)
//                .setHintAmbientBigPicture(true)
                .addAction(action);

        int color = ContextCompat.getColor(mContext, R.color.colorPrimary);
//        NotificationCompat.Style style = new android.support.v7.app.NotificationCompat.MediaStyle();
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(getIcon())
                .setContentTitle(mTitle)
                .setContentText(mMessage)
                .setColor(color)
                .setVibrate(new long[] { 50, 50, 500 })
                .setOnlyAlertOnce(true)
//                .setStyle(style)
                .addAction(action)
                .setContentIntent(wearPendingIntent)
                .extend(wearExtend);
//                .addAction(android.R.drawable.ic_media_play, playPauseIntent);
        Timber.d("notifBuilder created");

        Notification notif = notifBuilder.build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(mContext);
        manager.notify(notifId, notif);
        mPlaying = !mPlaying;
        Timber.d("manager has notified...");
    }

    private int getIcon() {
        if(mPlaying) {
            return android.R.drawable.ic_media_pause;
        } else {
            return android.R.drawable.ic_media_play;
        }
    }

    public void cancel() {
        NotificationManagerCompat.from(mContext).cancelAll();
    }
}
